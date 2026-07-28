package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByUsuarioIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUsuarioIdAndReadFalse(Long userId);

    /**
     * Listado paginado con filtros avanzados (2026-07-27): tipo, leída/no leída, rango de fecha
     * y búsqueda por título/cuerpo, todos opcionales. Reemplaza a
     * findByUsuarioIdOrderByCreatedAtDesc cuando el frontend envía algún filtro (el Pageable
     * trae el orden, ver NotificationController).
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.usuario.id = :userId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(n.title) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(n.body) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:type AS String) IS NULL OR CAST(:type AS String) = '' OR n.type = :type) " +
           "AND (CAST(:read AS Boolean) IS NULL OR n.read = :read) " +
           "AND (CAST(:createdAtDesde AS Instant) IS NULL OR n.createdAt >= :createdAtDesde) " +
           "AND (CAST(:createdAtHasta AS Instant) IS NULL OR n.createdAt <= :createdAtHasta)")
    Page<NotificationEntity> searchPaged(@Param("userId") Long userId,
                                        @Param("search") String search,
                                        @Param("type") String type,
                                        @Param("read") Boolean read,
                                        @Param("createdAtDesde") Instant createdAtDesde,
                                        @Param("createdAtHasta") Instant createdAtHasta,
                                        Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.usuario.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.usuario.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);
}
