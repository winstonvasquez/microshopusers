package com.microshop.users.application.mapper;

import com.microshop.users.application.dto.ChatConversacionResponseDto;
import com.microshop.users.application.dto.ChatMensajeResponseDto;
import com.microshop.users.infrastructure.persistence.entity.ChatConversacionEntity;
import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapea entidades JPA de chat a DTOs de respuesta.
 */
@Component
public class ChatMapper {

    public ChatConversacionResponseDto toDto(ChatConversacionEntity entity) {
        return new ChatConversacionResponseDto(
                entity.getId(),
                entity.getClienteId(),
                entity.getAsunto(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getLastMessageAt());
    }

    public ChatMensajeResponseDto toDto(ChatMensajeEntity entity) {
        return new ChatMensajeResponseDto(
                entity.getId(),
                entity.getConversacionId(),
                entity.getEmisorId(),
                entity.getEmisorTipo(),
                entity.getContenido(),
                entity.getTimestamp(),
                entity.isLeido());
    }
}
