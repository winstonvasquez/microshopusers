package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasPlanModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaasPlanModuleRepository extends JpaRepository<SaasPlanModuleEntity, Long> {
    List<SaasPlanModuleEntity> findByPlanId(Long planId);

    void deleteByPlanId(Long planId);
}
