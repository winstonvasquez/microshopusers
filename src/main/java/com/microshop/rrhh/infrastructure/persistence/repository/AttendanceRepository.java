package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Attendance;
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
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByTenantId(Long tenantId);

    Optional<Attendance> findByIdAndTenantId(Long id, Long tenantId);

    // N+1 fix (2026-07-21): mapper toDto toca employee (ManyToOne). Otros listados de
    // asistencia (getMonthlySummary ya cubierto por findByTenantIdAndFechaBetween abajo).
    @EntityGraph(attributePaths = "employee")
    List<Attendance> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    @EntityGraph(attributePaths = "employee")
    List<Attendance> findByTenantIdAndFecha(Long tenantId, LocalDate fecha);

    Optional<Attendance> findByTenantIdAndEmployee_IdAndFecha(Long tenantId, Long employeeId, LocalDate fecha);

    @EntityGraph(attributePaths = "employee")
    @Query("SELECT a FROM Attendance a WHERE a.tenantId = :tenantId AND a.employee.id = :employeeId " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fecha DESC")
    List<Attendance> findByTenantIdAndEmployeeIdAndFechaBetween(
            @Param("tenantId") Long tenantId,
            @Param("employeeId") Long employeeId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    long countByTenantIdAndFecha(Long tenantId, LocalDate fecha);

    @EntityGraph(attributePaths = "employee")
    List<Attendance> findByTenantIdAndFechaBetween(Long tenantId, LocalDate desde, LocalDate hasta);

    // Export server-side (sin filtro de fecha): trae todo el tenant con employee ya cargado (evita N+1 en el mapper).
    @EntityGraph(attributePaths = "employee")
    List<Attendance> findByTenantIdOrderByFechaDesc(Long tenantId);

    /**
     * Listado avanzado con filtros opcionales: búsqueda por texto (empleado/observaciones),
     * empleado, departamento (vía employee.department), tipo de registro, aprobador y rango
     * de fechas. Reemplaza el filtrado 100% client-side que hacía el componente Angular sobre
     * un único día cargado (patrón "(:x IS NULL OR ...)", vara de medir: EmployeeRepository.searchPaged).
     */
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT a FROM Attendance a WHERE a.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' " +
           "     OR LOWER(a.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.observaciones) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR a.employee.id = :employeeId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR a.employee.department.id = :departmentId) " +
           "AND (:tipoRegistro IS NULL OR a.tipoRegistro = :tipoRegistro) " +
           "AND (CAST(:aprobadoPorId AS Long) IS NULL OR a.aprobadoPor.id = :aprobadoPorId) " +
           "AND (CAST(:fechaDesde AS LocalDate) IS NULL OR a.fecha >= :fechaDesde) " +
           "AND (CAST(:fechaHasta AS LocalDate) IS NULL OR a.fecha <= :fechaHasta)")
    Page<Attendance> searchPaged(@Param("tenantId") Long tenantId,
            @Param("search") String search,
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("tipoRegistro") Attendance.AttendanceType tipoRegistro,
            @Param("aprobadoPorId") Long aprobadoPorId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);

    /**
     * Autoservicio ("Mi Asistencia"): asistencia de UN empleado con filtros opcionales de
     * tipo de registro y rango de fechas (sustituye el mes exacto). Scoped por tenant +
     * employeeId (riesgo cross-tenant nulo, ver SelfServiceController.resolveCurrentEmployeeId).
     */
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT a FROM Attendance a WHERE a.tenantId = :tenantId AND a.employee.id = :employeeId " +
           "AND (:tipoRegistro IS NULL OR a.tipoRegistro = :tipoRegistro) " +
           "AND (CAST(:fechaDesde AS LocalDate) IS NULL OR a.fecha >= :fechaDesde) " +
           "AND (CAST(:fechaHasta AS LocalDate) IS NULL OR a.fecha <= :fechaHasta) " +
           "ORDER BY a.fecha DESC")
    List<Attendance> findByTenantIdAndEmployeeIdFiltered(@Param("tenantId") Long tenantId,
            @Param("employeeId") Long employeeId,
            @Param("tipoRegistro") Attendance.AttendanceType tipoRegistro,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta);

    /** Ver {@link #searchPaged} — misma query sin paginar, para exportación server-side. */
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT a FROM Attendance a WHERE a.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' " +
           "     OR LOWER(a.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(a.observaciones) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR a.employee.id = :employeeId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR a.employee.department.id = :departmentId) " +
           "AND (:tipoRegistro IS NULL OR a.tipoRegistro = :tipoRegistro) " +
           "AND (CAST(:aprobadoPorId AS Long) IS NULL OR a.aprobadoPor.id = :aprobadoPorId) " +
           "AND (CAST(:fechaDesde AS LocalDate) IS NULL OR a.fecha >= :fechaDesde) " +
           "AND (CAST(:fechaHasta AS LocalDate) IS NULL OR a.fecha <= :fechaHasta) " +
           "ORDER BY a.fecha DESC")
    List<Attendance> searchAllForExport(@Param("tenantId") Long tenantId,
            @Param("search") String search,
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("tipoRegistro") Attendance.AttendanceType tipoRegistro,
            @Param("aprobadoPorId") Long aprobadoPorId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta);
}
