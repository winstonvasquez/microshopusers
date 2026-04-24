package com.microshop.rrhh.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.LeaveBalance;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.LeaveBalanceRepository;
import com.microshop.users.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceCommandService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    /**
     * Generate annual leave balance for all active employees.
     * Peru law: 30 calendar days per year after 1 year of service.
     */
    public int generateAnnualBalance(int year) {
        Long tenantId = tenantContext.getCurrentTenantId();
        List<Employee> activeEmployees = employeeRepository.findByTenantIdAndEstado(
                tenantId, Employee.EmployeeStatus.ACTIVO);

        int generated = 0;
        for (Employee emp : activeEmployees) {
            var existing = leaveBalanceRepository.findByTenantIdAndEmployeeIdAndAnio(tenantId, emp.getId(), year);
            if (existing.isPresent()) continue;

            LeaveBalance balance = LeaveBalance.builder()
                    .tenantId(tenantId)
                    .employee(emp)
                    .anio(year)
                    .diasGanados(new BigDecimal("30"))
                    .diasUsados(BigDecimal.ZERO)
                    .diasDisponibles(new BigDecimal("30"))
                    .diasVencidos(BigDecimal.ZERO)
                    .build();

            leaveBalanceRepository.save(balance);
            generated++;
        }

        log.info("Balance vacacional generado para {} empleados - Año: {} - Tenant: {}",
                generated, year, tenantId);
        return generated;
    }

    /**
     * Decrement balance when vacation is approved.
     */
    public void decrementBalance(Long employeeId, int days, int year) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LeaveBalance balance = leaveBalanceRepository.findByTenantIdAndEmployeeIdAndAnio(tenantId, employeeId, year)
                .orElseThrow(() -> new ConflictException(msg.get("vacation.no.balance")));

        BigDecimal daysDecimal = new BigDecimal(days);
        if (balance.getDiasDisponibles().compareTo(daysDecimal) < 0) {
            throw new ConflictException(msg.get("vacation.insufficient.balance"));
        }

        balance.setDiasUsados(balance.getDiasUsados().add(daysDecimal));
        // diasDisponibles is updated via @PreUpdate
        leaveBalanceRepository.save(balance);
    }
}
