package com.microshop.rrhh.application.dto.evaluation;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EvaluationCriteriaResponseDto(
    Long id,
    Long tenantId,
    String nombre,
    String descripcion,
    BigDecimal pesoPorcentaje,
    BigDecimal puntajeMinimo,
    BigDecimal puntajeMaximo,
    Boolean activo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
