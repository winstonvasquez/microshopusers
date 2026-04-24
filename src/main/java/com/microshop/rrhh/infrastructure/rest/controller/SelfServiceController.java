package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.dto.attendance.AttendanceResponseDto;
import com.microshop.rrhh.application.dto.evaluation.EvaluationResponseDto;
import com.microshop.rrhh.application.dto.evaluation.GoalResponseDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.dto.training.TrainingParticipationResponseDto;
import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.query.*;
import com.microshop.rrhh.application.command.VacationCommandService;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

import java.util.List;

/**
 * Portal de autoservicio para empleados.
 * Todos los endpoints están scoped al empleado vinculado al usuario autenticado.
 */
@RestController
@RequestMapping(ApiPaths.SELF_SERVICE)
@RequiredArgsConstructor
@Tag(name = "Self-Service", description = "Portal de autoservicio del empleado")
@SecurityRequirement(name = "bearer-key")
public class SelfServiceController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeQueryService employeeQueryService;
    private final PayrollQueryService payrollQueryService;
    private final VacationQueryService vacationQueryService;
    private final VacationCommandService vacationCommandService;
    private final AttendanceQueryService attendanceQueryService;
    private final EvaluationQueryService evaluationQueryService;
    private final GoalQueryService goalQueryService;
    private final TrainingQueryService trainingQueryService;
    private final TenantContext tenantContext;

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil del empleado actual")
    public ResponseEntity<EmployeeResponseDto> getMyProfile() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(employeeQueryService.getEmployeeById(employeeId));
    }

    @GetMapping("/payslips")
    @Operation(summary = "Obtener boletas de pago del empleado actual")
    public ResponseEntity<List<PayrollResponseDto>> getMyPayslips() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(payrollQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/vacations")
    @Operation(summary = "Obtener solicitudes de vacaciones del empleado actual")
    public ResponseEntity<List<VacationResponseDto>> getMyVacations() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(vacationQueryService.getByEmployee(employeeId));
    }

    @PostMapping("/vacations")
    @Operation(summary = "Crear solicitud de vacaciones")
    public ResponseEntity<VacationResponseDto> requestVacation(@Valid @RequestBody VacationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacationCommandService.createVacationRequest(request));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Obtener asistencia del empleado actual")
    public ResponseEntity<List<AttendanceResponseDto>> getMyAttendance(
            @RequestParam String month) {
        Long employeeId = resolveCurrentEmployeeId();
        YearMonth ym = YearMonth.parse(month);
        return ResponseEntity.ok(attendanceQueryService.getMonthlyReport(employeeId, ym));
    }

    @GetMapping("/evaluations")
    @Operation(summary = "Obtener evaluaciones del empleado actual")
    public ResponseEntity<List<EvaluationResponseDto>> getMyEvaluations() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(evaluationQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/goals")
    @Operation(summary = "Obtener metas del empleado actual")
    public ResponseEntity<List<GoalResponseDto>> getMyGoals() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(goalQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/trainings")
    @Operation(summary = "Obtener capacitaciones del empleado actual")
    public ResponseEntity<List<TrainingParticipationResponseDto>> getMyTrainings() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(trainingQueryService.getParticipationsByEmployee(employeeId));
    }

    private Long resolveCurrentEmployeeId() {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();
        if (userId == null) {
            throw new NotFoundException("Usuario no autenticado");
        }
        return employeeRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(e -> e.getId())
                .orElseThrow(() -> new NotFoundException("No se encontró un empleado vinculado al usuario actual"));
    }
}
