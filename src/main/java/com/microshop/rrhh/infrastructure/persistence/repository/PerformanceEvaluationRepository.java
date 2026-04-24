package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.PerformanceEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluation, Long> {

    List<PerformanceEvaluation> findByTenantId(Long tenantId);

    Optional<PerformanceEvaluation> findByIdAndTenantId(Long id, Long tenantId);

    List<PerformanceEvaluation> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    List<PerformanceEvaluation> findByTenantIdAndPeriodo(Long tenantId, String periodo);

    Optional<PerformanceEvaluation> findByTenantIdAndEmployeeIdAndPeriodo(Long tenantId, Long employeeId, String periodo);

    List<PerformanceEvaluation> findByTenantIdAndEvaluadorId(Long tenantId, Long evaluadorId);
}
