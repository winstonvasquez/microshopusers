package com.microshop.users.application.query;

import com.microshop.users.application.dto.SaasModuleDto;
import com.microshop.users.application.dto.SaasPlanDto;
import com.microshop.users.application.dto.CompanyProfileDto;
import com.microshop.users.application.mapper.CompanyMapper;
import com.microshop.users.infrastructure.persistence.entity.*;
import com.microshop.users.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SaasQueryService {

    private final SaasModuleRepository moduleRepository;
    private final SaasPlanRepository planRepository;
    private final SaasSubscriptionRepository subscriptionRepository;
    private final CompanyModuleRepository companyModuleRepository;
    private final CompanyRepository companyRepository;

    /**
     * Devuelve la lista de códigos de módulo habilitados para una empresa.
     * Lógica: módulos del plan UNION overrides por empresa (enabled=true).
     *
     * <p><b>Corregido el 2026-07-29 (M06).</b> Cuando la empresa no tenía suscripción, esto devolvía
     * <b>todos los módulos activos</b> «como fallback para datos existentes». Es exactamente lo
     * contrario de lo que un SaaS debe hacer: significaba que <b>un tenant sin plan contratado recibía
     * el catálogo completo</b> — POS, ventas, compras, inventario, contabilidad, logística, tesorería y
     * RRHH— en el claim del token. Medido en la base de desarrollo: de 7 empresas, solo 3 tenían
     * suscripción, así que <b>4 estaban operando con acceso total sin plan</b>. Y como además ningún
     * backend validaba el claim, el plan no restringía nada por ninguno de los dos lados.</p>
     *
     * <p>Ahora sin suscripción no hay módulos. El registro por el flujo SaaS y el alta de empresa
     * crean una suscripción TRIAL, así que el caso «sin suscripción» pasa a ser lo que siempre debió
     * ser: una anomalía de datos que se ve, no un permiso implícito. Se registra en el log para que se
     * pueda encontrar en vez de quedar silenciosa.</p>
     */
    public List<String> getEnabledModuleCodes(Long companyId) {
        if (companyId == null) return List.of();

        // Obtener módulos del plan vía suscripción
        List<String> planModules = subscriptionRepository.findEnabledModuleCodesByCompanyId(companyId);

        if (planModules.isEmpty()) {
            // Fail-closed. Antes se devolvían TODOS los módulos aquí; ver la nota del javadoc.
            log.warn("La empresa {} no tiene suscripcion con modulos: se emite el token SIN modulos "
                    + "habilitados. Si deberia poder operar, hay que darle una suscripcion "
                    + "(POST /users/api/saas/subscriptions) — antes esto le concedia el catalogo completo.",
                    companyId);
            return List.of();
        }

        // Aplicar overrides por empresa
        Set<String> enabled = new LinkedHashSet<>(planModules);
        List<CompanyModuleEntity> overrides = companyModuleRepository.findByCompanyId(companyId);
        for (CompanyModuleEntity override : overrides) {
            if (override.isEnabled()) {
                enabled.add(override.getModule().getCode());
            } else {
                enabled.remove(override.getModule().getCode());
            }
        }
        return new ArrayList<>(enabled);
    }

    public List<SaasModuleDto> getEnabledModules(Long companyId) {
        if (companyId == null) {
            // Retorna todos los módulos activos sin marcar enabled
            return moduleRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                    .map(m -> new SaasModuleDto(m.getId(), m.getCode(), m.getName(),
                            m.getDescription(), m.getIcon(), m.getRoutePrefix(), true))
                    .collect(Collectors.toList());
        }
        List<String> enabledCodes = getEnabledModuleCodes(companyId);
        Set<String> enabledSet = new HashSet<>(enabledCodes);
        return moduleRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(m -> new SaasModuleDto(m.getId(), m.getCode(), m.getName(),
                        m.getDescription(), m.getIcon(), m.getRoutePrefix(), enabledSet.contains(m.getCode())))
                .collect(Collectors.toList());
    }

    public List<SaasPlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .filter(SaasPlanEntity::isActive)
                .map(plan -> new SaasPlanDto(plan.getId(), plan.getCode(), plan.getName(),
                        plan.getDescription(), plan.getPriceMonthly(), plan.getPriceAnnual(),
                        plan.getMaxUsers(), planRepository.findModuleCodesByPlanId(plan.getId())))
                .collect(Collectors.toList());
    }

    /** Ver {@link #getAllPlans()} pero incluye planes inactivos — solo para el panel admin (SUPERADMIN). */
    public List<com.microshop.users.application.dto.SaasPlanAdminDto> getAllPlansForAdmin() {
        return getAllPlansForAdmin(null, null, null);
    }

    /**
     * Ver {@link #getAllPlansForAdmin()} con filtros avanzados opcionales: búsqueda por texto
     * (código/nombre/descripción), estado activo/inactivo y módulo incluido en el plan.
     */
    public List<com.microshop.users.application.dto.SaasPlanAdminDto> getAllPlansForAdmin(
            String search, Boolean isActive, String moduleCode) {
        String term = com.microshop.users.shared.util.AppUtils.searchTermOrNull(search);
        String module = com.microshop.users.shared.util.AppUtils.searchTermOrNull(moduleCode);
        return planRepository.searchPlans(term, isActive, module).stream()
                .map(plan -> new com.microshop.users.application.dto.SaasPlanAdminDto(plan.getId(), plan.getCode(), plan.getName(),
                        plan.getDescription(), plan.getPriceMonthly(), plan.getPriceAnnual(),
                        plan.getMaxUsers(), planRepository.findModuleCodesByPlanId(plan.getId()), plan.isActive()))
                .collect(Collectors.toList());
    }

    public CompanyProfileDto getCompanyProfile(Long companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        var subscription = subscriptionRepository.findByCompanyId(companyId);
        String planCode = subscription.map(s -> s.getPlan().getCode()).orElse("NONE");
        String status = subscription.map(SaasSubscriptionEntity::getStatus).orElse("NONE");
        List<SaasModuleDto> modules = getEnabledModules(companyId);
        return new CompanyProfileDto(company.getId(), company.getName(), company.getRuc(),
                company.getLegalName(), company.getAddress(), company.getPhone(),
                company.getEmail(), CompanyMapper.resolveLogoUrl(company), planCode, status, modules);
    }
}
