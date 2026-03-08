package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyCommandService {

    private final CompanyRepository companyRepository;
    private final MessageSource messageSource;

    public CompanyEntity createCompany(CompanyEntity company) {
        if (companyRepository.existsByRuc(company.getRuc())) {
            throw new IllegalArgumentException(messageSource.getMessage("company.ruc.exists",
                    new Object[] { company.getRuc() }, LocaleContextHolder.getLocale()));
        }
        return companyRepository.save(company);
    }

    public CompanyEntity updateCompany(Long id, CompanyEntity companyDetails) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for this id :: " + id));

        company.setName(companyDetails.getName());
        company.setRuc(companyDetails.getRuc());
        company.setActive(companyDetails.isActive());

        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for this id :: " + id));
        company.setActive(false); // Soft delete
        companyRepository.save(company);
    }
}
