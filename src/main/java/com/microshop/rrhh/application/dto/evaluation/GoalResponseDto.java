package com.microshop.rrhh.application.dto.evaluation;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record GoalResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    String employeeName,
    String titulo,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String estado,
    BigDecimal porcentajeAvance,
    String prioridad,
    Long asignadoPorId,
    String asignadoPorName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
