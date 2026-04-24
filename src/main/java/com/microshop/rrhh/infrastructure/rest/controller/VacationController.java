package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.LeaveBalanceCommandService;
import com.microshop.rrhh.application.command.VacationCommandService;
import com.microshop.rrhh.application.dto.vacation.LeaveBalanceDto;
import com.microshop.rrhh.application.dto.vacation.VacationApprovalDto;
import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.query.LeaveBalanceQueryService;
import com.microshop.rrhh.application.query.VacationQueryService;
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
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.VACATIONS)
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "Gestión de vacaciones y balance")
@SecurityRequirement(name = "bearer-key")
public class VacationController {

    private final VacationCommandService vacationCommandService;
    private final VacationQueryService vacationQueryService;
    private final LeaveBalanceCommandService leaveBalanceCommandService;
    private final LeaveBalanceQueryService leaveBalanceQueryService;

    // ── Vacation Requests ────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes de vacaciones")
    public ResponseEntity<List<VacationResponseDto>> getAllVacations() {
        return ResponseEntity.ok(vacationQueryService.getAllVacations());
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar solicitudes de un empleado")
    public ResponseEntity<List<VacationResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(vacationQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar solicitudes pendientes")
    public ResponseEntity<List<VacationResponseDto>> getPending() {
        return ResponseEntity.ok(vacationQueryService.getPending());
    }

    @PostMapping
    @Operation(summary = "Crear solicitud de vacaciones")
    public ResponseEntity<VacationResponseDto> createVacationRequest(@Valid @RequestBody VacationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacationCommandService.createVacationRequest(request));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Aprobar o rechazar solicitud de vacaciones")
    public ResponseEntity<VacationResponseDto> approveOrRejectVacation(
            @PathVariable Long id,
            @Valid @RequestBody VacationApprovalDto approval) {
        return ResponseEntity.ok(vacationCommandService.approveOrRejectVacation(id, approval));
    }

    // ── Leave Balance ────────────────────────────────────────────────────────

    @GetMapping("/balance/{employeeId}")
    @Operation(summary = "Obtener balance de vacaciones de un empleado para el año actual")
    public ResponseEntity<LeaveBalanceDto> getBalance(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : java.time.Year.now().getValue();
        return leaveBalanceQueryService.getBalance(employeeId, y)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/balance")
    @Operation(summary = "Listar balances de vacaciones por año")
    public ResponseEntity<List<LeaveBalanceDto>> getBalancesByYear(
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : java.time.Year.now().getValue();
        return ResponseEntity.ok(leaveBalanceQueryService.getBalancesByYear(y));
    }

    @PostMapping("/balance/generate")
    @Operation(summary = "Generar balance anual para todos los empleados activos")
    public ResponseEntity<Map<String, Integer>> generateAnnualBalance(
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : java.time.Year.now().getValue();
        int count = leaveBalanceCommandService.generateAnnualBalance(y);
        return ResponseEntity.ok(Map.of("generated", count));
    }
}
