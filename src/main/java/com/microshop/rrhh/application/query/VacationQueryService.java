package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.mapper.VacationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.infrastructure.persistence.repository.VacationRequestRepository;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VacationQueryService {

    private final VacationRequestRepository vacationRequestRepository;
    private final VacationMapper vacationMapper;
    private final TenantContext tenantContext;

    public Page<VacationResponseDto> getVacationsPaged(String search, VacationRequest.VacationStatus estado, Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        return vacationRequestRepository.searchPaged(tenantId, term, estado, pageable).map(vacationMapper::toDto);
    }

    public List<VacationResponseDto> getAllVacations() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantId(tenantId).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public List<VacationResponseDto> getByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public List<VacationResponseDto> getPending() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantIdAndEstado(tenantId, VacationRequest.VacationStatus.SOLICITADO).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public long countPending() {
        return vacationRequestRepository.countByTenantIdAndEstado(
                tenantContext.getCurrentTenantId(), VacationRequest.VacationStatus.SOLICITADO);
    }
}
