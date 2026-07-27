package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // N+1 fix (2026-07-21): mapper toDto toca manager y parent (ManyToOne).
    // NO se incluyen employees/positions: son 2 colecciones @OneToMany (bags) de la
    // misma entidad → fetch conjunto dispararía MultipleBagFetchException; quedan
    // cubiertas por default_batch_fetch_size (application.yml).
    @EntityGraph(attributePaths = {"manager", "parent"})
    List<Department> findByTenantId(Long tenantId);

    @EntityGraph(attributePaths = {"manager", "parent"})
    List<Department> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    @EntityGraph(attributePaths = {"manager", "parent"})
    List<Department> findByTenantIdAndParentIsNull(Long tenantId);

    @EntityGraph(attributePaths = {"manager", "parent"})
    List<Department> findByTenantIdAndParentId(Long tenantId, Long parentId);

    Optional<Department> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Department> findByCodigoAndTenantId(String codigo, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndActivo(Long tenantId, Boolean activo);

    @EntityGraph(attributePaths = {"manager", "parent"})
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND " +
           "(LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(d.codigo) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Department> searchByTenantIdAndTerm(@Param("tenantId") Long tenantId, @Param("term") String term);

    // Page + solo ManyToOne (sin colecciones) → seguro con paginación.
    // Filtros avanzados: manager, departamento padre y rango de fecha de creación.
    @EntityGraph(attributePaths = {"manager", "parent"})
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "  LOWER(d.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(d.codigo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:activo IS NULL OR d.activo = :activo) " +
           "AND (:managerId IS NULL OR d.manager.id = :managerId) " +
           "AND (:parentId IS NULL OR d.parent.id = :parentId) " +
           "AND (:createdAtDesde IS NULL OR d.createdAt >= :createdAtDesde) " +
           "AND (:createdAtHasta IS NULL OR d.createdAt <= :createdAtHasta)")
    Page<Department> searchPaged(@Param("tenantId") Long tenantId,
                                 @Param("search") String search,
                                 @Param("activo") Boolean activo,
                                 @Param("managerId") Long managerId,
                                 @Param("parentId") Long parentId,
                                 @Param("createdAtDesde") LocalDateTime createdAtDesde,
                                 @Param("createdAtHasta") LocalDateTime createdAtHasta,
                                 Pageable pageable);
}
