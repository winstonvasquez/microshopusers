package com.microshop.rrhh.application.dto.evaluation;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record EvaluationDetailResponseDto(
    Long id,
    Long criteriaId,
    String criteriaName,
    BigDecimal pesoPorcentaje,
    BigDecimal puntaje,
    String comentarios
) {}
