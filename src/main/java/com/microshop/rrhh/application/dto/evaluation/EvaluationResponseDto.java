package com.microshop.rrhh.application.dto.evaluation;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EvaluationResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    String employeeName,
    String periodo,
    Long evaluadorId,
    String evaluadorName,
    String tipoEvaluacion,
    String tipoRelacionEvaluador,
    BigDecimal puntaje,
    String comentarios,
    String fortalezas,
    String areasMejora,
    String planMejora,
    String estado,
    LocalDate fechaEvaluacion,
    LocalDate proximaRevision,
    List<EvaluationDetailResponseDto> details,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
