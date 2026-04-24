package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {

    List<EvaluationCriteria> findByTenantId(Long tenantId);

    List<EvaluationCriteria> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    Optional<EvaluationCriteria> findByIdAndTenantId(Long id, Long tenantId);
}
