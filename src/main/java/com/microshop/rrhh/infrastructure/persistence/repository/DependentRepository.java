package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<Dependent> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
}
