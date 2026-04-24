package com.microshop.rrhh.application.dto.attendance;

import com.microshop.rrhh.domain.model.Attendance;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRequestDto(
    @NotNull(message = "{attendance.employeeId.required}")
    Long employeeId,

    @NotNull(message = "{attendance.fecha.required}")
    LocalDate fecha,

    LocalTime horaEntrada,

    LocalTime horaSalida,

    @NotNull(message = "{attendance.tipoRegistro.required}")
    Attendance.AttendanceType tipoRegistro,

    @Size(max = 500, message = "{attendance.observaciones.size}")
    String observaciones,

    @Size(max = 500)
    String justificacion,

    @Size(max = 200)
    String ubicacionEntrada,

    @Size(max = 200)
    String ubicacionSalida
) {}
