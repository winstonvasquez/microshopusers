package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.vacation.LeaveBalanceDto;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.LeaveBalance;
import com.microshop.rrhh.infrastructure.persistence.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeaveBalanceQueryService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final TenantContext tenantContext;

    public Optional<LeaveBalanceDto> getBalance(Long employeeId, int year) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return leaveBalanceRepository.findByTenantIdAndEmployeeIdAndAnio(tenantId, employeeId, year)
                .map(this::toDto);
    }

    public List<LeaveBalanceDto> getBalancesByYear(int year) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return leaveBalanceRepository.findByTenantIdAndAnio(tenantId, year).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LeaveBalanceDto> getBalanceHistory(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return leaveBalanceRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(this::toDto)
                .toList();
    }

    private LeaveBalanceDto toDto(LeaveBalance lb) {
        Employee emp = lb.getEmployee();
        return LeaveBalanceDto.builder()
                .id(lb.getId())
                .employeeId(emp.getId())
                .employeeName(emp.getNombres() + " " + emp.getApellidos())
                .anio(lb.getAnio())
                .diasGanados(lb.getDiasGanados())
                .diasUsados(lb.getDiasUsados())
                .diasDisponibles(lb.getDiasDisponibles())
                .diasVencidos(lb.getDiasVencidos())
                .build();
    }
}
