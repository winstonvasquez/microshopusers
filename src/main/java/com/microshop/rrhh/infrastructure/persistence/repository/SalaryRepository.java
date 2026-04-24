package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByTenantIdAndEmployeeIdOrderByFechaInicioDesc(Long tenantId, Long employeeId);

    Optional<Salary> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM Salary s WHERE s.tenantId = :tenantId AND s.employee.id = :employeeId " +
           "AND s.fechaFin IS NULL ORDER BY s.fechaInicio DESC")
    Optional<Salary> findCurrentSalary(@Param("tenantId") Long tenantId, @Param("employeeId") Long employeeId);
}
