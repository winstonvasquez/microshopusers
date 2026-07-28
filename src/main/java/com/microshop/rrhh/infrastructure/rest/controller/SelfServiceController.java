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
import com.microshop.rrhh.domain.model.Attendance;
import com.microshop.rrhh.domain.model.Payroll;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

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

    private final EmployeeQueryService employeeQueryService;
    private final PayrollQueryService payrollQueryService;
    private final VacationQueryService vacationQueryService;
    private final VacationCommandService vacationCommandService;
    private final AttendanceQueryService attendanceQueryService;
    private final EvaluationQueryService evaluationQueryService;
    private final GoalQueryService goalQueryService;
    private final TrainingQueryService trainingQueryService;

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil del empleado actual")
    public ResponseEntity<EmployeeResponseDto> getMyProfile() {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(employeeQueryService.getEmployeeById(employeeId));
    }

    @GetMapping("/payslips")
    @Operation(summary = "Obtener boletas de pago del empleado actual, con filtro opcional de estado")
    public ResponseEntity<List<PayrollResponseDto>> getMyPayslips(
            @RequestParam(required = false) Payroll.PayrollStatus estado) {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(payrollQueryService.getByEmployee(employeeId, estado));
    }

    @GetMapping("/vacations")
    @Operation(summary = "Obtener solicitudes de vacaciones del empleado actual, con filtro opcional de estado")
    public ResponseEntity<List<VacationResponseDto>> getMyVacations(
            @RequestParam(required = false) VacationRequest.VacationStatus estado) {
        Long employeeId = resolveCurrentEmployeeId();
        return ResponseEntity.ok(vacationQueryService.getByEmployee(employeeId, estado));
    }

    @PostMapping("/vacations")
    @Operation(summary = "Crear solicitud de vacaciones propia. El employeeId del body se ignora: "
            + "la solicitud se registra siempre a nombre del empleado resuelto del JWT.")
    public ResponseEntity<VacationResponseDto> requestVacation(@Valid @RequestBody VacationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vacationCommandService.createOwnVacationRequest(request));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Obtener asistencia del empleado actual. Acepta 'month' (legado) O un rango "
            + "fechaDesde/fechaHasta + tipoRegistro (filtros avanzados); si se envía el rango, este tiene prioridad.")
    public ResponseEntity<List<AttendanceResponseDto>> getMyAttendance(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Attendance.AttendanceType tipoRegistro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        Long employeeId = resolveCurrentEmployeeId();
        if (fechaDesde != null || fechaHasta != null || tipoRegistro != null) {
            return ResponseEntity.ok(attendanceQueryService.getMyAttendanceFiltered(employeeId, tipoRegistro, fechaDesde, fechaHasta));
        }
        if (month == null || month.isBlank()) {
            throw new BusinessException("Debe indicar el parámetro 'month' con formato YYYY-MM");
        }
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException("El parámetro 'month' debe tener formato YYYY-MM");
        }
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
        return employeeQueryService.resolveCurrentEmployeeId();
    }
}
