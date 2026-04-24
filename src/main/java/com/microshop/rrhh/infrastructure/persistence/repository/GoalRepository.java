package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByTenantId(Long tenantId);

    Optional<Goal> findByIdAndTenantId(Long id, Long tenantId);

    List<Goal> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    List<Goal> findByTenantIdAndEstado(Long tenantId, Goal.GoalStatus estado);

    List<Goal> findByTenantIdAndEmployeeIdAndEstado(Long tenantId, Long employeeId, Goal.GoalStatus estado);
}
