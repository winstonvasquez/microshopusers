package com.microshop.users.application.command;

import com.microshop.users.application.dto.SubscriptionAdminDto;
import com.microshop.users.infrastructure.persistence.entity.SaasSubscriptionEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasPlanRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasSubscriptionRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/** Lectura y cambio de plan/estado de la suscripción de una empresa (SUPERADMIN). */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaasSubscriptionCommandService {

    /** Valores de {@code saas_subscription.status}; la columna es VARCHAR(20) sin CHECK. */
    static final Set<String> ESTADOS = Set.of("TRIAL", "ACTIVE", "CANCELLED", "EXPIRED");

    private final SaasSubscriptionRepository subscriptionRepository;
    private final SaasPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final SesionRevocacionService sesionRevocacionService;

    @Transactional(readOnly = true)
    public SubscriptionAdminDto get(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new NotFoundException("Empresa no encontrada");
        }
        return subscriptionRepository.findByCompanyId(companyId)
                .map(SaasSubscriptionCommandService::toDto)
                .orElseThrow(() -> new NotFoundException("La empresa no tiene suscripción"));
    }

    /**
     * Sin @Transactional a propósito: el guardado se compromete (save de Spring Data) ANTES de revocar
     * sesiones. Revocar primero dejaría una ventana en la que el usuario reingresa con el plan viejo.
     */
    public SubscriptionAdminDto change(Long companyId, String planCode, String status) {
        String estado = status.trim().toUpperCase();
        if (!ESTADOS.contains(estado)) {
            throw new BusinessException("Estado de suscripción inválido: " + status + " (válidos: " + ESTADOS + ")");
        }
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada"));
        var plan = planRepository.findByCode(planCode.trim())
                .filter(p -> p.isActive())
                .orElseThrow(() -> new BusinessException("Plan inexistente o inactivo: " + planCode));

        Optional<SaasSubscriptionEntity> actual = subscriptionRepository.findByCompanyId(companyId);
        SaasSubscriptionEntity sub = actual.orElseGet(() -> SaasSubscriptionEntity.builder()
                .company(company)
                .startsAt(Instant.now())
                .build());
        sub.setPlan(plan);
        sub.setStatus(estado);
        sub = subscriptionRepository.save(sub);

        // El claim `modules` del JWT sale del plan: hay que forzar un nuevo login para regenerarlo.
        sesionRevocacionService.revocarPorEmpresa(companyId);
        log.info("Suscripción de la empresa {} -> plan {} / {}", companyId, plan.getCode(), estado);
        return toDto(sub);
    }

    private static SubscriptionAdminDto toDto(SaasSubscriptionEntity s) {
        return new SubscriptionAdminDto(s.getCompany().getId(), s.getPlan().getCode(), s.getPlan().getName(),
                s.getStatus(), s.getTrialEndsAt());
    }
}
