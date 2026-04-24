package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByTenantId(Long tenantId);

    Optional<Payroll> findByIdAndTenantId(Long id, Long tenantId);

    List<Payroll> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    List<Payroll> findByTenantIdAndPeriodo(Long tenantId, String periodo);

    Optional<Payroll> findByTenantIdAndEmployee_IdAndPeriodo(Long tenantId, Long employeeId, String periodo);

    List<Payroll> findByTenantIdAndEstado(Long tenantId, Payroll.PayrollStatus estado);

    long countByTenantIdAndPeriodo(Long tenantId, String periodo);
}
