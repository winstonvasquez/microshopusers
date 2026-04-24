package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.GoalCommandService;
import com.microshop.rrhh.application.dto.evaluation.GoalRequestDto;
import com.microshop.rrhh.application.dto.evaluation.GoalResponseDto;
import com.microshop.rrhh.application.query.GoalQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.GOALS)
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Gestión de metas y objetivos")
@SecurityRequirement(name = "bearer-key")
public class GoalController {

    private final GoalCommandService goalCommandService;
    private final GoalQueryService goalQueryService;

    @GetMapping
    @Operation(summary = "Listar todas las metas")
    public ResponseEntity<List<GoalResponseDto>> getAll() {
        return ResponseEntity.ok(goalQueryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener meta por ID")
    public ResponseEntity<GoalResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(goalQueryService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar metas de un empleado")
    public ResponseEntity<List<GoalResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(goalQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/status/{estado}")
    @Operation(summary = "Listar metas por estado")
    public ResponseEntity<List<GoalResponseDto>> getByStatus(@PathVariable String estado) {
        return ResponseEntity.ok(goalQueryService.getByStatus(estado));
    }

    @PostMapping
    @Operation(summary = "Crear meta")
    public ResponseEntity<GoalResponseDto> create(@Valid @RequestBody GoalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalCommandService.createGoal(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar meta")
    public ResponseEntity<GoalResponseDto> update(@PathVariable Long id, @Valid @RequestBody GoalRequestDto request) {
        return ResponseEntity.ok(goalCommandService.updateGoal(id, request));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Actualizar progreso de meta")
    public ResponseEntity<GoalResponseDto> updateProgress(@PathVariable Long id, @RequestParam BigDecimal porcentaje) {
        return ResponseEntity.ok(goalCommandService.updateProgress(id, porcentaje));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Completar meta")
    public ResponseEntity<GoalResponseDto> complete(@PathVariable Long id) {
        return ResponseEntity.ok(goalCommandService.completeGoal(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar meta")
    public ResponseEntity<GoalResponseDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(goalCommandService.cancelGoal(id));
    }
}
