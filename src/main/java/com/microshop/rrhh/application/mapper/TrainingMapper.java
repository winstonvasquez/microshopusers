package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.training.TrainingRequestDto;
import com.microshop.rrhh.application.dto.training.TrainingResponseDto;
import com.microshop.rrhh.domain.model.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public Training toEntity(TrainingRequestDto dto, Long tenantId) {
        return Training.builder()
                .tenantId(tenantId)
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .instructor(dto.instructor())
                .duracionHoras(dto.duracionHoras())
                .estado(Training.TrainingStatus.PLANIFICADO)
                .build();
    }

    public void updateEntity(Training entity, TrainingRequestDto dto) {
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setFechaInicio(dto.fechaInicio());
        entity.setFechaFin(dto.fechaFin());
        entity.setInstructor(dto.instructor());
        entity.setDuracionHoras(dto.duracionHoras());
    }

    public TrainingResponseDto toDto(Training entity, long participantes) {
        return TrainingResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .instructor(entity.getInstructor())
                .duracionHoras(entity.getDuracionHoras())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .participantes(participantes)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
