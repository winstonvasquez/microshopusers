package com.microshop.users.application.dto;

import java.time.Instant;

/**
 * Fila plana para el reporte de exportación de clientes/usuarios
 * (admin/reportes/reportes-clientes). Expone campos que
 * {@link UserResponseDto} no trae (activo, fecha de creación cruda,
 * nombres/apellidos sueltos) para que el export server-side pueda
 * generar exactamente las mismas columnas que arma hoy el frontend.
 */
public record UserExportRowDto(
        Long id,
        String username,
        String email,
        String nombres,
        String apellidos,
        boolean activo,
        Instant fechaCreacion) {
}
