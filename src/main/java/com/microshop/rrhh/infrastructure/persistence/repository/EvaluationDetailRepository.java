package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.EvaluationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationDetailRepository extends JpaRepository<EvaluationDetail, Long> {

    List<EvaluationDetail> findByTenantIdAndEvaluationId(Long tenantId, Long evaluationId);
}
