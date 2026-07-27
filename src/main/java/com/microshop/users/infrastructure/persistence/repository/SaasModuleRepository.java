package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaasModuleRepository extends JpaRepository<SaasModuleEntity, Long> {
    List<SaasModuleEntity> findAllByIsActiveTrueOrderBySortOrderAsc();

    Optional<SaasModuleEntity> findByCode(String code);
}
