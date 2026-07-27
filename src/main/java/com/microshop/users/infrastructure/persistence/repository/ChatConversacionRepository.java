package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.ChatConversacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    /**
     * Listado paginado para el panel de soporte con filtros avanzados (2026-07-27): antes
     * listarConversacionesAdmin() hardcodeaba estado='ABIERTA' y devolvía List sin paginar
     * (findByEstado). Ahora acepta búsqueda por asunto, estado, cliente y rango de fecha de
     * apertura / último mensaje, todos opcionales. Patrón (:x IS NULL OR :x = '' OR ...) para
     * Strings, igual que EmployeeRepository/VacationRequestRepository de este mismo servicio.
     */
    @Query("SELECT c FROM ChatConversacionEntity c WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(c.asunto) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR :estado = '' OR c.estado = :estado) " +
           "AND (:clienteId IS NULL OR c.clienteId = :clienteId) " +
           "AND (:createdAtDesde IS NULL OR c.createdAt >= :createdAtDesde) " +
           "AND (:createdAtHasta IS NULL OR c.createdAt <= :createdAtHasta) " +
           "AND (:lastMessageAtDesde IS NULL OR c.lastMessageAt >= :lastMessageAtDesde) " +
           "AND (:lastMessageAtHasta IS NULL OR c.lastMessageAt <= :lastMessageAtHasta)")
    Page<ChatConversacionEntity> searchAdminPaged(@Param("search") String search,
                                                  @Param("estado") String estado,
                                                  @Param("clienteId") Long clienteId,
                                                  @Param("createdAtDesde") Instant createdAtDesde,
                                                  @Param("createdAtHasta") Instant createdAtHasta,
                                                  @Param("lastMessageAtDesde") Instant lastMessageAtDesde,
                                                  @Param("lastMessageAtHasta") Instant lastMessageAtHasta,
                                                  Pageable pageable);
}
