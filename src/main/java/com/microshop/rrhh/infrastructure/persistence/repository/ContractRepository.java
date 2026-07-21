package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // N+1 fix (2026-07-21): mapper toDto toca employee (ManyToOne).
    @EntityGraph(attributePaths = "employee")
    List<Contract> findByTenantId(Long tenantId);

    @EntityGraph(attributePaths = "employee")
    List<Contract> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<Contract> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId AND c.employee.id = :employeeId AND c.estado = 'ACTIVO'")
    Optional<Contract> findActiveByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    @EntityGraph(attributePaths = "employee")
    List<Contract> findByTenantIdAndEstado(Long tenantId, Contract.ContractStatus estado);

    @EntityGraph(attributePaths = "employee")
    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId AND c.fechaFin IS NOT NULL AND c.fechaFin <= :fecha AND c.estado = 'ACTIVO'")
    List<Contract> findExpiringBefore(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndEstado(Long tenantId, Contract.ContractStatus estado);

    // Page + solo ManyToOne (sin colecciones) → seguro con paginación.
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "  LOWER(c.employee.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.employee.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:tipo IS NULL OR c.tipoContrato = :tipo)")
    Page<Contract> searchPaged(@Param("tenantId") Long tenantId,
                               @Param("search") String search,
                               @Param("estado") Contract.ContractStatus estado,
                               @Param("tipo") Contract.ContractType tipo,
                               Pageable pageable);
}
