package com.microshop.rrhh.application.dto.training;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TrainingParticipationRequestDto(
    @NotNull(message = "{training.participation.trainingId.required}")
    Long trainingId,

    @NotNull(message = "{training.participation.employeeId.required}")
    Long employeeId,

    @DecimalMin(value = "0.0", message = "{training.participation.asistencia.min}")
    @DecimalMax(value = "100.0", message = "{training.participation.asistencia.max}")
    BigDecimal asistenciaPorcentaje,

    @DecimalMin(value = "0.0", message = "{training.participation.nota.min}")
    @DecimalMax(value = "100.0", message = "{training.participation.nota.max}")
    BigDecimal notaFinal,

    Boolean aprobado,

    @Size(max = 500, message = "{training.participation.comentarios.size}")
    String comentarios
) {}
