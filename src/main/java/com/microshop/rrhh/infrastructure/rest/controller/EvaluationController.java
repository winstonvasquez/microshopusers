package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.EvaluationCommandService;
import com.microshop.rrhh.application.dto.evaluation.*;
import com.microshop.rrhh.application.query.EvaluationQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.EVALUATIONS)
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "Gestión de evaluaciones de desempeño")
@SecurityRequirement(name = "bearer-key")
public class EvaluationController {

    private final EvaluationCommandService evaluationCommandService;
    private final EvaluationQueryService evaluationQueryService;

    @GetMapping
    @Operation(summary = "Listar todas las evaluaciones")
    public ResponseEntity<List<EvaluationResponseDto>> getAll() {
        return ResponseEntity.ok(evaluationQueryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evaluación por ID")
    public ResponseEntity<EvaluationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationQueryService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar evaluaciones de un empleado")
    public ResponseEntity<List<EvaluationResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(evaluationQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/period/{periodo}")
    @Operation(summary = "Listar evaluaciones por periodo")
    public ResponseEntity<List<EvaluationResponseDto>> getByPeriod(@PathVariable String periodo) {
        return ResponseEntity.ok(evaluationQueryService.getByPeriod(periodo));
    }

    @GetMapping("/evaluador/{evaluadorId}")
    @Operation(summary = "Listar evaluaciones asignadas a un evaluador")
    public ResponseEntity<List<EvaluationResponseDto>> getByEvaluador(@PathVariable Long evaluadorId) {
        return ResponseEntity.ok(evaluationQueryService.getByEvaluador(evaluadorId));
    }

    @PostMapping
    @Operation(summary = "Crear evaluación")
    public ResponseEntity<EvaluationResponseDto> create(@Valid @RequestBody EvaluationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationCommandService.createEvaluation(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evaluación")
    public ResponseEntity<EvaluationResponseDto> update(@PathVariable Long id, @Valid @RequestBody EvaluationRequestDto request) {
        return ResponseEntity.ok(evaluationCommandService.updateEvaluation(id, request));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Completar evaluación")
    public ResponseEntity<EvaluationResponseDto> complete(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.completeEvaluation(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar evaluación")
    public ResponseEntity<EvaluationResponseDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.approveEvaluation(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar evaluación")
    public ResponseEntity<EvaluationResponseDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.cancelEvaluation(id));
    }

    // ── Criteria ────────────────────────────────────────────────────────────

    @GetMapping("/criteria")
    @Operation(summary = "Listar todos los criterios de evaluación")
    public ResponseEntity<List<EvaluationCriteriaResponseDto>> getAllCriteria() {
        return ResponseEntity.ok(evaluationQueryService.getAllCriteria());
    }

    @GetMapping("/criteria/active")
    @Operation(summary = "Listar criterios activos")
    public ResponseEntity<List<EvaluationCriteriaResponseDto>> getActiveCriteria() {
        return ResponseEntity.ok(evaluationQueryService.getActiveCriteria());
    }

    @GetMapping("/criteria/{id}")
    @Operation(summary = "Obtener criterio por ID")
    public ResponseEntity<EvaluationCriteriaResponseDto> getCriteriaById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationQueryService.getCriteriaById(id));
    }

    @PostMapping("/criteria")
    @Operation(summary = "Crear criterio de evaluación")
    public ResponseEntity<EvaluationCriteriaResponseDto> createCriteria(@Valid @RequestBody EvaluationCriteriaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationCommandService.createCriteria(request));
    }

    @PutMapping("/criteria/{id}")
    @Operation(summary = "Actualizar criterio de evaluación")
    public ResponseEntity<EvaluationCriteriaResponseDto> updateCriteria(@PathVariable Long id, @Valid @RequestBody EvaluationCriteriaRequestDto request) {
        return ResponseEntity.ok(evaluationCommandService.updateCriteria(id, request));
    }

    @PatchMapping("/criteria/{id}/deactivate")
    @Operation(summary = "Desactivar criterio de evaluación")
    public ResponseEntity<Void> deactivateCriteria(@PathVariable Long id) {
        evaluationCommandService.deactivateCriteria(id);
        return ResponseEntity.noContent().build();
    }
}
