package com.microshop.rrhh.application.dto.payroll;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PayrollRequestDto(
    @NotNull(message = "{payroll.employeeId.required}")
    Long employeeId,

    @NotBlank(message = "{payroll.periodo.required}")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "{payroll.periodo.pattern}")
    String periodo,

    @NotNull(message = "{payroll.sueldoBase.required}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{payroll.sueldoBase.min}")
    BigDecimal sueldoBase,

    @DecimalMin(value = "0.0", message = "{payroll.bonos.min}")
    BigDecimal bonos,

    @DecimalMin(value = "0.0", message = "{payroll.descuentos.min}")
    BigDecimal descuentos,

    @DecimalMin(value = "0.0", message = "{payroll.asignacionFamiliar.min}")
    BigDecimal asignacionFamiliar,

    @DecimalMin(value = "0.0", message = "{payroll.montoHorasExtras.min}")
    BigDecimal montoHorasExtras,

    @Min(value = 0, message = "{payroll.diasTrabajados.min}")
    Integer diasTrabajados
) {}
