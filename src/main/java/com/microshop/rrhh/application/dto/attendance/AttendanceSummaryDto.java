package com.microshop.rrhh.application.dto.attendance;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AttendanceSummaryDto(
    Long employeeId,
    String employeeName,
    int diasTrabajados,
    int tardanzas,
    int faltas,
    int permisos,
    BigDecimal totalHorasTrabajadas,
    BigDecimal totalHorasExtras
) {}
