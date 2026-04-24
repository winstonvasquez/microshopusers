package com.microshop.rrhh.application.dto.evaluation;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequestDto(
    @NotNull(message = "El ID del empleado es obligatorio")
    Long employeeId,

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    String titulo,

    String descripcion,

    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,

    @NotNull(message = "La fecha de fin es obligatoria")
    LocalDate fechaFin,

    String prioridad,

    Long asignadoPorId,

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    BigDecimal porcentajeAvance
) {}
