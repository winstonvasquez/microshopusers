package com.microshop.users.application.mapper;

import com.microshop.users.application.dto.SegmentoRequestDto;
import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.infrastructure.persistence.entity.SegmentoEntity;
import org.springframework.stereotype.Component;

@Component
public class SegmentoMapper {

    public SegmentoEntity toEntity(SegmentoRequestDto dto) {
        var entity = SegmentoEntity.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .color(dto.color())
                .tipoCliente(dto.tipoCliente())
                .build();
        entity.setActivo(dto.activo());
        return entity;
    }

    public SegmentoResponseDto toDto(SegmentoEntity entity) {
        return new SegmentoResponseDto(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getColor(),
                entity.getTipoCliente(),
                entity.getTotalClientes(),
                entity.isActivo(),
                entity.getFechaCreacion()
        );
    }

    public void updateEntity(SegmentoEntity entity, SegmentoRequestDto dto) {
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setColor(dto.color());
        entity.setTipoCliente(dto.tipoCliente());
        entity.setActivo(dto.activo());
    }
}
