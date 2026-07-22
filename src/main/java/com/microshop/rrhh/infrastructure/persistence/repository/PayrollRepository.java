package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // Analytics dashboard (2026-07-22): SUM agregado en SQL, COALESCE evita null si no hay filas.
    @Query("SELECT COALESCE(SUM(p.sueldoBase), 0) FROM Payroll p WHERE p.tenantId = :tenantId AND p.estado = :estado")
    BigDecimal sumSueldoBaseByTenantIdAndEstado(@Param("tenantId") Long tenantId, @Param("estado") Payroll.PayrollStatus estado);
}
