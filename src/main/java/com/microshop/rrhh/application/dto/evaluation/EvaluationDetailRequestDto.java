package com.microshop.rrhh.application.dto.evaluation;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record EvaluationDetailRequestDto(
    @NotNull
    Long criteriaId,

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    BigDecimal puntaje,

    @Size(max = 1000)
    String comentarios
) {}
