package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.mapper.PayrollMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Payroll;
import com.microshop.rrhh.infrastructure.persistence.repository.PayrollRepository;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /**
     * Autoservicio "Mis Boletas": ver {@link #getByEmployee(Long)} con filtro opcional de estado
     * de boleta (GENERADO/APROBADO/PAGADO/CANCELADO).
     */
    public List<PayrollResponseDto> getByEmployee(Long employeeId, Payroll.PayrollStatus estado) {
        Long tenantId = tenantContext.getCurrentTenantId();
        List<Payroll> boletas = estado != null
                ? payrollRepository.findByTenantIdAndEmployee_IdAndEstado(tenantId, employeeId, estado)
                : payrollRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId);
        return boletas.stream()
                .map(payrollMapper::toDto)
                .toList();
    }

    public PayrollResponseDto getById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return payrollRepository.findByIdAndTenantId(id, tenantId)
                .map(payrollMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Planilla no encontrada"));
    }

    /**
     * Listado paginado de planillas con filtros avanzados: periodo, búsqueda por empleado, estado
     * de boleta, empleado, departamento, sistema previsional/AFP y rango de fecha de pago.
     */
    public Page<PayrollResponseDto> getPayrollPaged(String periodo, String search, Payroll.PayrollStatus estado,
            Long employeeId, Long departmentId, String afpOnp, LocalDate fechaPagoDesde, LocalDate fechaPagoHasta,
            Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        String afpTerm = AppUtils.searchTermOrNull(afpOnp);
        String periodoTerm = AppUtils.searchTermOrNull(periodo);
        return payrollRepository.searchPaged(tenantId, periodoTerm, term, estado, employeeId, departmentId,
                afpTerm, fechaPagoDesde, fechaPagoHasta, pageable).map(payrollMapper::toDto);
    }
}
