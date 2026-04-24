package com.microshop.rrhh.application.dto.vacation;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LeaveBalanceDto(
    Long id,
    Long employeeId,
    String employeeName,
    Integer anio,
    BigDecimal diasGanados,
    BigDecimal diasUsados,
    BigDecimal diasDisponibles,
    BigDecimal diasVencidos
) {}
