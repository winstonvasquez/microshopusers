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

    // Analytics dashboard (2026-07-22): equivalente COUNT de findExpiringBefore, sin traer entidades.
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.tenantId = :tenantId AND c.fechaFin IS NOT NULL AND c.fechaFin <= :fecha AND c.estado = 'ACTIVO'")
    long countExpiringBefore(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    // Page + solo ManyToOne (sin colecciones) → seguro con paginación.
    // Filtros avanzados (2026-07-27): jornada laboral, moneda, empleado, departamento (vía
    // employee.department, sin JOIN real: solo lee la FK) y rangos de fecha de inicio/fin.
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(c.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(c.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(c.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:tipo IS NULL OR c.tipoContrato = :tipo) " +
           "AND (:jornada IS NULL OR c.jornadaLaboral = :jornada) " +
           "AND (CAST(:moneda AS String) IS NULL OR CAST(:moneda AS String) = '' OR c.moneda = :moneda) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR c.employee.id = :employeeId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR c.employee.department.id = :departmentId) " +
           "AND (CAST(:fechaInicioDesde AS LocalDate) IS NULL OR c.fechaInicio >= :fechaInicioDesde) " +
           "AND (CAST(:fechaInicioHasta AS LocalDate) IS NULL OR c.fechaInicio <= :fechaInicioHasta) " +
           "AND (CAST(:fechaFinDesde AS LocalDate) IS NULL OR c.fechaFin >= :fechaFinDesde) " +
           "AND (CAST(:fechaFinHasta AS LocalDate) IS NULL OR c.fechaFin <= :fechaFinHasta)")
    Page<Contract> searchPaged(@Param("tenantId") Long tenantId,
                               @Param("search") String search,
                               @Param("estado") Contract.ContractStatus estado,
                               @Param("tipo") Contract.ContractType tipo,
                               @Param("jornada") Contract.WorkingDay jornada,
                               @Param("moneda") String moneda,
                               @Param("employeeId") Long employeeId,
                               @Param("departmentId") Long departmentId,
                               @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
                               @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
                               @Param("fechaFinDesde") LocalDate fechaFinDesde,
                               @Param("fechaFinHasta") LocalDate fechaFinHasta,
                               Pageable pageable);
}
