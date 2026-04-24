package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.evaluation.GoalRequestDto;
import com.microshop.rrhh.application.dto.evaluation.GoalResponseDto;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Goal;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.GoalRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class GoalCommandService {

    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;
    private final EvaluationMapper evaluationMapper;
    private final TenantContext tenantContext;

    public GoalResponseDto createGoal(@Valid GoalRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        Employee asignadoPor = null;
        if (request.asignadoPorId() != null) {
            asignadoPor = employeeRepository.findByIdAndTenantId(request.asignadoPorId(), tenantId)
                    .orElseThrow(() -> new NotFoundException("Empleado asignador no encontrado"));
        }

        Goal goal = evaluationMapper.toGoalEntity(request, tenantId, employee, asignadoPor);
        Goal saved = goalRepository.save(goal);
        log.info("Meta creada: {} - Empleado: {} - Tenant: {}", saved.getId(), request.employeeId(), tenantId);
        return evaluationMapper.toGoalDto(saved);
    }

    public GoalResponseDto updateGoal(Long id, @Valid GoalRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Goal goal = goalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Meta no encontrada"));

        goal.setTitulo(request.titulo());
        goal.setDescripcion(request.descripcion());
        goal.setFechaInicio(request.fechaInicio());
        goal.setFechaFin(request.fechaFin());
        if (request.prioridad() != null) {
            goal.setPrioridad(Goal.Priority.valueOf(request.prioridad()));
        }
        if (request.porcentajeAvance() != null) {
            goal.setPorcentajeAvance(request.porcentajeAvance());
        }

        Goal saved = goalRepository.save(goal);
        log.info("Meta actualizada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toGoalDto(saved);
    }

    public GoalResponseDto updateProgress(Long id, BigDecimal porcentaje) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Goal goal = goalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Meta no encontrada"));

        goal.setPorcentajeAvance(porcentaje);
        if (porcentaje.compareTo(new BigDecimal("100")) >= 0) {
            goal.setEstado(Goal.GoalStatus.COMPLETADO);
        }

        Goal saved = goalRepository.save(goal);
        log.info("Progreso de meta actualizado: {} - {}% - Tenant: {}", id, porcentaje, tenantId);
        return evaluationMapper.toGoalDto(saved);
    }

    public GoalResponseDto completeGoal(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Goal goal = goalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Meta no encontrada"));

        goal.setEstado(Goal.GoalStatus.COMPLETADO);
        goal.setPorcentajeAvance(new BigDecimal("100"));
        Goal saved = goalRepository.save(goal);
        log.info("Meta completada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toGoalDto(saved);
    }

    public GoalResponseDto cancelGoal(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Goal goal = goalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Meta no encontrada"));

        goal.setEstado(Goal.GoalStatus.CANCELADO);
        Goal saved = goalRepository.save(goal);
        log.info("Meta cancelada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toGoalDto(saved);
    }
}
