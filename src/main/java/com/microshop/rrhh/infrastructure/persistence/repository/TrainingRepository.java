package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByTenantId(Long tenantId);

    Optional<Training> findByIdAndTenantId(Long id, Long tenantId);

    List<Training> findByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);

    long countByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);

    // Analytics dashboard (2026-07-22): SUM agregado en SQL, COALESCE evita null si no hay filas.
    @Query("SELECT COALESCE(SUM(t.duracionHoras), 0) FROM Training t WHERE t.tenantId = :tenantId AND t.estado = :estado")
    long sumDuracionHorasByTenantIdAndEstado(@Param("tenantId") Long tenantId, @Param("estado") Training.TrainingStatus estado);

    // Listado paginado server-side con filtro de estado + rango de fecha de inicio opcionales.
    @Query("SELECT t FROM Training t WHERE t.tenantId = :tenantId " +
           "AND (:estado IS NULL OR t.estado = :estado) " +
           "AND (:fechaInicioDesde IS NULL OR t.fechaInicio >= :fechaInicioDesde) " +
           "AND (:fechaInicioHasta IS NULL OR t.fechaInicio <= :fechaInicioHasta)")
    Page<Training> searchPaged(@Param("tenantId") Long tenantId,
                                @Param("estado") Training.TrainingStatus estado,
                                @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
                                @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
                                Pageable pageable);
}
