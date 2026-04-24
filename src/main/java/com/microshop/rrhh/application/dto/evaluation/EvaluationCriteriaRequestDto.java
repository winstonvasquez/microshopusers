package com.microshop.rrhh.application.dto.evaluation;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record EvaluationCriteriaRequestDto(
    @NotBlank(message = "El nombre del criterio es obligatorio")
    @Size(max = 100)
    String nombre,

    @Size(max = 500)
    String descripcion,

    @NotNull(message = "El peso porcentual es obligatorio")
    @DecimalMin("0.01")
    @DecimalMax("100.0")
    BigDecimal pesoPorcentaje,

    BigDecimal puntajeMinimo,

    BigDecimal puntajeMaximo
) {}
