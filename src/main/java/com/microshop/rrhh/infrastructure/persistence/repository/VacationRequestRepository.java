package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.VacationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    List<VacationRequest> findByTenantId(Long tenantId);

    Optional<VacationRequest> findByIdAndTenantId(Long id, Long tenantId);

    List<VacationRequest> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    List<VacationRequest> findByTenantIdAndEstado(Long tenantId, VacationRequest.VacationStatus estado);

    List<VacationRequest> findByTenantIdAndEmployee_IdAndEstado(Long tenantId, Long employeeId, VacationRequest.VacationStatus estado);

    long countByTenantIdAndEstado(Long tenantId, VacationRequest.VacationStatus estado);
}
