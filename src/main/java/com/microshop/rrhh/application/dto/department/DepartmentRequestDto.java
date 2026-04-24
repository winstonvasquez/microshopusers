package com.microshop.rrhh.application.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DepartmentRequestDto(
    @NotBlank(message = "{department.codigo.required}")
    @Size(max = 20, message = "{department.codigo.size}")
    String codigo,

    @NotBlank(message = "{department.nombre.required}")
    @Size(max = 100, message = "{department.nombre.size}")
    String nombre,

    @Size(max = 500, message = "{department.descripcion.size}")
    String descripcion,

    Long managerId,

    Long parentId
) {}
