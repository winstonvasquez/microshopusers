package com.microshop.users.application.query;

import com.microshop.users.application.dto.SaasModuleDto;
import com.microshop.users.application.dto.SaasPlanDto;
import com.microshop.users.application.dto.CompanyProfileDto;
import com.microshop.users.infrastructure.persistence.entity.*;
import com.microshop.users.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SaasQueryService {

    private final SaasModuleRepository moduleRepository;
    private final SaasPlanRepository planRepository;
    private final SaasSubscriptionRepository subscriptionRepository;
    private final CompanyModuleRepository companyModuleRepository;
    private final CompanyRepository companyRepository;

    /**
     * Devuelve la lista de códigos de módulo habilitados para una empresa.
     * Lógica: módulos del plan UNION overrides por empresa (enabled=true).
     * Si no tiene suscripción, retorna todos los módulos como fallback para datos existentes.
     */
    public List<String> getEnabledModuleCodes(Long companyId) {
        if (companyId == null) return List.of();

        // Obtener módulos del plan vía suscripción
        List<String> planModules = subscriptionRepository.findEnabledModuleCodesByCompanyId(companyId);

        // Sin suscripción: retornar todos los módulos activos como fallback
        if (planModules.isEmpty()) {
            return moduleRepository.findAllByIsActiveTrueOrderBySortOrderAsc()
                    .stream().map(SaasModuleEntity::getCode).collect(Collectors.toList());
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
                        plan.getMaxUsers(), List.of()))
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
                company.getEmail(), company.getLogoUrl(), planCode, status, modules);
    }
}
