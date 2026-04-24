package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.evaluation.*;
import com.microshop.rrhh.domain.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class EvaluationMapper {

    public PerformanceEvaluation toEntity(EvaluationRequestDto dto, Long tenantId, Employee employee, Employee evaluador) {
        PerformanceEvaluation eval = PerformanceEvaluation.builder()
                .tenantId(tenantId)
                .employee(employee)
                .evaluador(evaluador)
                .periodo(dto.periodo())
                .tipoEvaluacion(dto.tipoEvaluacion() != null
                        ? PerformanceEvaluation.EvaluationType.valueOf(dto.tipoEvaluacion())
                        : PerformanceEvaluation.EvaluationType.ANUAL)
                .puntaje(dto.puntaje())
                .comentarios(dto.comentarios())
                .fortalezas(dto.fortalezas())
                .areasMejora(dto.areasMejora())
                .planMejora(dto.planMejora())
                .fechaEvaluacion(dto.fechaEvaluacion())
                .proximaRevision(dto.proximaRevision())
                .build();
        return eval;
    }

    public void updateEntity(PerformanceEvaluation entity, EvaluationRequestDto dto) {
        entity.setPuntaje(dto.puntaje());
        entity.setComentarios(dto.comentarios());
        entity.setFortalezas(dto.fortalezas());
        entity.setAreasMejora(dto.areasMejora());
        entity.setPlanMejora(dto.planMejora());
        entity.setProximaRevision(dto.proximaRevision());
        if (dto.tipoEvaluacion() != null) {
            entity.setTipoEvaluacion(PerformanceEvaluation.EvaluationType.valueOf(dto.tipoEvaluacion()));
        }
    }

    public EvaluationResponseDto toDto(PerformanceEvaluation entity) {
        String employeeName = null;
        String evaluadorName = null;
        try {
            if (entity.getEmployee() != null) {
                employeeName = entity.getEmployee().getNombres() + " " + entity.getEmployee().getApellidos();
            }
        } catch (Exception ignored) {}
        try {
            if (entity.getEvaluador() != null) {
                evaluadorName = entity.getEvaluador().getNombres() + " " + entity.getEvaluador().getApellidos();
            }
        } catch (Exception ignored) {}

        List<EvaluationDetailResponseDto> details = Collections.emptyList();
        try {
            if (entity.getDetails() != null && !entity.getDetails().isEmpty()) {
                details = entity.getDetails().stream().map(this::toDetailDto).toList();
            }
        } catch (Exception ignored) {}

        return EvaluationResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(employeeName)
                .periodo(entity.getPeriodo())
                .evaluadorId(entity.getEvaluador() != null ? entity.getEvaluador().getId() : null)
                .evaluadorName(evaluadorName)
                .tipoEvaluacion(entity.getTipoEvaluacion() != null ? entity.getTipoEvaluacion().name() : null)
                .puntaje(entity.getPuntaje())
                .comentarios(entity.getComentarios())
                .fortalezas(entity.getFortalezas())
                .areasMejora(entity.getAreasMejora())
                .planMejora(entity.getPlanMejora())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .fechaEvaluacion(entity.getFechaEvaluacion())
                .proximaRevision(entity.getProximaRevision())
                .details(details)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public EvaluationDetailResponseDto toDetailDto(EvaluationDetail detail) {
        String criteriaName = null;
        BigDecimal peso = null;
        try {
            if (detail.getCriteria() != null) {
                criteriaName = detail.getCriteria().getNombre();
                peso = detail.getCriteria().getPesoPorcentaje();
            }
        } catch (Exception ignored) {}

        return EvaluationDetailResponseDto.builder()
                .id(detail.getId())
                .criteriaId(detail.getCriteria() != null ? detail.getCriteria().getId() : null)
                .criteriaName(criteriaName)
                .pesoPorcentaje(peso)
                .puntaje(detail.getPuntaje())
                .comentarios(detail.getComentarios())
                .build();
    }

    // ── Criteria ────────────────────────────────────────────────────────────

    public EvaluationCriteria toCriteriaEntity(EvaluationCriteriaRequestDto dto, Long tenantId) {
        return EvaluationCriteria.builder()
                .tenantId(tenantId)
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .pesoPorcentaje(dto.pesoPorcentaje())
                .puntajeMinimo(dto.puntajeMinimo() != null ? dto.puntajeMinimo() : BigDecimal.ZERO)
                .puntajeMaximo(dto.puntajeMaximo() != null ? dto.puntajeMaximo() : new BigDecimal("100"))
                .build();
    }

    public EvaluationCriteriaResponseDto toCriteriaDto(EvaluationCriteria entity) {
        return EvaluationCriteriaResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .pesoPorcentaje(entity.getPesoPorcentaje())
                .puntajeMinimo(entity.getPuntajeMinimo())
                .puntajeMaximo(entity.getPuntajeMaximo())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // ── Goals ───────────────────────────────────────────────────────────────

    public Goal toGoalEntity(GoalRequestDto dto, Long tenantId, Employee employee, Employee asignadoPor) {
        return Goal.builder()
                .tenantId(tenantId)
                .employee(employee)
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .prioridad(dto.prioridad() != null ? Goal.Priority.valueOf(dto.prioridad()) : Goal.Priority.MEDIA)
                .porcentajeAvance(dto.porcentajeAvance() != null ? dto.porcentajeAvance() : BigDecimal.ZERO)
                .asignadoPor(asignadoPor)
                .build();
    }

    public GoalResponseDto toGoalDto(Goal entity) {
        String employeeName = null;
        String asignadoPorName = null;
        try {
            if (entity.getEmployee() != null) {
                employeeName = entity.getEmployee().getNombres() + " " + entity.getEmployee().getApellidos();
            }
        } catch (Exception ignored) {}
        try {
            if (entity.getAsignadoPor() != null) {
                asignadoPorName = entity.getAsignadoPor().getNombres() + " " + entity.getAsignadoPor().getApellidos();
            }
        } catch (Exception ignored) {}

        return GoalResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(employeeName)
                .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .porcentajeAvance(entity.getPorcentajeAvance())
                .prioridad(entity.getPrioridad() != null ? entity.getPrioridad().name() : null)
                .asignadoPorId(entity.getAsignadoPor() != null ? entity.getAsignadoPor().getId() : null)
                .asignadoPorName(asignadoPorName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
