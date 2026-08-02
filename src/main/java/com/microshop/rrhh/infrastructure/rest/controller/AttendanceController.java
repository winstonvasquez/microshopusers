package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.AttendanceCommandService;
import com.microshop.rrhh.application.dto.attendance.*;
import com.microshop.rrhh.application.query.AttendanceQueryService;
import com.microshop.rrhh.domain.model.Attendance;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.util.SpreadsheetExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/paged")
    @Operation(summary = "Listar asistencia paginada con filtros avanzados (búsqueda, empleado, departamento, "
            + "tipo de registro, aprobador y rango de fechas) — server-side, reemplaza el filtrado client-side")
    public ResponseEntity<Page<AttendanceResponseDto>> getAttendancePaged(
            @PageableDefault(size = 20, sort = "fecha", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Attendance.AttendanceType tipoRegistro,
            @RequestParam(required = false) Long aprobadoPorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity.ok(attendanceQueryService.getAttendancePaged(
                search, employeeId, departmentId, tipoRegistro, aprobadoPorId, fechaDesde, fechaHasta, pageable));
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

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/export")
    @Operation(summary = "Exportar asistencia a XLSX o CSV (generado en el backend, datos limpios sin HTML), "
            + "con los mismos filtros avanzados que /paged")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Attendance.AttendanceType tipo,
            @RequestParam(required = false) Long aprobadoPorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        // Mismos filtros que /paged (search+empleado+departamento+tipo+aprobador+rango de fechas), sin paginación real.
        List<AttendanceResponseDto> registros = attendanceQueryService.getAttendanceForExport(
                search, employeeId, departmentId, tipo, aprobadoPorId, fechaDesde, fechaHasta);

        List<String> cabeceras = List.of("Empleado", "Fecha", "Entrada", "Salida", "Tipo", "Observaciones");
        List<List<Object>> filas = registros.stream()
                .map(a -> List.<Object>of(
                        valorOVacio(a.employeeName()),
                        a.fecha() != null ? a.fecha().format(FECHA_FORMATTER) : "",
                        a.horaEntrada() != null ? a.horaEntrada().toString() : "—",
                        a.horaSalida() != null ? a.horaSalida().toString() : "—",
                        a.tipoRegistro() != null ? a.tipoRegistro().name() : "",
                        a.observaciones() != null ? SpreadsheetExporter.plano(a.observaciones()) : "—"))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "asistencia.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Asistencia", cabeceras, filas);
            filename = "asistencia.xlsx";
            contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(bytes);
    }

    /** Retorna cadena vacía si el valor es null, para no propagar "null" literal a la exportación. */
    private static String valorOVacio(String valor) {
        return valor != null ? valor : "";
    }
}
