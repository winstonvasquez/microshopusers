package com.microshop.rrhh.application.dto.evaluation;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvaluationRequestDto(
    @NotNull(message = "{evaluation.employeeId.required}")
    Long employeeId,

    @NotBlank(message = "{evaluation.periodo.required}")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "{evaluation.periodo.pattern}")
    String periodo,

    @NotNull(message = "{evaluation.evaluadorId.required}")
    Long evaluadorId,

    @NotNull(message = "{evaluation.puntaje.required}")
    @DecimalMin(value = "0.0", message = "{evaluation.puntaje.min}")
    @DecimalMax(value = "100.0", message = "{evaluation.puntaje.max}")
    BigDecimal puntaje,

    String tipoEvaluacion,

    String tipoRelacionEvaluador,

    @Size(max = 2000, message = "{evaluation.comentarios.size}")
    String comentarios,

    @Size(max = 1000, message = "{evaluation.fortalezas.size}")
    String fortalezas,

    @Size(max = 1000, message = "{evaluation.areasMejora.size}")
    String areasMejora,

    String planMejora,

    @NotNull(message = "{evaluation.fechaEvaluacion.required}")
    LocalDate fechaEvaluacion,

    LocalDate proximaRevision,

    List<EvaluationDetailRequestDto> details
) {}
