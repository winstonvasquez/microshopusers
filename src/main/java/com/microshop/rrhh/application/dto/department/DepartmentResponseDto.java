package com.microshop.rrhh.application.dto.department;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DepartmentResponseDto(
    Long id,
    Long tenantId,
    String codigo,
    String nombre,
    String descripcion,
    Long managerId,
    String managerName,
    Long parentId,
    String parentName,
    Boolean activo,
    int employeeCount,
    int positionCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<DepartmentResponseDto> subDepartments
) {}
