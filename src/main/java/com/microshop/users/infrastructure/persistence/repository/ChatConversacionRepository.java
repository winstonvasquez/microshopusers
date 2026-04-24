package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.ChatConversacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para conversaciones de chat soporte.
 */
@Repository
public interface ChatConversacionRepository extends JpaRepository<ChatConversacionEntity, Long> {

    /**
     * Obtiene todas las conversaciones de un cliente.
     */
    List<ChatConversacionEntity> findByClienteId(Long clienteId);

    /**
     * Obtiene las conversaciones de un cliente filtradas por estado.
     * Ejemplo: findByClienteIdAndEstado(clienteId, "ABIERTA")
     */
    List<ChatConversacionEntity> findByClienteIdAndEstado(Long clienteId, String estado);

    /**
     * Obtiene todas las conversaciones con un estado dado (para el panel admin).
     */
    List<ChatConversacionEntity> findByEstado(String estado);

    /**
     * Obtiene la primera conversación abierta de un cliente (conversación activa).
     */
    Optional<ChatConversacionEntity> findFirstByClienteIdAndEstadoOrderByCreatedAtDesc(Long clienteId, String estado);
}
