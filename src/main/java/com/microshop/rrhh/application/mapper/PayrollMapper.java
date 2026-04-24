package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.payroll.PayrollDetailDto;
import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Payroll;
import com.microshop.rrhh.domain.model.PayrollDetail;
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
                .bonos(dto.bonos() != null ? dto.bonos() : BigDecimal.ZERO)
                .descuentos(dto.descuentos() != null ? dto.descuentos() : BigDecimal.ZERO)
                .estado(Payroll.PayrollStatus.GENERADO)
                .build();
    }

    public PayrollResponseDto toDto(Payroll entity) {
        Employee emp = entity.getEmployee();
        List<PayrollDetailDto> details = entity.getDetails() != null
                ? entity.getDetails().stream().map(this::toDetailDto).toList()
                : List.of();

        return PayrollResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeName(emp != null ? emp.getNombres() + " " + emp.getApellidos() : null)
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
