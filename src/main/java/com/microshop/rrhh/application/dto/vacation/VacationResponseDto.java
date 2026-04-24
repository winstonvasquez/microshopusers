package com.microshop.rrhh.application.dto.vacation;
import lombok.Builder;

import com.microshop.rrhh.domain.model.VacationRequest;

import java.time.LocalDate;

@Builder
public record VacationResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Integer dias,
    VacationRequest.VacationStatus estado,
    String motivo,
    Long aprobadoPor,
    LocalDate fechaAprobacion,
    String comentariosAprobacion,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
