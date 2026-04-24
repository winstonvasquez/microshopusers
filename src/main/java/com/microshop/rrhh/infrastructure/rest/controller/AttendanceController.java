package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.AttendanceCommandService;
import com.microshop.rrhh.application.dto.attendance.*;
import com.microshop.rrhh.application.query.AttendanceQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
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
import java.util.List;

@RestController
@RequestMapping(ApiPaths.ATTENDANCE)
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Gestión de asistencia")
@SecurityRequirement(name = "bearer-key")
public class AttendanceController {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    @PostMapping
    @Operation(summary = "Registrar asistencia manualmente")
    public ResponseEntity<AttendanceResponseDto> registerAttendance(@Valid @RequestBody AttendanceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceCommandService.registerAttendance(request));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Marcar entrada (check-in)")
    public ResponseEntity<AttendanceResponseDto> checkIn(@Valid @RequestBody CheckInOutDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceCommandService.checkIn(request));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Marcar salida (check-out)")
    public ResponseEntity<AttendanceResponseDto> checkOut(@Valid @RequestBody CheckInOutDto request) {
        return ResponseEntity.ok(attendanceCommandService.checkOut(request));
    }

    @GetMapping("/date/{fecha}")
    @Operation(summary = "Listar asistencia por fecha")
    public ResponseEntity<List<AttendanceResponseDto>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(attendanceQueryService.getAttendanceByDate(fecha));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar asistencia de un empleado")
    public ResponseEntity<List<AttendanceResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceQueryService.getAttendanceByEmployee(employeeId));
    }

    @GetMapping("/report")
    @Operation(summary = "Reporte mensual de un empleado")
    public ResponseEntity<List<AttendanceResponseDto>> getMonthlyReport(
            @RequestParam Long employeeId,
            @RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return ResponseEntity.ok(attendanceQueryService.getMonthlyReport(employeeId, ym));
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen mensual por empleado")
    public ResponseEntity<List<AttendanceSummaryDto>> getMonthlySummary(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return ResponseEntity.ok(attendanceQueryService.getMonthlySummary(ym));
    }
}
