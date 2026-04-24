package com.microshop.rrhh.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.application.dto.evaluation.*;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class EvaluationCommandService {

    private final PerformanceEvaluationRepository evaluationRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final EvaluationDetailRepository detailRepository;
    private final EmployeeRepository employeeRepository;
    private final EvaluationMapper evaluationMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public EvaluationResponseDto createEvaluation(@Valid EvaluationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("evaluation.employee.not.found", "Empleado no encontrado")));

        Employee evaluador = employeeRepository.findByIdAndTenantId(request.evaluadorId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("evaluation.evaluador.not.found", "Evaluador no encontrado")));

        PerformanceEvaluation evaluation = evaluationMapper.toEntity(request, tenantId, employee, evaluador);

        if (request.details() != null && !request.details().isEmpty()) {
            for (EvaluationDetailRequestDto detailDto : request.details()) {
                EvaluationCriteria criteria = criteriaRepository.findByIdAndTenantId(detailDto.criteriaId(), tenantId)
                        .orElseThrow(() -> new NotFoundException("Criterio no encontrado: " + detailDto.criteriaId()));

                EvaluationDetail detail = EvaluationDetail.builder()
                        .tenantId(tenantId)
                        .evaluation(evaluation)
                        .criteria(criteria)
                        .puntaje(detailDto.puntaje())
                        .comentarios(detailDto.comentarios())
                        .build();
                evaluation.getDetails().add(detail);
            }
        }

        PerformanceEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluación creada: {} - Empleado: {} - Periodo: {} - Tenant: {}",
                saved.getId(), request.employeeId(), request.periodo(), tenantId);
        return evaluationMapper.toDto(saved);
    }

    public EvaluationResponseDto updateEvaluation(Long id, @Valid EvaluationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        PerformanceEvaluation evaluation = evaluationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada"));

        if (evaluation.getEstado() != PerformanceEvaluation.EvaluationStatus.BORRADOR) {
            throw new BusinessException("Solo se pueden editar evaluaciones en estado BORRADOR");
        }

        evaluationMapper.updateEntity(evaluation, request);

        if (request.details() != null) {
            evaluation.getDetails().clear();
            for (EvaluationDetailRequestDto detailDto : request.details()) {
                EvaluationCriteria criteria = criteriaRepository.findByIdAndTenantId(detailDto.criteriaId(), tenantId)
                        .orElseThrow(() -> new NotFoundException("Criterio no encontrado: " + detailDto.criteriaId()));

                EvaluationDetail detail = EvaluationDetail.builder()
                        .tenantId(tenantId)
                        .evaluation(evaluation)
                        .criteria(criteria)
                        .puntaje(detailDto.puntaje())
                        .comentarios(detailDto.comentarios())
                        .build();
                evaluation.getDetails().add(detail);
            }
        }

        PerformanceEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluación actualizada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toDto(saved);
    }

    public EvaluationResponseDto completeEvaluation(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        PerformanceEvaluation evaluation = evaluationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada"));

        if (evaluation.getEstado() != PerformanceEvaluation.EvaluationStatus.BORRADOR) {
            throw new BusinessException("Solo se pueden completar evaluaciones en estado BORRADOR");
        }

        evaluation.setEstado(PerformanceEvaluation.EvaluationStatus.COMPLETADA);
        PerformanceEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluación completada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toDto(saved);
    }

    public EvaluationResponseDto approveEvaluation(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        PerformanceEvaluation evaluation = evaluationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada"));

        if (evaluation.getEstado() != PerformanceEvaluation.EvaluationStatus.COMPLETADA) {
            throw new BusinessException("Solo se pueden aprobar evaluaciones en estado COMPLETADA");
        }

        evaluation.setEstado(PerformanceEvaluation.EvaluationStatus.APROBADA);
        PerformanceEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluación aprobada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toDto(saved);
    }

    public EvaluationResponseDto cancelEvaluation(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        PerformanceEvaluation evaluation = evaluationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada"));

        evaluation.setEstado(PerformanceEvaluation.EvaluationStatus.CANCELADA);
        PerformanceEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluación cancelada: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toDto(saved);
    }

    // ── Criteria ────────────────────────────────────────────────────────────

    public EvaluationCriteriaResponseDto createCriteria(@Valid EvaluationCriteriaRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        EvaluationCriteria criteria = evaluationMapper.toCriteriaEntity(request, tenantId);
        EvaluationCriteria saved = criteriaRepository.save(criteria);
        log.info("Criterio de evaluación creado: {} - Tenant: {}", saved.getId(), tenantId);
        return evaluationMapper.toCriteriaDto(saved);
    }

    public EvaluationCriteriaResponseDto updateCriteria(Long id, @Valid EvaluationCriteriaRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        EvaluationCriteria criteria = criteriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Criterio no encontrado"));

        criteria.setNombre(request.nombre());
        criteria.setDescripcion(request.descripcion());
        criteria.setPesoPorcentaje(request.pesoPorcentaje());
        if (request.puntajeMinimo() != null) criteria.setPuntajeMinimo(request.puntajeMinimo());
        if (request.puntajeMaximo() != null) criteria.setPuntajeMaximo(request.puntajeMaximo());

        EvaluationCriteria saved = criteriaRepository.save(criteria);
        log.info("Criterio actualizado: {} - Tenant: {}", id, tenantId);
        return evaluationMapper.toCriteriaDto(saved);
    }

    public void deactivateCriteria(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        EvaluationCriteria criteria = criteriaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Criterio no encontrado"));
        criteria.setActivo(false);
        criteriaRepository.save(criteria);
        log.info("Criterio desactivado: {} - Tenant: {}", id, tenantId);
    }
}
