package com.microshop.rrhh.application.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckInOutDto(
    @NotNull(message = "{attendance.employeeId.required}")
    Long employeeId,

    @Size(max = 200)
    String ubicacion
) {}
