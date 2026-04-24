package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.application.mapper.ContractMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.infrastructure.persistence.repository.ContractRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContractQueryService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final TenantContext tenantContext;
    private final MessageSource messageSource;

    public List<ContractResponseDto> getAllContracts() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return contractRepository.findByTenantId(tenantId).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    public ContractResponseDto getContractById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Contract contract = contractRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("contract.not.found", null, Locale.getDefault())));
        return contractMapper.toDto(contract);
    }

    public List<ContractResponseDto> getContractsByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return contractRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    public List<ContractResponseDto> getContractsByStatus(Contract.ContractStatus status) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return contractRepository.findByTenantIdAndEstado(tenantId, status).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    public List<ContractResponseDto> getExpiringContracts(int days) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate targetDate = LocalDate.now().plusDays(days);
        return contractRepository.findExpiringBefore(tenantId, targetDate).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    public long countContracts() {
        return contractRepository.countByTenantId(tenantContext.getCurrentTenantId());
    }

    public long countActiveContracts() {
        return contractRepository.countByTenantIdAndEstado(
                tenantContext.getCurrentTenantId(), Contract.ContractStatus.ACTIVO);
    }
}
