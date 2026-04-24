package com.microshop.users.application.dto;

import java.time.Instant;

/**
 * DTO de respuesta para segmentos de clientes.
 */
public record SegmentoResponseDto(
        Long id,
        String nombre,
        String descripcion,
        String color,
        String tipoCliente,
        Integer totalClientes,
        boolean activo,
        Instant fechaCreacion
) {}
