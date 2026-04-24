package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.TrainingCommandService;
import com.microshop.rrhh.application.dto.training.*;
import com.microshop.rrhh.application.query.TrainingQueryService;
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
@RequestMapping(ApiPaths.TRAININGS)
@RequiredArgsConstructor
@Tag(name = "Trainings", description = "Gestión de capacitaciones")
@SecurityRequirement(name = "bearer-key")
public class TrainingController {

    private final TrainingCommandService trainingCommandService;
    private final TrainingQueryService trainingQueryService;

    @GetMapping
    @Operation(summary = "Listar todas las capacitaciones")
    public ResponseEntity<List<TrainingResponseDto>> getAll() {
        return ResponseEntity.ok(trainingQueryService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener capacitación por ID")
    public ResponseEntity<TrainingResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingQueryService.getById(id));
    }

    @GetMapping("/status/{estado}")
    @Operation(summary = "Listar capacitaciones por estado")
    public ResponseEntity<List<TrainingResponseDto>> getByStatus(@PathVariable String estado) {
        return ResponseEntity.ok(trainingQueryService.getByStatus(estado));
    }

    @PostMapping
    @Operation(summary = "Crear capacitación")
    public ResponseEntity<TrainingResponseDto> create(@Valid @RequestBody TrainingRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingCommandService.createTraining(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar capacitación")
    public ResponseEntity<TrainingResponseDto> update(@PathVariable Long id, @Valid @RequestBody TrainingRequestDto request) {
        return ResponseEntity.ok(trainingCommandService.updateTraining(id, request));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Iniciar capacitación")
    public ResponseEntity<TrainingResponseDto> start(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCommandService.startTraining(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Completar capacitación")
    public ResponseEntity<TrainingResponseDto> complete(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCommandService.completeTraining(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar capacitación")
    public ResponseEntity<TrainingResponseDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCommandService.cancelTraining(id));
    }

    // ── Participación ───────────────────────────────────────────────────────

    @GetMapping("/{trainingId}/participants")
    @Operation(summary = "Listar participantes de una capacitación")
    public ResponseEntity<List<TrainingParticipationResponseDto>> getParticipants(@PathVariable Long trainingId) {
        return ResponseEntity.ok(trainingQueryService.getParticipantsByTraining(trainingId));
    }

    @GetMapping("/employee/{employeeId}/participations")
    @Operation(summary = "Listar capacitaciones de un empleado")
    public ResponseEntity<List<TrainingParticipationResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(trainingQueryService.getParticipationsByEmployee(employeeId));
    }

    @PostMapping("/participants")
    @Operation(summary = "Inscribir participante")
    public ResponseEntity<TrainingParticipationResponseDto> enroll(@Valid @RequestBody TrainingParticipationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingCommandService.enrollParticipant(request));
    }

    @PutMapping("/participants/{id}")
    @Operation(summary = "Actualizar participación")
    public ResponseEntity<TrainingParticipationResponseDto> updateParticipation(@PathVariable Long id, @Valid @RequestBody TrainingParticipationRequestDto request) {
        return ResponseEntity.ok(trainingCommandService.updateParticipation(id, request));
    }

    @PostMapping("/participants/{id}/certificate")
    @Operation(summary = "Emitir certificado")
    public ResponseEntity<TrainingParticipationResponseDto> issueCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCommandService.issueCertificate(id));
    }
}
