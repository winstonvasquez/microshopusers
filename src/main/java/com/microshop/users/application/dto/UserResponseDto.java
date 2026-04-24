package com.microshop.users.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para usuarios
 */
public record UserResponseDto(
        Long id,
        String username,
        String email,
        RolDto rol,
        PersonaDto persona,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public record RolDto(
            Long id,
            String nombre,
            String descripcion) {
    }

    public record PersonaDto(
            Long id,
            String nombres,
            String apellidos,
            String nombreCompleto,
            String tipoDocumento,
            String numeroDocumento,
            LocalDate fechaNacimiento) {
    }
}
