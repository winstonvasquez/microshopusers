package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.PayrollDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long> {

    List<PayrollDetail> findByPayrollId(Long payrollId);
}
