package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByTenantId(Long tenantId);

    List<Position> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    List<Position> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);

    Optional<Position> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Position> findByCodigoAndTenantId(String codigo, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndActivo(Long tenantId, Boolean activo);

    long countByTenantIdAndDepartmentId(Long tenantId, Long departmentId);

    @Query("SELECT p FROM Position p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Position> searchByTenantIdAndTerm(@Param("tenantId") Long tenantId, @Param("term") String term);

    @Query("SELECT p FROM Position p WHERE p.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(p.codigo) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR p.department.id = :departmentId) " +
           "AND (CAST(:activo AS Boolean) IS NULL OR p.activo = :activo) " +
           "AND (CAST(:nivel AS String) IS NULL OR CAST(:nivel AS String) = '' OR p.nivel = :nivel)")
    Page<Position> searchPaged(@Param("tenantId") Long tenantId,
                               @Param("search") String search,
                               @Param("departmentId") Long departmentId,
                               @Param("activo") Boolean activo,
                               @Param("nivel") String nivel,
                               Pageable pageable);
}
