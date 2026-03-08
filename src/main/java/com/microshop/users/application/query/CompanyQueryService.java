package com.microshop.users.application.query;

import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.web.dto.CompanyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CompanyQueryService {

    private final CompanyRepository companyRepository;

    public List<com.microshop.users.infrastructure.web.dto.CompanyResponseDto> findAll() {
        return companyRepository.findAllProjected();
    }

    public Optional<CompanyResponseDto> findById(@NonNull Long id) {
        return companyRepository.findProjectedById(id);
    }
}
