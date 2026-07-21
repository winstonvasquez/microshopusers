package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.PerformanceEvaluation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
