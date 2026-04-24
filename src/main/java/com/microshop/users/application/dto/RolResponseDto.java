package com.microshop.users.application.dto;

/**
 * DTO de respuesta para roles
 */
public record RolResponseDto(
        Long id,
        String nombre,
        String descripcion) {
}
