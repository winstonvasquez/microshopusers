package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.CompanyModuleEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasModuleEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.CompanyModuleRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyCommandService {

    private final CompanyRepository companyRepository;
    private final CompanyModuleRepository companyModuleRepository;
    private final SaasModuleRepository saasModuleRepository;
    private final MessageHelper msg;

    public CompanyEntity createCompany(CompanyEntity company) {
        if (companyRepository.existsByRuc(company.getRuc())) {
            throw new IllegalArgumentException(msg.get("company.ruc.exists", company.getRuc()));
        }
        return companyRepository.save(company);
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
        company.setLogoUrl(companyDetails.getLogoUrl());
        company.setDomain(companyDetails.getDomain());

        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("company.not.found.with.id", id)));
        company.setActive(false);
        companyRepository.save(company);
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
