package com.microshop.rrhh.application.dto.attendance;
import lombok.Builder;

import com.microshop.rrhh.domain.model.Attendance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record AttendanceResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    String employeeName,
    LocalDate fecha,
    LocalTime horaEntrada,
    LocalTime horaSalida,
    BigDecimal horasTrabajadas,
    BigDecimal horasExtras,
    Attendance.AttendanceType tipoRegistro,
    String observaciones,
    String justificacion,
    String ubicacionEntrada,
    String ubicacionSalida,
    LocalDateTime createdAt
) {}
