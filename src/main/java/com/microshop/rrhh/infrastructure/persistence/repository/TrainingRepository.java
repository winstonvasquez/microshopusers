package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByTenantId(Long tenantId);

    Optional<Training> findByIdAndTenantId(Long id, Long tenantId);

    List<Training> findByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);

    long countByTenantIdAndEstado(Long tenantId, Training.TrainingStatus estado);
}
