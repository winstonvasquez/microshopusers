package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.CompanyThemeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyThemeConfigRepository extends JpaRepository<CompanyThemeConfigEntity, Long> {
    Optional<CompanyThemeConfigEntity> findByCompanyIdAndModule(Long companyId, String module);
}
