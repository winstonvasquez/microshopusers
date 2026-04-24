package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SaasSubscriptionRepository extends JpaRepository<SaasSubscriptionEntity, Long> {
    Optional<SaasSubscriptionEntity> findByCompanyId(Long companyId);

    @Query("SELECT m.code FROM SaasSubscriptionEntity s " +
           "JOIN SaasPlanModuleEntity pm ON pm.plan.id = s.plan.id " +
           "JOIN SaasModuleEntity m ON m.id = pm.module.id " +
           "WHERE s.company.id = :companyId AND s.activo = true AND m.isActive = true " +
           "ORDER BY m.sortOrder")
    List<String> findEnabledModuleCodesByCompanyId(@Param("companyId") Long companyId);
}
