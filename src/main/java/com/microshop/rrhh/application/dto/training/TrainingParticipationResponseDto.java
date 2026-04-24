package com.microshop.rrhh.application.dto.training;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record TrainingParticipationResponseDto(
    Long id,
    Long tenantId,
    Long trainingId,
    String trainingName,
    Long employeeId,
    String employeeName,
    LocalDate fechaInscripcion,
    String estado,
    BigDecimal asistenciaPorcentaje,
    BigDecimal notaFinal,
    Boolean aprobado,
    Boolean certificadoEmitido,
    String comentarios,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
