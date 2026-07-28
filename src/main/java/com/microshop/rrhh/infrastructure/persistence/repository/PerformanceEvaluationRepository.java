package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.PerformanceEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluation, Long> {

    // N+1 fix (2026-07-21): findByTenantId retorna List (no paginado) → seguro incluir
    // la colección "details" (único @OneToMany de la entidad, sin riesgo de
    // MultipleBagFetchException) + su ManyToOne anidado "details.criteria".
    @EntityGraph(attributePaths = {"employee", "evaluador", "details", "details.criteria"})
    List<PerformanceEvaluation> findByTenantId(Long tenantId);

    Optional<PerformanceEvaluation> findByIdAndTenantId(Long id, Long tenantId);

    @EntityGraph(attributePaths = {"employee", "evaluador", "details", "details.criteria"})
    List<PerformanceEvaluation> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    @EntityGraph(attributePaths = {"employee", "evaluador", "details", "details.criteria"})
    List<PerformanceEvaluation> findByTenantIdAndPeriodo(Long tenantId, String periodo);

    Optional<PerformanceEvaluation> findByTenantIdAndEmployeeIdAndPeriodo(Long tenantId, Long employeeId, String periodo);

    @EntityGraph(attributePaths = {"employee", "evaluador", "details", "details.criteria"})
    List<PerformanceEvaluation> findByTenantIdAndEvaluadorId(Long tenantId, Long evaluadorId);

    // Analytics dashboard (2026-07-22): counts/avg agregados en SQL en lugar de traer todas las evaluaciones.
    long countByTenantIdAndEstado(Long tenantId, PerformanceEvaluation.EvaluationStatus estado);

    @Query("SELECT COUNT(e) FROM PerformanceEvaluation e WHERE e.tenantId = :tenantId AND e.estado IN :estados")
    long countByTenantIdAndEstadoIn(@Param("tenantId") Long tenantId, @Param("estados") Collection<PerformanceEvaluation.EvaluationStatus> estados);

    @Query("SELECT COALESCE(SUM(e.puntaje), 0) FROM PerformanceEvaluation e WHERE e.tenantId = :tenantId")
    BigDecimal sumPuntajeByTenantId(@Param("tenantId") Long tenantId);

    long countByTenantId(Long tenantId);

    // Page + solo ManyToOne (sin colecciones "details") → seguro con paginación, no infla filas.
    // Filtros avanzados (2026-07-27): búsqueda por texto (empleado/evaluador/periodo), evaluado,
    // evaluador, departamento (vía employee.department, sin JOIN real: solo lee la FK) y periodo
    // exacto, además del rango de fecha de evaluación ya existente y el nuevo rango de próxima revisión.
    @EntityGraph(attributePaths = {"employee", "evaluador"})
    @Query("SELECT e FROM PerformanceEvaluation e WHERE e.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(e.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(e.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(e.employee.codigoEmpleado) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(e.evaluador.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(e.evaluador.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(e.periodo) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (:estado IS NULL OR e.estado = :estado) " +
           "AND (:tipo IS NULL OR e.tipoEvaluacion = :tipo) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR e.employee.id = :employeeId) " +
           "AND (CAST(:evaluadorId AS Long) IS NULL OR e.evaluador.id = :evaluadorId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR e.employee.department.id = :departmentId) " +
           "AND (CAST(:periodo AS String) IS NULL OR CAST(:periodo AS String) = '' OR e.periodo = :periodo) " +
           "AND (CAST(:desde AS LocalDate) IS NULL OR e.fechaEvaluacion >= :desde) " +
           "AND (CAST(:hasta AS LocalDate) IS NULL OR e.fechaEvaluacion <= :hasta) " +
           "AND (CAST(:proximaRevisionDesde AS LocalDate) IS NULL OR e.proximaRevision >= :proximaRevisionDesde) " +
           "AND (CAST(:proximaRevisionHasta AS LocalDate) IS NULL OR e.proximaRevision <= :proximaRevisionHasta)")
    Page<PerformanceEvaluation> findFiltered(@Param("tenantId") Long tenantId,
                                              @Param("search") String search,
                                              @Param("estado") PerformanceEvaluation.EvaluationStatus estado,
                                              @Param("tipo") PerformanceEvaluation.EvaluationType tipo,
                                              @Param("employeeId") Long employeeId,
                                              @Param("evaluadorId") Long evaluadorId,
                                              @Param("departmentId") Long departmentId,
                                              @Param("periodo") String periodo,
                                              @Param("desde") LocalDate desde,
                                              @Param("hasta") LocalDate hasta,
                                              @Param("proximaRevisionDesde") LocalDate proximaRevisionDesde,
                                              @Param("proximaRevisionHasta") LocalDate proximaRevisionHasta,
                                              Pageable pageable);
}
