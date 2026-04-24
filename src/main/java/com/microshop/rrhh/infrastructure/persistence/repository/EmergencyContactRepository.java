package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<EmergencyContact> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
}
