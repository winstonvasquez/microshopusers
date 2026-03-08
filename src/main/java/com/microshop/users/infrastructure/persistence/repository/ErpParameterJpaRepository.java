package com.microshop.users.infrastructure.persistence.repository;

import com.microshop.users.infrastructure.persistence.entity.ErpParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ErpParameterJpaRepository extends JpaRepository<ErpParameterEntity, Long> {

    Optional<ErpParameterEntity> findByParamKey(String paramKey);

    List<ErpParameterEntity> findAllByIsActiveTrue();
}
