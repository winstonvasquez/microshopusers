package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.mapper.VacationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.infrastructure.persistence.repository.VacationRequestRepository;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VacationQueryService {

    private final VacationRequestRepository vacationRequestRepository;
    private final VacationMapper vacationMapper;
    private final TenantContext tenantContext;

    /** Overload corto (compatibilidad): delega a la versión con todos los filtros con nulls. */
    public Page<VacationResponseDto> getVacationsPaged(String search, VacationRequest.VacationStatus estado, Pageable pageable) {
        return getVacationsPaged(search, estado, null, null, null, null,
                null, null, null, null, null, null, pageable);
    }

    /**
     * Listado paginado server-side con TODOS los filtros avanzados (2026-07-27): búsqueda, estado,
     * tipo de vacación, empleado, departamento, aprobador y rangos de fecha de inicio/fin/aprobación.
     */
    public Page<VacationResponseDto> getVacationsPaged(String search, VacationRequest.VacationStatus estado,
                                                        VacationRequest.VacationType tipoVacacion,
                                                        Long employeeId, Long departmentId, Long aprobadoPorId,
                                                        LocalDate fechaInicioDesde, LocalDate fechaInicioHasta,
                                                        LocalDate fechaFinDesde, LocalDate fechaFinHasta,
                                                        LocalDate fechaAprobacionDesde, LocalDate fechaAprobacionHasta,
                                                        Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        return vacationRequestRepository.searchPaged(tenantId, term, estado, tipoVacacion, employeeId,
                        departmentId, aprobadoPorId, fechaInicioDesde, fechaInicioHasta,
                        fechaFinDesde, fechaFinHasta, fechaAprobacionDesde, fechaAprobacionHasta, pageable)
                .map(vacationMapper::toDto);
    }

    public List<VacationResponseDto> getAllVacations() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantId(tenantId).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public List<VacationResponseDto> getByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    /** Autoservicio "Mis Vacaciones": ver {@link #getByEmployee(Long)} con filtro opcional de estado. */
    public List<VacationResponseDto> getByEmployee(Long employeeId, VacationRequest.VacationStatus estado) {
        Long tenantId = tenantContext.getCurrentTenantId();
        List<VacationRequest> solicitudes = estado != null
                ? vacationRequestRepository.findByTenantIdAndEmployee_IdAndEstado(tenantId, employeeId, estado)
                : vacationRequestRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId);
        return solicitudes.stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public List<VacationResponseDto> getPending() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return vacationRequestRepository.findByTenantIdAndEstado(tenantId, VacationRequest.VacationStatus.SOLICITADO).stream()
                .map(vacationMapper::toDto)
                .toList();
    }

    public long countPending() {
        return vacationRequestRepository.countByTenantIdAndEstado(
                tenantContext.getCurrentTenantId(), VacationRequest.VacationStatus.SOLICITADO);
    }
}
