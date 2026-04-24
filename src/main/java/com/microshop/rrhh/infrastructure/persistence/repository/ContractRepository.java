package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByTenantId(Long tenantId);

    List<Contract> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);

    Optional<Contract> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId AND c.employee.id = :employeeId AND c.estado = 'ACTIVO'")
    Optional<Contract> findActiveByEmployeeId(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);

    List<Contract> findByTenantIdAndEstado(Long tenantId, Contract.ContractStatus estado);

    @Query("SELECT c FROM Contract c WHERE c.tenantId = :tenantId AND c.fechaFin IS NOT NULL AND c.fechaFin <= :fecha AND c.estado = 'ACTIVO'")
    List<Contract> findExpiringBefore(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndEstado(Long tenantId, Contract.ContractStatus estado);
}
