package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.ThemeSeasonalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para temas estacionales.
 */
@Repository
public interface ThemeSeasonalRepository extends JpaRepository<ThemeSeasonalEntity, Long> {

    /**
     * Busca el primer tema estacional activo para una fecha dada (global o por tenant).
     *
     * @param date fecha a consultar
     * @return tema estacional activo, si existe
     */
    @Query("SELECT t FROM ThemeSeasonalEntity t " +
           "WHERE t.active = true " +
           "AND t.startDate <= :date " +
           "AND t.endDate >= :date " +
           "ORDER BY t.id ASC")
    List<ThemeSeasonalEntity> findActiveThemesForDate(@Param("date") LocalDate date);

    /**
     * Busca el primer tema estacional activo para una fecha, filtrando por tenant o global.
     *
     * @param date     fecha a consultar
     * @param tenantId ID del tenant
     * @return tema estacional activo, si existe
     */
    @Query("SELECT t FROM ThemeSeasonalEntity t " +
           "WHERE t.active = true " +
           "AND t.startDate <= :date " +
           "AND t.endDate >= :date " +
           "AND (t.tenantId = :tenantId OR t.tenantId IS NULL) " +
           "ORDER BY t.tenantId DESC NULLS LAST, t.id ASC")
    Optional<ThemeSeasonalEntity> findActiveThemeForDate(
            @Param("date") LocalDate date,
            @Param("tenantId") String tenantId);

    /** Obtiene todos los temas estacionales activos ordenados por fecha de inicio. */
    List<ThemeSeasonalEntity> findAllByActiveTrueOrderByStartDateAsc();
}
