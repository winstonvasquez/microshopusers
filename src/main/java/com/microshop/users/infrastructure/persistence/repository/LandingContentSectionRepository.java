package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.LandingContentSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandingContentSectionRepository extends JpaRepository<LandingContentSectionEntity, Long> {
    Optional<LandingContentSectionEntity> findBySectionKey(String sectionKey);

    List<LandingContentSectionEntity> findAllByActivoTrue();
}
