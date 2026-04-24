package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.position.PositionRequestDto;
import com.microshop.rrhh.application.dto.position.PositionResponseDto;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public Position toEntity(PositionRequestDto dto, Long tenantId) {
        return Position.builder()
                .tenantId(tenantId)
                .codigo(dto.codigo())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .nivel(dto.nivel())
                .salarioMinimo(dto.salarioMinimo())
                .salarioMaximo(dto.salarioMaximo())
                .requisitos(dto.requisitos())
                .activo(true)
                .build();
    }

    public void updateEntity(Position entity, PositionRequestDto dto) {
        entity.setCodigo(dto.codigo());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setNivel(dto.nivel());
        entity.setSalarioMinimo(dto.salarioMinimo());
        entity.setSalarioMaximo(dto.salarioMaximo());
        entity.setRequisitos(dto.requisitos());
    }

    public PositionResponseDto toDto(Position entity) {
        Department dept = entity.getDepartment();

        return PositionResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .departmentId(dept != null ? dept.getId() : null)
                .departmentName(dept != null ? dept.getNombre() : null)
                .nivel(entity.getNivel())
                .salarioMinimo(entity.getSalarioMinimo())
                .salarioMaximo(entity.getSalarioMaximo())
                .requisitos(entity.getRequisitos())
                .activo(entity.getActivo())
                .employeeCount(entity.getEmployees() != null ? entity.getEmployees().size() : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
