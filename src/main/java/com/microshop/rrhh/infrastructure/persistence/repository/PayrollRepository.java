package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByTenantId(Long tenantId);

    Optional<Payroll> findByIdAndTenantId(Long id, Long tenantId);

    List<Payroll> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    // Autoservicio "Mis Boletas": filtro opcional de estado (ver SelfServiceController.getMyPayslips).
    List<Payroll> findByTenantIdAndEmployee_IdAndEstado(Long tenantId, Long employeeId, Payroll.PayrollStatus estado);

    List<Payroll> findByTenantIdAndPeriodo(Long tenantId, String periodo);

    Optional<Payroll> findByTenantIdAndEmployee_IdAndPeriodo(Long tenantId, Long employeeId, String periodo);

    List<Payroll> findByTenantIdAndEstado(Long tenantId, Payroll.PayrollStatus estado);

    long countByTenantIdAndPeriodo(Long tenantId, String periodo);

    // Analytics dashboard (2026-07-22): SUM agregado en SQL, COALESCE evita null si no hay filas.
    @Query("SELECT COALESCE(SUM(p.sueldoBase), 0) FROM Payroll p WHERE p.tenantId = :tenantId AND p.estado = :estado")
    BigDecimal sumSueldoBaseByTenantIdAndEstado(@Param("tenantId") Long tenantId, @Param("estado") Payroll.PayrollStatus estado);

    /**
     * Listado con filtros avanzados opcionales: periodo, búsqueda por texto (empleado), estado de
     * boleta, empleado, departamento y sistema previsional/AFP, más rango de fecha de pago. Todos
     * los parámetros son opcionales (null = no filtra); reemplaza el filtrado 100% client-side que
     * antes hacía el componente Angular de planillas sobre el periodo cargado en memoria.
     */
    @EntityGraph(attributePaths = "employee")
    @Query("SELECT p FROM Payroll p WHERE p.tenantId = :tenantId " +
            "AND (:periodo IS NULL OR :periodo = '' OR p.periodo = :periodo) " +
            "AND (:search IS NULL OR :search = '' " +
            "     OR LOWER(p.employee.nombres) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.employee.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:estado IS NULL OR p.estado = :estado) " +
            "AND (:employeeId IS NULL OR p.employee.id = :employeeId) " +
            "AND (:departmentId IS NULL OR p.employee.department.id = :departmentId) " +
            "AND (:afpOnp IS NULL OR :afpOnp = '' OR p.afpOnp = :afpOnp) " +
            "AND (:fechaPagoDesde IS NULL OR p.fechaPago >= :fechaPagoDesde) " +
            "AND (:fechaPagoHasta IS NULL OR p.fechaPago <= :fechaPagoHasta)")
    Page<Payroll> searchPaged(@Param("tenantId") Long tenantId,
            @Param("periodo") String periodo,
            @Param("search") String search,
            @Param("estado") Payroll.PayrollStatus estado,
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("afpOnp") String afpOnp,
            @Param("fechaPagoDesde") LocalDate fechaPagoDesde,
            @Param("fechaPagoHasta") LocalDate fechaPagoHasta,
            Pageable pageable);
}
