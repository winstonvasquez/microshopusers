package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.payroll.PayrollDetailDto;
import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Payroll;
import com.microshop.rrhh.domain.model.PayrollDetail;
import com.microshop.users.shared.util.AppUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PayrollMapper {

    public Payroll toEntity(PayrollRequestDto dto, Long tenantId, Employee employee) {
        return Payroll.builder()
                .tenantId(tenantId)
                .employee(employee)
                .periodo(dto.periodo())
                .sueldoBase(dto.sueldoBase())
                .bonos(AppUtils.zeroIfNull(dto.bonos()))
                .descuentos(AppUtils.zeroIfNull(dto.descuentos()))
                .asignacionFamiliar(AppUtils.zeroIfNull(dto.asignacionFamiliar()))
                .montoHorasExtras(AppUtils.zeroIfNull(dto.montoHorasExtras()))
                .diasTrabajados(dto.diasTrabajados())
                .estado(Payroll.PayrollStatus.GENERADO)
                .build();
    }

    /**
     * Aplica al entity los campos manuales editables de una corrección (PUT /payroll/{id}).
     * NO toca employee/tenantId/estado/afpOnp/montoAfpOnp/essalud/rentaQuinta — esos los
     * resuelve PayrollCommandService (requieren lookup de tenant y el mismo cálculo
     * previsional que createPayroll/generatePayrollForPeriod, fuente única en
     * calcularAportesPrevisionales).
     */
    public void updateEntity(Payroll entity, PayrollRequestDto dto) {
        entity.setPeriodo(dto.periodo());
        entity.setSueldoBase(dto.sueldoBase());
        entity.setBonos(AppUtils.zeroIfNull(dto.bonos()));
        entity.setDescuentos(AppUtils.zeroIfNull(dto.descuentos()));
        entity.setAsignacionFamiliar(AppUtils.zeroIfNull(dto.asignacionFamiliar()));
        entity.setMontoHorasExtras(AppUtils.zeroIfNull(dto.montoHorasExtras()));
        entity.setDiasTrabajados(dto.diasTrabajados());
    }

    public PayrollResponseDto toDto(Payroll entity) {
        Employee emp = entity.getEmployee();
        List<PayrollDetailDto> details = entity.getDetails() != null
                ? entity.getDetails().stream().map(this::toDetailDto).toList()
                : List.of();

        return PayrollResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(AppUtils.idOrNull(emp, e -> e.getId()))
                .employeeName(emp != null ? AppUtils.fullName(emp.getNombres(), emp.getApellidos()) : null)
                .periodo(entity.getPeriodo())
                .sueldoBase(entity.getSueldoBase())
                .bonos(entity.getBonos())
                .descuentos(entity.getDescuentos())
                .afpOnp(entity.getAfpOnp())
                .montoAfpOnp(entity.getMontoAfpOnp())
                .essalud(entity.getEssalud())
                .rentaQuinta(entity.getRentaQuinta())
                .cts(entity.getCts())
                .gratificacion(entity.getGratificacion())
                .asignacionFamiliar(entity.getAsignacionFamiliar())
                .diasTrabajados(entity.getDiasTrabajados())
                .horasExtras(entity.getHorasExtras())
                .montoHorasExtras(entity.getMontoHorasExtras())
                .neto(entity.getNeto())
                .estado(entity.getEstado())
                .fechaPago(entity.getFechaPago())
                .pagoId(entity.getPagoId())
                .details(details)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PayrollDetailDto toDetailDto(PayrollDetail detail) {
        return PayrollDetailDto.builder()
                .id(detail.getId())
                .concepto(detail.getConcepto())
                .tipo(detail.getTipo())
                .monto(detail.getMonto())
                .cantidad(detail.getCantidad())
                .tasa(detail.getTasa())
                .build();
    }
}
