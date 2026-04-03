package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.UserThemePreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface UserThemePreferenceRepository extends JpaRepository<UserThemePreferenceEntity, Long> {
    Optional<UserThemePreferenceEntity> findByUserIdAndCompanyIdAndModule(Long userId, Long companyId, String module);

    @Modifying
    @Transactional
    void deleteByUserIdAndCompanyIdAndModule(Long userId, Long companyId, String module);
}
