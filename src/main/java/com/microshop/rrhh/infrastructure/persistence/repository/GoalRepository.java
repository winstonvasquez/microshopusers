package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Goal;
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
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // N+1 fix (2026-07-21): mapper toGoalDto toca employee y asignadoPor (ambos ManyToOne).
    @EntityGraph(attributePaths = {"employee", "asignadoPor"})
    List<Goal> findByTenantId(Long tenantId);

    Optional<Goal> findByIdAndTenantId(Long id, Long tenantId);

    @EntityGraph(attributePaths = {"employee", "asignadoPor"})
    List<Goal> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    @EntityGraph(attributePaths = {"employee", "asignadoPor"})
    List<Goal> findByTenantIdAndEstado(Long tenantId, Goal.GoalStatus estado);

    List<Goal> findByTenantIdAndEmployeeIdAndEstado(Long tenantId, Long employeeId, Goal.GoalStatus estado);

    // Analytics dashboard (2026-07-22): count agregado en SQL en lugar de traer todas las metas.
    long countByTenantIdAndEstado(Long tenantId, Goal.GoalStatus estado);

    // Page + solo ManyToOne (sin colecciones) → seguro con paginación, no infla filas.
    // Filtros avanzados (2026-07-27): búsqueda por texto, estado, prioridad, empleado,
    // asignador, departamento (vía employee.department, sin JOIN real: solo lee la FK) y
    // rangos de fecha de inicio/fin — reemplaza el filtrado client-side de goal-list.
    @EntityGraph(attributePaths = {"employee", "asignadoPor"})
    @Query("SELECT g FROM Goal g WHERE g.tenantId = :tenantId " +
           "AND (CAST(:search AS String) IS NULL OR CAST(:search AS String) = '' OR " +
           "  LOWER(g.titulo) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(g.descripcion) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(g.employee.nombres) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
           "  LOWER(g.employee.apellidos) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))) " +
           "AND (:estado IS NULL OR g.estado = :estado) " +
           "AND (:prioridad IS NULL OR g.prioridad = :prioridad) " +
           "AND (CAST(:employeeId AS Long) IS NULL OR g.employee.id = :employeeId) " +
           "AND (CAST(:asignadoPorId AS Long) IS NULL OR g.asignadoPor.id = :asignadoPorId) " +
           "AND (CAST(:departmentId AS Long) IS NULL OR g.employee.department.id = :departmentId) " +
           "AND (CAST(:fechaInicioDesde AS LocalDate) IS NULL OR g.fechaInicio >= :fechaInicioDesde) " +
           "AND (CAST(:fechaInicioHasta AS LocalDate) IS NULL OR g.fechaInicio <= :fechaInicioHasta) " +
           "AND (CAST(:fechaFinDesde AS LocalDate) IS NULL OR g.fechaFin >= :fechaFinDesde) " +
           "AND (CAST(:fechaFinHasta AS LocalDate) IS NULL OR g.fechaFin <= :fechaFinHasta)")
    Page<Goal> searchPaged(@Param("tenantId") Long tenantId,
                           @Param("search") String search,
                           @Param("estado") Goal.GoalStatus estado,
                           @Param("prioridad") Goal.Priority prioridad,
                           @Param("employeeId") Long employeeId,
                           @Param("asignadoPorId") Long asignadoPorId,
                           @Param("departmentId") Long departmentId,
                           @Param("fechaInicioDesde") LocalDate fechaInicioDesde,
                           @Param("fechaInicioHasta") LocalDate fechaInicioHasta,
                           @Param("fechaFinDesde") LocalDate fechaFinDesde,
                           @Param("fechaFinHasta") LocalDate fechaFinHasta,
                           Pageable pageable);
}
