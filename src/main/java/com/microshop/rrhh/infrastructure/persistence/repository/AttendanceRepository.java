package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByTenantId(Long tenantId);

    Optional<Attendance> findByIdAndTenantId(Long id, Long tenantId);

    List<Attendance> findByTenantIdAndEmployee_Id(Long tenantId, Long employeeId);

    List<Attendance> findByTenantIdAndFecha(Long tenantId, LocalDate fecha);

    Optional<Attendance> findByTenantIdAndEmployee_IdAndFecha(Long tenantId, Long employeeId, LocalDate fecha);

    @Query("SELECT a FROM Attendance a WHERE a.tenantId = :tenantId AND a.employee.id = :employeeId " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fecha DESC")
    List<Attendance> findByTenantIdAndEmployeeIdAndFechaBetween(
            @Param("tenantId") Long tenantId,
            @Param("employeeId") Long employeeId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    long countByTenantIdAndFecha(Long tenantId, LocalDate fecha);
}
