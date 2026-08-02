package com.microshop.rrhh.application.dto.vacation;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
public record VacationApprovalDto(
    @NotNull(message = "{vacation.approval.approved.required}")
    Boolean approved,

    // El valor viaja como HTML del editor de texto enriquecido del frontend.
    @Size(max = 4000, message = "{vacation.approval.comentarios.size}")
    String comentarios
) {}
