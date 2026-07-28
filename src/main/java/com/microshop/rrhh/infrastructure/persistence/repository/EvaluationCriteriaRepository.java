package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.EvaluationCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {

    List<EvaluationCriteria> findByTenantId(Long tenantId);

    List<EvaluationCriteria> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    Optional<EvaluationCriteria> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Listado paginado con filtros avanzados: búsqueda por texto (nombre/descripción) y
     * estado (activo/inactivo) — reemplaza el filtrado client-side de criteria-list.
     */
    @Query("SELECT c FROM EvaluationCriteria c WHERE c.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(c.descripcion) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:activo AS Boolean) IS NULL OR c.activo = :activo)")
    Page<EvaluationCriteria> searchPaged(@Param("tenantId") Long tenantId,
                                          @Param("search") String search,
                                          @Param("activo") Boolean activo,
                                          Pageable pageable);
}
