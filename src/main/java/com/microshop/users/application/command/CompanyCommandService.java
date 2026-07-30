package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.CompanyModuleEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasModuleEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.CompanyModuleRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasModuleRepository;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.infrastructure.persistence.entity.SaasSubscriptionEntity;
import com.microshop.users.infrastructure.persistence.repository.SaasPlanRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasSubscriptionRepository;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.ImagenBinariaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyCommandService {

    private final CompanyRepository companyRepository;
    private final CompanyModuleRepository companyModuleRepository;
    private final SaasModuleRepository saasModuleRepository;
    private final SaasPlanRepository planRepository;
    private final SaasSubscriptionRepository subscriptionRepository;
    private final SesionRevocacionService sesionRevocacionService;
    private final MessageHelper msg;

    /** Plan con el que nace toda empresa nueva. Mismo que usa el autoservicio SaaS. */
    private static final String PLAN_INICIAL = "STARTER";

    /**
     * Alta de empresa desde el panel de administración.
     *
     * <p><b>Crea también una suscripción TRIAL (M06, 2026-07-29).</b> Antes no lo hacía, y el flujo de
     * autoservicio ({@code SaasOnboardingCommandService}) sí — de modo que las empresas creadas por un
     * administrador quedaban <b>sin plan</b>. Eso no era inocuo: {@code getEnabledModuleCodes}
     * interpretaba «sin suscripción» como «todos los módulos», así que un alta por esta vía concedía el
     * catálogo completo. Medido: 4 de las 7 empresas de la base de desarrollo estaban en esa situación.
     * Con el fallback ya cerrado, no crear la suscripción dejaría a la empresa sin poder operar, que es
     * el otro extremo. Se crea igual que en el autoservicio: TRIAL de 30 días sobre {@code STARTER},
     * que un SUPERADMIN puede cambiar después.</p>
     */
    public CompanyEntity createCompany(CompanyEntity company) {
        if (companyRepository.existsByRuc(company.getRuc())) {
            // ConflictException (409), no IllegalArgumentException: es un choque con un registro
            // existente, no un argumento mal formado. Convención de shared/exception.
            throw new ConflictException(msg.get("company.ruc.exists", company.getRuc()));
        }
        CompanyEntity guardada = companyRepository.save(company);
        crearSuscripcionInicial(guardada);
        return guardada;
    }

    /**
     * Da a la empresa una suscripción TRIAL si no tiene ninguna. Idempotente: si ya existe no la toca,
     * para que llamarlo dos veces (o desde un backfill) no duplique.
     *
     * <p>No falla el alta si el plan base no está sembrado: se registra y se sigue. Bloquear la creación
     * de una empresa por un catálogo de planes incompleto sería peor que dejarla sin plan, sobre todo
     * ahora que «sin plan» se ve en el log en lugar de conceder acceso total en silencio.</p>
     */
    private void crearSuscripcionInicial(CompanyEntity company) {
        if (subscriptionRepository.findByCompanyId(company.getId()).isPresent()) {
            return;
        }
        var plan = planRepository.findByCode(PLAN_INICIAL);
        if (plan.isEmpty()) {
            log.error("No existe el plan {}: la empresa {} queda SIN suscripcion y por tanto sin modulos "
                    + "habilitados. Sembrar el catalogo de planes y crearsela.", PLAN_INICIAL, company.getId());
            return;
        }
        subscriptionRepository.save(SaasSubscriptionEntity.builder()
                .company(company)
                .plan(plan.get())
                .status("TRIAL")
                .startsAt(Instant.now())
                .trialEndsAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build());
        log.info("Empresa {} creada con suscripcion TRIAL sobre el plan {}", company.getId(), PLAN_INICIAL);
    }

    public CompanyEntity updateCompany(Long id, CompanyEntity companyDetails) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("company.not.found.with.id", id)));

        company.setName(companyDetails.getName());
        company.setRuc(companyDetails.getRuc());
        company.setActive(companyDetails.isActive());
        company.setLegalName(companyDetails.getLegalName());
        company.setAddress(companyDetails.getAddress());
        company.setPhone(companyDetails.getPhone());
        company.setEmail(companyDetails.getEmail());
        // logo_url es SOLO para logos externos. Si el formulario reenvía la URL calculada
        // del binario (round-trip de /users/api/companies/{id}/logo), se conserva el valor
        // actual en vez de persistir una ruta que apunta a este mismo endpoint.
        if (!esUrlLogoBinario(companyDetails.getLogoUrl())) {
            company.setLogoUrl(companyDetails.getLogoUrl());
        }
        company.setDomain(companyDetails.getDomain());

        return companyRepository.save(company);
    }

    /** {@code true} si la URL es la ruta calculada del logo binario de este servicio. */
    private static boolean esUrlLogoBinario(String url) {
        return url != null && url.startsWith(ApiPaths.COMPANIES + "/") && url.endsWith("/logo");
    }

    /**
     * Suspende o reactiva una empresa tocando ÚNICAMENTE {@code active}.
     *
     * <p>Deliberadamente NO reutiliza {@link #updateCompany}: ese método sobrescribe ocho campos con
     * lo que venga en el DTO, así que usarlo para girar un booleano pone a null todo lo que el
     * llamador no reenvíe. Es la misma clase de fallo que el proyecto ya documentó con
     * {@code form.getRawValue()} en el frontend: un campo ausente no se ignora, borra el dato.</p>
     *
     * <p>Queda registrado en el log de la aplicación y, desde M05, también en {@code company_aud} vía
     * Envers, con el valor anterior — que es lo que permite reconstruir quién suspendió a quién.</p>
     */
    public CompanyEntity cambiarEstado(Long id, boolean activa) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(msg.get("company.not.found.with.id", id)));

        if (company.isActive() == activa) {
            // Idempotente: repetir la operación no es un error, y devolver la empresa tal cual evita
            // que el frontend tenga que distinguir «ya estaba así» de «no se pudo».
            log.info("Empresa {} ya estaba {}; no se cambia nada", id, activa ? "activa" : "suspendida");
            return company;
        }

        company.setActive(activa);
        CompanyEntity guardada = companyRepository.save(company);

        if (!activa) {
            // M27: suspender sin cortar las sesiones vivas dejaba a los usuarios dentro operando con
            // normalidad hasta que su token expirase — hasta 24 h. B08 cerró el login; esto cierra lo
            // ya emitido. Efectivo de inmediato en este servicio, que consulta la sesión en su filtro;
            // los otros cinco lo verán al adoptar la comprobación de jti (ver SesionRevocacionService).
            int revocadas = sesionRevocacionService.revocarPorEmpresa(id);
            log.info("Empresa {} ({}) SUSPENDIDA; {} sesiones vivas revocadas", id, company.getRuc(), revocadas);
        } else {
            log.info("Empresa {} ({}) REACTIVADA", id, company.getRuc());
        }
        return guardada;
    }

    /**
     * Soft delete de la empresa.
     *
     * <p>Revoca las sesiones igual que {@link #cambiarEstado(Long, boolean)}: esto pone
     * {@code active = false}, así que es una suspensión por otra puerta y tenía el mismo agujero.
     * Dejarlo sin revocar significaría que «eliminar» una empresa es menos efectivo que suspenderla.</p>
     */
    public void deleteCompany(Long id) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(msg.get("company.not.found.with.id", id)));
        company.setActive(false);
        companyRepository.save(company);
        int revocadas = sesionRevocacionService.revocarPorEmpresa(id);
        log.info("Empresa {} dada de baja (soft delete); {} sesiones vivas revocadas", id, revocadas);
    }

    /**
     * Almacena el logotipo binario de la empresa. Valida MIME, tamaño (máx 500 KB) y magic bytes.
     * Calcula el MD5 del binario como ETag para caché HTTP y anula la URL externa previa.
     */
    public CompanyEntity uploadLogo(Long companyId, MultipartFile file) {
        byte[] bytes = ImagenBinariaUtils.validarYExtraer(file);

        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("company.not.found.with.id", companyId)));

        company.setLogoData(bytes);
        company.setLogoMime(file.getContentType());
        company.setLogoEtag(DigestUtils.md5DigestAsHex(bytes));
        company.setLogoSize(bytes.length);
        company.setLogoUrl(null);

        log.info("Logo actualizado para empresa {} ({} bytes)", companyId, bytes.length);
        return companyRepository.save(company);
    }

    /**
     * Elimina el logotipo binario de la empresa: borra el blob y TODOS sus metadatos
     * (mime, etag, size) para que {@code serveLogo} responda 404 y la entidad quede
     * consistente. Mismo patrón que CategoriaCommandService.deleteImagen en ventas.
     */
    public CompanyEntity deleteLogo(Long companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("company.not.found.with.id", companyId)));

        company.setLogoData(null);
        company.setLogoMime(null);
        company.setLogoEtag(null);
        company.setLogoSize(null);
        company.setLogoUrl(null);

        log.info("Logo eliminado para empresa {}", companyId);
        return companyRepository.save(company);
    }

    public void toggleModule(Long companyId, Long moduleId, boolean enabled) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("company.not.found.with.id", companyId)));
        SaasModuleEntity module = saasModuleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduleId));

        List<CompanyModuleEntity> existing = companyModuleRepository.findByCompanyId(companyId);
        CompanyModuleEntity override = existing.stream()
                .filter(cm -> cm.getModule().getId().equals(moduleId))
                .findFirst()
                .orElse(null);

        if (override != null) {
            override.setEnabled(enabled);
            companyModuleRepository.save(override);
        } else {
            companyModuleRepository.save(CompanyModuleEntity.builder()
                    .company(company)
                    .module(module)
                    .isEnabled(enabled)
                    .build());
        }
    }
}
