package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Position;
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
}
