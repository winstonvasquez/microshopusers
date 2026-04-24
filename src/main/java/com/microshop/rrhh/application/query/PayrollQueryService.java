package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.mapper.PayrollMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.infrastructure.persistence.repository.PayrollRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PayrollQueryService {

    private final PayrollRepository payrollRepository;
    private final PayrollMapper payrollMapper;
    private final TenantContext tenantContext;

    public List<PayrollResponseDto> getByPeriod(String periodo) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return payrollRepository.findByTenantIdAndPeriodo(tenantId, periodo).stream()
                .map(payrollMapper::toDto)
                .toList();
    }

    public List<PayrollResponseDto> getByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return payrollRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId).stream()
                .map(payrollMapper::toDto)
                .toList();
    }

    public PayrollResponseDto getById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return payrollRepository.findByIdAndTenantId(id, tenantId)
                .map(payrollMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Planilla no encontrada"));
    }
}
