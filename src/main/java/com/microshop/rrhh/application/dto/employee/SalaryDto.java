package com.microshop.rrhh.application.dto.employee;

import com.microshop.rrhh.domain.model.Salary;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class SalaryDto {

    private SalaryDto() {}

    @Builder
    public record Request(
        @NotNull
        LocalDate fechaInicio,

        LocalDate fechaFin,

        @NotNull @Positive
        BigDecimal salarioBase,

        String moneda,

        Salary.SalaryChangeReason motivo,

        BigDecimal porcentajeIncremento
    ) {}

    @Builder
    public record Response(
        Long id,
        Long employeeId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal salarioBase,
        String moneda,
        Salary.SalaryChangeReason motivo,
        BigDecimal porcentajeIncremento,
        Long aprobadoPorId,
        String aprobadoPorName,
        LocalDateTime createdAt
    ) {}
}
