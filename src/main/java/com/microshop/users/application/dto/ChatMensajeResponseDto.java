package com.microshop.users.application.dto;

import java.time.Instant;

/**
 * DTO de respuesta para un mensaje de chat de soporte.
 */
public record ChatMensajeResponseDto(
        Long id,
        Long conversacionId,
        Long emisorId,
        String emisorTipo,
        String contenido,
        Instant timestamp,
        boolean leido) {
}
