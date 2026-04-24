package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.department.DepartmentRequestDto;
import com.microshop.rrhh.application.dto.department.DepartmentResponseDto;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Employee;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequestDto dto, Long tenantId) {
        return Department.builder()
                .tenantId(tenantId)
                .codigo(dto.codigo())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .activo(true)
                .build();
    }

    public void updateEntity(Department entity, DepartmentRequestDto dto) {
        entity.setCodigo(dto.codigo());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
    }

    public DepartmentResponseDto toDto(Department entity) {
        return toDto(entity, false);
    }

    public DepartmentResponseDto toDto(Department entity, boolean includeChildren) {
        Employee manager = entity.getManager();
        Department parent = entity.getParent();

        List<DepartmentResponseDto> children = Collections.emptyList();
        if (includeChildren && entity.getSubDepartments() != null) {
            children = entity.getSubDepartments().stream()
                    .map(child -> toDto(child, true))
                    .toList();
        }

        return DepartmentResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null ? manager.getNombres() + " " + manager.getApellidos() : null)
                .parentId(parent != null ? parent.getId() : null)
                .parentName(parent != null ? parent.getNombre() : null)
                .activo(entity.getActivo())
                .employeeCount(entity.getEmployees() != null ? entity.getEmployees().size() : 0)
                .positionCount(entity.getPositions() != null ? entity.getPositions().size() : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subDepartments(children)
                .build();
    }
}
