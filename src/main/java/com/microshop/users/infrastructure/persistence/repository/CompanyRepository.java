package com.microshop.users.infrastructure.persistence.repository;


import com.microshop.users.application.dto.CompanyResponseDto;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByRuc(String ruc);

    Optional<CompanyEntity> findByRuc(String ruc);

    @Query("SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c")
    List<CompanyResponseDto> findAllProjected();

    @Query("SELECT new com.microshop.users.application.dto.CompanyResponseDto(c.id, c.name, c.ruc, c.isActive) FROM CompanyEntity c WHERE c.id = :id")
    Optional<CompanyResponseDto> findProjectedById(@Param("id") Long id);
}
