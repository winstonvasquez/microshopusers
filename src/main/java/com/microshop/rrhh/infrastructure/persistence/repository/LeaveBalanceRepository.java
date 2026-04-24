package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByTenantIdAndEmployeeIdAndAnio(Long tenantId, Long employeeId, Integer anio);

    List<LeaveBalance> findByTenantIdAndAnio(Long tenantId, Integer anio);

    List<LeaveBalance> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
}
