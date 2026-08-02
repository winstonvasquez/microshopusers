package com.microshop.rrhh.application.dto.vacation;

import com.microshop.rrhh.domain.model.VacationRequest;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record VacationRequestDto(
    @NotNull(message = "{vacation.employeeId.required}")
    Long employeeId,

    @NotNull(message = "{vacation.fechaInicio.required}")
    @FutureOrPresent(message = "{vacation.fechaInicio.futureOrPresent}")
    LocalDate fechaInicio,

    @NotNull(message = "{vacation.fechaFin.required}")
    LocalDate fechaFin,

    @NotNull(message = "{vacation.dias.required}")
    @Min(value = 1, message = "{vacation.dias.min}")
    Integer dias,

    // El valor viaja como HTML del editor de texto enriquecido del frontend.
    @Size(max = 4000, message = "{vacation.motivo.size}")
    String motivo,

    // Opcional: si el cliente no lo envía, el mapper aplica el default ANUAL del dominio.
    VacationRequest.VacationType tipoVacacion
) {}
