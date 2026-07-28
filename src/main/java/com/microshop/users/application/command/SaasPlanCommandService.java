package com.microshop.users.application.command;

import com.microshop.users.application.dto.SaasPlanAdminDto;
import com.microshop.users.application.dto.SaasPlanAdminRequest;
import com.microshop.users.infrastructure.persistence.entity.SaasModuleEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasPlanEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasPlanModuleEntity;
import com.microshop.users.infrastructure.persistence.repository.SaasModuleRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasPlanModuleRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasPlanRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * CRUD de planes SaaS (nombre, precios, límite de usuarios, módulos incluidos) — configuración
 * global de la plataforma, restringida a SUPERADMIN (ver SecurityConfig + SaasAdminController).
 * Nunca DELETE físico: saas_subscription referencia el plan por FK, se usa isActive (soft-hide).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaasPlanCommandService {

    private final SaasPlanRepository planRepository;
    private final SaasModuleRepository moduleRepository;
    private final SaasPlanModuleRepository planModuleRepository;

    @Transactional
    public SaasPlanAdminDto createPlan(SaasPlanAdminRequest request) {
        log.info("Creating SaaS plan: {}", request.code());

        if (planRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("Ya existe un plan con el código " + request.code());
        }

        var plan = SaasPlanEntity.builder()
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .priceMonthly(request.priceMonthly())
                .priceAnnual(request.priceAnnual())
                .maxUsers(request.maxUsers())
                .isActive(true)
                .build();
        plan = planRepository.save(plan);

        replaceModules(plan.getId(), request.moduleCodes());

        log.info("SaaS plan created: {} (id={})", plan.getCode(), plan.getId());
        return toAdminDto(plan);
    }

    @Transactional
    public SaasPlanAdminDto updatePlan(Long id, SaasPlanAdminRequest request) {
        log.info("Updating SaaS plan: {}", id);

        var plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("saas.plan.not.found"));

        planRepository.findByCode(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Ya existe otro plan con el código " + request.code());
                });

        plan.setCode(request.code());
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setPriceMonthly(request.priceMonthly());
        plan.setPriceAnnual(request.priceAnnual());
        plan.setMaxUsers(request.maxUsers());
        plan = planRepository.save(plan);

        replaceModules(plan.getId(), request.moduleCodes());

        log.info("SaaS plan updated: {} (id={})", plan.getCode(), id);
        return toAdminDto(plan);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        log.info("Setting SaaS plan {} active={}", id, active);
        var plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("saas.plan.not.found"));
        plan.setActive(active);
        planRepository.save(plan);
    }

    /** Reemplaza el set completo de módulos del plan — más simple que un diff incremental. */
    private void replaceModules(Long planId, List<String> moduleCodes) {
        planModuleRepository.deleteByPlanId(planId);
        if (moduleCodes == null || moduleCodes.isEmpty()) return;

        // El request puede repetir un código (la UI arma la lista desde varios checkboxes) y
        // `uk_plan_module` es (plan_id, module_id): sin deduplicar, el segundo INSERT da 500.
        // LinkedHashSet para no alterar el orden en que llegaron.
        var codigos = new LinkedHashSet<>(moduleCodes);

        var plan = planRepository.getReferenceById(planId);
        for (String code : codigos) {
            SaasModuleEntity module = moduleRepository.findByCode(code)
                    .orElseThrow(() -> new BusinessException("Módulo desconocido: " + code));
            planModuleRepository.save(SaasPlanModuleEntity.builder()
                    .plan(plan)
                    .module(module)
                    .build());
        }
    }

    private SaasPlanAdminDto toAdminDto(SaasPlanEntity plan) {
        List<String> moduleCodes = planRepository.findModuleCodesByPlanId(plan.getId());
        return new SaasPlanAdminDto(plan.getId(), plan.getCode(), plan.getName(), plan.getDescription(),
                plan.getPriceMonthly(), plan.getPriceAnnual(), plan.getMaxUsers(), moduleCodes, plan.isActive());
    }
}
