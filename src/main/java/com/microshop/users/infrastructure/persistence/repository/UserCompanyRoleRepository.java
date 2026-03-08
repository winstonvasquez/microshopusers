package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.UserCompanyRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCompanyRoleRepository extends JpaRepository<UserCompanyRoleEntity, Long> {
    List<UserCompanyRoleEntity> findByUserCompanyId(Long userCompanyId);
}
