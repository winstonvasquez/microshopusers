package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.VacationRequest;
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
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByTenantId(Long tenantId);

    Optional<VacationRequest> findByIdAndTenantId(Long id, Long tenantId);

    List<VacationRequest> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    List<VacationRequest> findByTenantIdAndEstado(Long tenantId, VacationRequest.VacationStatus estado);

    List<VacationRequest> findByTenantIdAndEmployee_IdAndEstado(Long tenantId, Long employeeId, VacationRequest.VacationStatus estado);

    long countByTenantIdAndEstado(Long tenantId, VacationRequest.VacationStatus estado);

    // Filtros avanzados (2026-07-27): tipo de vacación, empleado, departamento, aprobador y rangos
    // de fecha de inicio/fin/aprobación, + codigoEmpleado en el ILIKE (faltaba) y @EntityGraph
    // sobre employee/aprobadoPor para evitar N+1 (el mapper toca ambas relaciones).
    @EntityGraph(attributePaths = {"employee", "aprobadoPor"})
    @Query("SELECT v FROM VacationRequest v WHERE v.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(v.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(v.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(v.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (:estado IS NULL OR v.estado = :estado) " +
           "AND (:tipoVacacion IS NULL OR v.tipoVacacion = :tipoVacacion) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR v.employee.id = :employeeId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR v.employee.department.id = :departmentId) " +
           "AND (CAST(:aprobadoPorId AS Long) IS NULL OR v.aprobadoPor.id = :aprobadoPorId) " +
           "AND (CAST(:fechaInicioDesde AS LocalDate) IS NULL OR v.fechaInicio >= :fechaInicioDesde) " +
           "AND (CAST(:fechaInicioHasta AS LocalDate) IS NULL OR v.fechaInicio <= :fechaInicioHasta) " +
           "AND (CAST(:fechaFinDesde AS LocalDate) IS NULL OR v.fechaFin >= :fechaFinDesde) " +
           "AND (CAST(:fechaFinHasta AS LocalDate) IS NULL OR v.fechaFin <= :fechaFinHasta) " +
           "AND (CAST(:fechaAprobacionDesde AS LocalDate) IS NULL OR v.fechaAprobacion >= :fechaAprobacionDesde) " +
           "AND (CAST(:fechaAprobacionHasta AS LocalDate) IS NULL OR v.fechaAprobacion <= :fechaAprobacionHasta)")
    Page<VacationRequest> searchPaged(@Param("tenantId") Long tenantId,
                                      @Param("search") String search,
                                      @Param("estado") VacationRequest.VacationStatus estado,
                                      @Param("tipoVacacion") VacationRequest.VacationType tipoVacacion,
                                      @Param("employeeId") Long employeeId,
                                      @Param("departmentId") Long departmentId,
                                      @Param("aprobadoPorId") Long aprobadoPorId,
                                      @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
                                      @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
                                      @Param("fechaFinDesde") LocalDate fechaFinDesde,
                                      @Param("fechaFinHasta") LocalDate fechaFinHasta,
                                      @Param("fechaAprobacionDesde") LocalDate fechaAprobacionDesde,
                                      @Param("fechaAprobacionHasta") LocalDate fechaAprobacionHasta,
                                      Pageable pageable);
}
