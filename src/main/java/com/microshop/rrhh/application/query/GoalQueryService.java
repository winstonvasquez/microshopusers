package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.evaluation.GoalResponseDto;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Goal;
import com.microshop.rrhh.infrastructure.persistence.repository.GoalRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
