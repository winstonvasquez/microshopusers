package com.microshop.rrhh.application.dto.vacation;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
public record VacationApprovalDto(
    @NotNull(message = "{vacation.approval.approved.required}")
    Boolean approved,

    @Size(max = 500, message = "{vacation.approval.comentarios.size}")
    String comentarios
) {}
