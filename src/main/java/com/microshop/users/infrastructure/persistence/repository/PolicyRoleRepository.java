package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.PolicyRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRoleRepository extends JpaRepository<PolicyRoleEntity, Long> {

    @Query("SELECT pr FROM PolicyRoleEntity pr JOIN FETCH pr.policy WHERE pr.rol.id = :rolId")
    List<PolicyRoleEntity> findByRolIdWithPolicy(@Param("rolId") Long rolId);
}
