package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.evaluation.GoalResponseDto;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Goal;
import com.microshop.rrhh.infrastructure.persistence.repository.GoalRepository;
import com.microshop.users.shared.exception.NotFoundException;
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
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final EvaluationMapper evaluationMapper;
    private final TenantContext tenantContext;

    public List<GoalResponseDto> getAll() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return goalRepository.findByTenantId(tenantId).stream()
                .map(evaluationMapper::toGoalDto)
                .toList();
    }

    public GoalResponseDto getById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return goalRepository.findByIdAndTenantId(id, tenantId)
                .map(evaluationMapper::toGoalDto)
                .orElseThrow(() -> new NotFoundException("Meta no encontrada"));
    }

    public List<GoalResponseDto> getByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return goalRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(evaluationMapper::toGoalDto)
                .toList();
    }

    public List<GoalResponseDto> getByStatus(String estado) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return goalRepository.findByTenantIdAndEstado(tenantId, Goal.GoalStatus.valueOf(estado)).stream()
                .map(evaluationMapper::toGoalDto)
                .toList();
    }

    /**
     * Listado paginado con filtros avanzados: búsqueda por texto, estado, prioridad,
     * empleado, asignador, departamento y rangos de fecha de inicio/fin — reemplaza el
     * filtrado client-side que hacía goal-list.component.ts sobre la lista completa.
     */
    public Page<GoalResponseDto> getGoalsPaged(String search, Goal.GoalStatus estado, Goal.Priority prioridad,
                                                Long employeeId, Long asignadoPorId, Long departmentId,
                                                LocalDate fechaInicioDesde, LocalDate fechaInicioHasta,
                                                LocalDate fechaFinDesde, LocalDate fechaFinHasta,
                                                Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        return goalRepository.searchPaged(tenantId, term, estado, prioridad, employeeId, asignadoPorId,
                        departmentId, fechaInicioDesde, fechaInicioHasta, fechaFinDesde, fechaFinHasta, pageable)
                .map(evaluationMapper::toGoalDto);
    }
}
