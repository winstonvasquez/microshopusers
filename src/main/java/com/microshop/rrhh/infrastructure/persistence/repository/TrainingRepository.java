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

    // Listado paginado server-side: estado + rango fechaInicio/fechaFin + búsqueda por texto + instructor.
    // ':search = ''' en vez de 'IS NULL' para el bind String (mismo gotcha documentado en microshopventas).
    @Query("SELECT t FROM Training t WHERE t.tenantId = :tenantId " +
           "AND (:estado IS NULL OR t.estado = :estado) " +
           "AND (CAST(:instructor AS String) = '' OR t.instructor = :instructor) " +
           "AND (CAST(:search AS String) = '' " +
           "     OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(t.instructor) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:fechaInicioDesde AS LocalDate) IS NULL OR t.fechaInicio >= :fechaInicioDesde) " +
           "AND (CAST(:fechaInicioHasta AS LocalDate) IS NULL OR t.fechaInicio <= :fechaInicioHasta) " +
           "AND (CAST(:fechaFinDesde AS LocalDate) IS NULL OR t.fechaFin >= :fechaFinDesde) " +
           "AND (CAST(:fechaFinHasta AS LocalDate) IS NULL OR t.fechaFin <= :fechaFinHasta)")
    Page<Training> searchPaged(@Param("tenantId") Long tenantId,
                                @Param("search") String search,
                                @Param("estado") Training.TrainingStatus estado,
                                @Param("instructor") String instructor,
                                @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
                                @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
                                @Param("fechaFinDesde") LocalDate fechaFinDesde,
                                @Param("fechaFinHasta") LocalDate fechaFinHasta,
                                Pageable pageable);
}
