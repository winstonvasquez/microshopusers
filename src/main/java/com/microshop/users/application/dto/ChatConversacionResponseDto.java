package com.microshop.users.application.dto;

import java.time.Instant;

/**
 * DTO de respuesta para una conversación de chat de soporte.
 */
public record ChatConversacionResponseDto(
        Long id,
        Long clienteId,
        String asunto,
        String estado,
        Instant createdAt,
        Instant lastMessageAt) {
}
