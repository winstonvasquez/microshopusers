package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByTenantId(Long tenantId);

    List<Department> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    List<Department> findByTenantIdAndParentIsNull(Long tenantId);

    List<Department> findByTenantIdAndParentId(Long tenantId, Long parentId);

    Optional<Department> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Department> findByCodigoAndTenantId(String codigo, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndActivo(Long tenantId, Boolean activo);

    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND " +
           "(LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(d.codigo) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Department> searchByTenantIdAndTerm(@Param("tenantId") Long tenantId, @Param("term") String term);
}
