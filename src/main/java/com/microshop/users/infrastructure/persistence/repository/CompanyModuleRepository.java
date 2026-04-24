package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.CompanyModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompanyModuleRepository extends JpaRepository<CompanyModuleEntity, Long> {
    List<CompanyModuleEntity> findByCompanyId(Long companyId);
}
