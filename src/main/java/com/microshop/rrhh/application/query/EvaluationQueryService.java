package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.evaluation.*;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EvaluationQueryService {

    private final PerformanceEvaluationRepository evaluationRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final EvaluationMapper evaluationMapper;
    private final TenantContext tenantContext;

    public List<EvaluationResponseDto> getAll() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return evaluationRepository.findByTenantId(tenantId).stream()
                .map(evaluationMapper::toDto)
                .toList();
    }

    public EvaluationResponseDto getById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return evaluationRepository.findByIdAndTenantId(id, tenantId)
                .map(evaluationMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada"));
    }

    public List<EvaluationResponseDto> getByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return evaluationRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(evaluationMapper::toDto)
                .toList();
    }

    public List<EvaluationResponseDto> getByPeriod(String periodo) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return evaluationRepository.findByTenantIdAndPeriodo(tenantId, periodo).stream()
                .map(evaluationMapper::toDto)
                .toList();
    }

    public List<EvaluationResponseDto> getByEvaluador(Long evaluadorId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return evaluationRepository.findByTenantIdAndEvaluadorId(tenantId, evaluadorId).stream()
                .map(evaluationMapper::toDto)
                .toList();
    }

    // ── Criteria ────────────────────────────────────────────────────────────

    public List<EvaluationCriteriaResponseDto> getAllCriteria() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return criteriaRepository.findByTenantId(tenantId).stream()
                .map(evaluationMapper::toCriteriaDto)
                .toList();
    }

    public List<EvaluationCriteriaResponseDto> getActiveCriteria() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return criteriaRepository.findByTenantIdAndActivo(tenantId, true).stream()
                .map(evaluationMapper::toCriteriaDto)
                .toList();
    }

    public EvaluationCriteriaResponseDto getCriteriaById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return criteriaRepository.findByIdAndTenantId(id, tenantId)
                .map(evaluationMapper::toCriteriaDto)
                .orElseThrow(() -> new NotFoundException("Criterio no encontrado"));
    }
}
