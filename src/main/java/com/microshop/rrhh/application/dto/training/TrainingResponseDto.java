package com.microshop.rrhh.application.dto.training;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TrainingResponseDto(
    Long id,
    Long tenantId,
    String nombre,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String instructor,
    Integer duracionHoras,
    String estado,
    long participantes,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
