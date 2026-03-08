package com.microshop.users.infrastructure.web.dto;

/**
 * DTO de respuesta para roles
 */
public record RolResponseDto(
        Long id,
        String nombre,
        String descripcion) {
}
