package com.microshop.users.application.query;

import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasSubscriptionRepository;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyRoleEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasSubscriptionEntity;
import com.microshop.users.infrastructure.persistence.entity.SaasPlanEntity;
import com.microshop.users.application.dto.CompanyResponseDto;
import com.microshop.users.application.dto.CompanyUserDto;
import com.microshop.users.application.dto.CompanySubscriptionDto;
import com.microshop.users.application.mapper.CompanyMapper;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CompanyQueryService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserCompanyRepository userCompanyRepository;
    private final SaasSubscriptionRepository subscriptionRepository;

    public List<CompanyResponseDto> findAll() {
        return companyRepository.findAllProjected();
    }

    /**
     * Ver {@link #findAll()} pero acotado a UNA empresa cuando {@code companyId} es no-nulo.
     * Defensa cross-tenant: un ADMIN normal solo debe ver su propia empresa, a diferencia
     * de un SUPERADMIN que llama con {@code companyId=null}.
     */
    public List<CompanyResponseDto> findAll(Long companyId) {
        if (companyId == null) return findAll();
        return companyRepository.findProjectedById(companyId).map(List::of).orElseGet(List::of);
    }

    public Page<CompanyResponseDto> findPaged(String search, Boolean active, Instant fechaCreacionDesde,
            Instant fechaCreacionHasta, Pageable pageable) {
        String term = AppUtils.searchTermOrNull(search);
        return companyRepository.searchPaged(term, active, fechaCreacionDesde, fechaCreacionHasta, pageable);
    }

    /** Ver {@link #findPaged(String, Boolean, Instant, Instant, Pageable)}, acotado a UNA empresa cuando {@code companyId} es no-nulo. */
    public Page<CompanyResponseDto> findPaged(String search, Boolean active, Instant fechaCreacionDesde,
            Instant fechaCreacionHasta, Pageable pageable, Long companyId) {
        String term = AppUtils.searchTermOrNull(search);
        return companyRepository.searchPagedScoped(term, active, fechaCreacionDesde, fechaCreacionHasta, companyId, pageable);
    }

    public Optional<CompanyResponseDto> findById(@NonNull Long id) {
        return companyRepository.findProjectedById(id);
    }

    public Optional<CompanyResponseDto> findFullById(@NonNull Long id) {
        return companyRepository.findById(id).map(companyMapper::toDto);
    }

    public List<CompanyUserDto> findUsersByCompanyId(Long companyId) {
        return userCompanyRepository.findByCompanyId(companyId).stream()
                .map(uc -> new CompanyUserDto(
                        uc.getUsuario().getId(),
                        uc.getUsuario().getUsername(),
                        uc.getUsuario().getUsername(),
                        uc.getUsuario().getEmail(),
                        uc.getRoles().stream()
                                .map(r -> r.getRol().getNombre())
                                .toList(),
                        uc.isActive(),
                        uc.getFechaCreacion()
                ))
                .toList();
    }

    public Optional<CompanySubscriptionDto> findSubscriptionByCompanyId(Long companyId) {
        return subscriptionRepository.findByCompanyId(companyId)
                .map(sub -> {
                    SaasPlanEntity plan = sub.getPlan();
                    return new CompanySubscriptionDto(
                            sub.getId(),
                            plan.getCode(),
                            plan.getName(),
                            plan.getDescription(),
                            sub.getStatus(),
                            sub.getStartsAt(),
                            sub.getEndsAt(),
                            sub.getTrialEndsAt(),
                            plan.getPriceMonthly(),
                            plan.getPriceAnnual(),
                            plan.getMaxUsers()
                    );
                });
    }
}
