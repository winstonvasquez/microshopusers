package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.SaasPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SaasPlanRepository extends JpaRepository<SaasPlanEntity, Long> {
    Optional<SaasPlanEntity> findByCode(String code);
}
