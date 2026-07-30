package com.microshop.users.application.command;

import com.microshop.users.config.security.JwtClaims;

import com.microshop.users.application.dto.SaasRegisterRequest;
import com.microshop.users.application.dto.SaasRegisterResponse;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.config.security.JwtService;
import com.microshop.users.infrastructure.persistence.entity.*;
import com.microshop.users.infrastructure.persistence.repository.*;
import com.microshop.users.shared.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SaasOnboardingCommandService {

    private final CompanyRepository companyRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final SaasPlanRepository planRepository;
    private final SaasSubscriptionRepository subscriptionRepository;
    private final SesionRepository sesionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SaasQueryService saasQueryService;

    public SaasRegisterResponse register(SaasRegisterRequest request) {
        // 1. Validar unicidad
        if (companyRepository.findByRuc(request.ruc()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con RUC: " + request.ruc());
        }
        if (usuarioRepository.findByUsername(request.adminEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        // 2. Crear empresa
        CompanyEntity company = CompanyEntity.builder()
                .name(request.companyName())
                .ruc(request.ruc())
                .isActive(true)
                .build();
        company = companyRepository.save(company);

        // 3. Obtener rol ADMIN (debe existir del seed V3)
        RolEntity rolAdmin = rolRepository.findByNombre(AppConstants.Seguridad.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado. Ejecute las migraciones."));

        // 4. Crear PersonaEntity requerida por UsuarioEntity (NOT NULL)
        PersonaEntity persona = PersonaEntity.builder()
                .nombres(request.adminNombres())
                .apellidos(request.adminApellidos())
                .tipoDocumento("DNI")
                .numeroDocumento(generateTempDocumento(request.ruc()))
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build();
        persona = personaRepository.save(persona);

        // 5. Crear usuario admin
        UsuarioEntity usuario = UsuarioEntity.builder()
                .username(request.adminEmail())
                .password(passwordEncoder.encode(request.adminPassword()))
                .email(request.adminEmail())
                .rol(rolAdmin)
                .persona(persona)
                .build();
        usuario = usuarioRepository.save(usuario);

        // 6. Vincular usuario con empresa
        UserCompanyEntity userCompany = UserCompanyEntity.builder()
                .usuario(usuario)
                .company(company)
                .isActive(true)
                .build();
        userCompanyRepository.save(userCompany);

        // 7. Crear suscripción de prueba
        String planCode = request.resolvedPlanCode();
        SaasPlanEntity plan = planRepository.findByCode(planCode)
                .orElseGet(() -> planRepository.findByCode("STARTER")
                        .orElseThrow(() -> new IllegalStateException("Plan STARTER no encontrado")));

        SaasSubscriptionEntity subscription = SaasSubscriptionEntity.builder()
                .company(company)
                .plan(plan)
                .status("TRIAL")
                .startsAt(Instant.now())
                .trialEndsAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        subscriptionRepository.save(subscription);

        // 8. Generar JWT con módulos habilitados.
        //
        // El claim `roles` NO se emitía aquí y sí en AuthCommandService: quien se registraba por este
        // flujo recibía un token SIN roles, así que todo hasRole()/hasAuthority() le fallaba en los seis
        // servicios hasta que volviera a iniciar sesión por la ruta normal. Las dos rutas de emisión
        // habían divergido sin que nada lo detectara, que es la razón de ser de JwtClaims.
        List<String> enabledModules = saasQueryService.getEnabledModuleCodes(company.getId());
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaims.USER_ID, usuario.getId());
        claims.put(JwtClaims.COMPANY_ID, company.getId());
        if (usuario.getRol() != null && usuario.getRol().getNombre() != null) {
            claims.put(JwtClaims.ROLES, List.of("ROLE_" + usuario.getRol().getNombre().toUpperCase()));
        }
        if (!enabledModules.isEmpty()) {
            claims.put(JwtClaims.MODULES, String.join(JwtClaims.MODULES_SEPARATOR, enabledModules));
        }
        var userDetails = new User(usuario.getUsername(), usuario.getPassword(), Collections.emptyList());
        String token = jwtService.generateToken(claims, userDetails);

        // 9. Crear sesión
        SesionEntity session = SesionEntity.builder()
                .usuario(usuario)
                .token(token)
                .fechaInicio(Instant.now())
                .fechaExpiracion(Instant.now().plus(1, ChronoUnit.DAYS))
                .valido(true)
                // jti: identificador de revocacion (M27), igual que en el login.
                .jti(jwtService.extractJti(token))
                .companyId(company.getId())
                .build();
        sesionRepository.save(session);

        log.info("SaaS onboarding completado para empresa {} (plan: {})", company.getId(), planCode);

        return new SaasRegisterResponse(company.getId(), usuario.getId(), token,
                usuario.getUsername(), plan.getCode(), "TRIAL", enabledModules);
    }

    /**
     * Genera un número de documento temporal único basado en el RUC.
     * Se usan los últimos 8 dígitos del RUC como DNI provisional.
     */
    private String generateTempDocumento(String ruc) {
        return "T" + ruc.substring(ruc.length() - 7);
    }
}
