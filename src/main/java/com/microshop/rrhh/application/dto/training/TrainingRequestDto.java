package com.microshop.rrhh.application.dto.training;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record TrainingRequestDto(
    @NotBlank(message = "{training.nombre.required}")
    @Size(max = 200, message = "{training.nombre.size}")
    String nombre,

    @Size(max = 1000, message = "{training.descripcion.size}")
    String descripcion,

    @NotNull(message = "{training.fechaInicio.required}")
    LocalDate fechaInicio,

    @NotNull(message = "{training.fechaFin.required}")
    LocalDate fechaFin,

    @Size(max = 200, message = "{training.instructor.size}")
    String instructor,

    @Min(value = 1, message = "{training.duracionHoras.min}")
    Integer duracionHoras
) {}
