package com.microshop.users.application.mapper;

import com.microshop.users.application.dto.VendedorResponseDto;
import com.microshop.users.infrastructure.persistence.entity.VendedorEntity;
import org.springframework.stereotype.Component;

/**
 * Extraído de {@code VendedorCommandService} y {@code VendedorQueryService}, que tenían este
 * mapeo duplicado byte a byte. Sin cambios de comportamiento.
 */
@Component
public class VendedorMapper {

    public VendedorResponseDto mapToDto(VendedorEntity entity) {
        return VendedorResponseDto.builder()
                .id(entity.getId())
                .dniRuc(entity.getDniRuc())
                .telefonoContacto(entity.getTelefonoContacto())
                .estadoAprobacion(entity.getEstadoAprobacion())
                .usuarioId(entity.getUsuario().getId())
                .username(entity.getUsuario().getUsername())
                .email(entity.getUsuario().getEmail())
                .build();
    }
}
