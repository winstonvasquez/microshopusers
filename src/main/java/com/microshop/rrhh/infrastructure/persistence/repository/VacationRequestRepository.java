package com.microshop.rrhh.infrastructure.persistence.repository;

import com.microshop.rrhh.domain.model.VacationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT v FROM VacationRequest v WHERE v.tenantId = :tenantId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "  LOWER(v.employee.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(v.employee.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR v.estado = :estado)")
    Page<VacationRequest> searchPaged(@Param("tenantId") Long tenantId,
                                      @Param("search") String search,
                                      @Param("estado") VacationRequest.VacationStatus estado,
                                      Pageable pageable);
}
