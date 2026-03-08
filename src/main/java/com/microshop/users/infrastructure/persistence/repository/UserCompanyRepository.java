package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompanyRepository extends JpaRepository<UserCompanyEntity, Long> {
    Optional<UserCompanyEntity> findByUsuarioIdAndCompanyId(Long usuarioId, Long companyId);

    @EntityGraph(attributePaths = { "roles", "roles.rol" })
    List<UserCompanyEntity> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = { "roles", "roles.rol" })
    List<UserCompanyEntity> findByCompanyId(Long companyId);
}
