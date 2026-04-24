package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.ChatMensajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio JPA para mensajes de chat soporte.
 */
@Repository
public interface ChatMensajeRepository extends JpaRepository<ChatMensajeEntity, Long> {

    /**
     * Obtiene todos los mensajes de una conversación ordenados cronológicamente.
     */
    List<ChatMensajeEntity> findByConversacionIdOrderByTimestampAsc(Long conversacionId);

    /**
     * Obtiene mensajes de una conversación posteriores a un instante dado (para polling).
     */
    List<ChatMensajeEntity> findByConversacionIdAndTimestampAfterOrderByTimestampAsc(
            Long conversacionId, Instant since);

    /**
     * Cuenta mensajes no leídos del SOPORTE en una conversación (para el badge del cliente).
     */
    long countByConversacionIdAndLeidoFalseAndEmisorTipo(Long conversacionId, String emisorTipo);
}
