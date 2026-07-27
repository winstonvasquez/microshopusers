package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaasPlanRepository extends JpaRepository<SaasPlanEntity, Long> {
    Optional<SaasPlanEntity> findByCode(String code);

    @Query("SELECT m.code FROM SaasPlanModuleEntity pm JOIN pm.module m " +
           "WHERE pm.plan.id = :planId AND m.isActive = true ORDER BY m.sortOrder")
    List<String> findModuleCodesByPlanId(@Param("planId") Long planId);
}
