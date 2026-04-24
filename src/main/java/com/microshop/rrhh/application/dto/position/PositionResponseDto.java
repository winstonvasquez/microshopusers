package com.microshop.rrhh.application.dto.position;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PositionResponseDto(
    Long id,
    Long tenantId,
    String codigo,
    String nombre,
    String descripcion,
    Long departmentId,
    String departmentName,
    String nivel,
    BigDecimal salarioMinimo,
    BigDecimal salarioMaximo,
    String requisitos,
    Boolean activo,
    int employeeCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
