package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // N+1 fix (2026-07-21): mapper toDto toca department/position/supervisor (todos ManyToOne).
    @EntityGraph(attributePaths = {"department", "position", "supervisor"})
    List<Employee> findByTenantId(Long tenantId);

    Optional<Employee> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Employee> findByCodigoEmpleadoAndTenantId(String codigoEmpleado, Long tenantId);

    Optional<Employee> findByDocumentoIdentidadAndTenantId(String documentoIdentidad, Long tenantId);

    @EntityGraph(attributePaths = {"department", "position", "supervisor"})
    List<Employee> findByTenantIdAndEstado(Long tenantId, Employee.EmployeeStatus estado);

    @EntityGraph(attributePaths = {"department", "position", "supervisor"})
    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId AND " +
           "(LOWER(e.nombres) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.codigoEmpleado) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Employee> searchByTenantIdAndTerm(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm);

    // Page + solo ManyToOne (sin colecciones) → seguro con paginación, no infla filas.
    // Filtros avanzados (2026-07-27): selects (puesto, supervisor, tipo doc, sistema previsional,
    // AFP, género, estado civil, tipo de contrato vigente) + rangos de fecha de ingreso, salida y
    // nacimiento. Sigue el patrón (:x IS NULL OR :x = '' OR ...) ya usado en :search de este archivo.
    @EntityGraph(attributePaths = {"department", "position", "supervisor"})
    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "  LOWER(e.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(e.codigoEmpleado) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(e.documentoIdentidad) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR e.estado = :estado) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:positionId IS NULL OR e.position.id = :positionId) " +
           "AND (:supervisorId IS NULL OR e.supervisor.id = :supervisorId) " +
           "AND (:tipoDocumento IS NULL OR :tipoDocumento = '' OR e.tipoDocumento = :tipoDocumento) " +
           "AND (:sistemaPrevisional IS NULL OR :sistemaPrevisional = '' OR e.sistemaPrevisional = :sistemaPrevisional) " +
           "AND (:afpNombre IS NULL OR :afpNombre = '' OR e.afpNombre = :afpNombre) " +
           "AND (:genero IS NULL OR e.genero = :genero) " +
           "AND (:estadoCivil IS NULL OR e.estadoCivil = :estadoCivil) " +
           "AND (:tipoContrato IS NULL OR EXISTS (SELECT 1 FROM Contract c WHERE c.employee = e " +
           "  AND c.tipoContrato = :tipoContrato AND c.estado = :estadoContratoVigente)) " +
           "AND (:fechaIngresoDesde IS NULL OR e.fechaIngreso >= :fechaIngresoDesde) " +
           "AND (:fechaIngresoHasta IS NULL OR e.fechaIngreso <= :fechaIngresoHasta) " +
           "AND (:fechaSalidaDesde IS NULL OR e.fechaSalida >= :fechaSalidaDesde) " +
           "AND (:fechaSalidaHasta IS NULL OR e.fechaSalida <= :fechaSalidaHasta) " +
           "AND (:fechaNacimientoDesde IS NULL OR e.fechaNacimiento >= :fechaNacimientoDesde) " +
           "AND (:fechaNacimientoHasta IS NULL OR e.fechaNacimiento <= :fechaNacimientoHasta)")
    Page<Employee> searchPaged(@Param("tenantId") Long tenantId,
                               @Param("search") String search,
                               @Param("estado") Employee.EmployeeStatus estado,
                               @Param("departmentId") Long departmentId,
                               @Param("positionId") Long positionId,
                               @Param("supervisorId") Long supervisorId,
                               @Param("tipoDocumento") String tipoDocumento,
                               @Param("sistemaPrevisional") String sistemaPrevisional,
                               @Param("afpNombre") String afpNombre,
                               @Param("genero") Employee.Gender genero,
                               @Param("estadoCivil") Employee.MaritalStatus estadoCivil,
                               @Param("tipoContrato") Contract.ContractType tipoContrato,
                               @Param("estadoContratoVigente") Contract.ContractStatus estadoContratoVigente,
                               @Param("fechaIngresoDesde") LocalDate fechaIngresoDesde,
                               @Param("fechaIngresoHasta") LocalDate fechaIngresoHasta,
                               @Param("fechaSalidaDesde") LocalDate fechaSalidaDesde,
                               @Param("fechaSalidaHasta") LocalDate fechaSalidaHasta,
                               @Param("fechaNacimientoDesde") LocalDate fechaNacimientoDesde,
                               @Param("fechaNacimientoHasta") LocalDate fechaNacimientoHasta,
                               Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndEstado(Long tenantId, Employee.EmployeeStatus estado);

    Optional<Employee> findByTenantIdAndUserId(Long tenantId, Long userId);

    // Analytics dashboard (2026-07-22): agregación SQL en lugar de traer todos los empleados a memoria.
    @Query("SELECT e.department.nombre AS nombre, COUNT(e) AS total FROM Employee e " +
           "WHERE e.tenantId = :tenantId AND e.department IS NOT NULL " +
           "GROUP BY e.department.nombre")
    List<DepartmentHeadcount> countByDepartment(@Param("tenantId") Long tenantId);

    interface DepartmentHeadcount {
        String getNombre();
        Long getTotal();
    }
}
