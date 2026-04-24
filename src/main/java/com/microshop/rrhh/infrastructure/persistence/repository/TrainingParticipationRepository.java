package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.TrainingParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingParticipationRepository extends JpaRepository<TrainingParticipation, Long> {

    List<TrainingParticipation> findByTenantId(Long tenantId);

    Optional<TrainingParticipation> findByIdAndTenantId(Long id, Long tenantId);

    List<TrainingParticipation> findByTenantIdAndTrainingId(Long tenantId, Long trainingId);

    List<TrainingParticipation> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<TrainingParticipation> findByTenantIdAndTrainingIdAndEmployeeId(Long tenantId, Long trainingId, Long employeeId);

    List<TrainingParticipation> findByTenantIdAndEstado(Long tenantId, TrainingParticipation.ParticipationStatus estado);

    long countByTenantIdAndTrainingId(Long tenantId, Long trainingId);
}
