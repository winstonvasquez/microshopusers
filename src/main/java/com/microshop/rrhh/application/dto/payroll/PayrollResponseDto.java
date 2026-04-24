package com.microshop.rrhh.application.dto.payroll;
import lombok.Builder;

import com.microshop.rrhh.domain.model.Payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PayrollResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    String employeeName,
    String periodo,
    BigDecimal sueldoBase,
    BigDecimal bonos,
    BigDecimal descuentos,
    String afpOnp,
    BigDecimal montoAfpOnp,
    BigDecimal essalud,
    BigDecimal rentaQuinta,
    BigDecimal cts,
    BigDecimal gratificacion,
    BigDecimal asignacionFamiliar,
    Integer diasTrabajados,
    BigDecimal horasExtras,
    BigDecimal montoHorasExtras,
    BigDecimal neto,
    Payroll.PayrollStatus estado,
    LocalDate fechaPago,
    Long pagoId,
    List<PayrollDetailDto> details,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
