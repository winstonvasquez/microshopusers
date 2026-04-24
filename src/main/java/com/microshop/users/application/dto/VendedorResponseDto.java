package com.microshop.users.application.dto;

import lombok.Builder;

@Builder
public record VendedorResponseDto(
        Long id,
        String dniRuc,
        String telefonoContacto,
        String estadoAprobacion,
        Long usuarioId,
        String username,
        String email) {
}
