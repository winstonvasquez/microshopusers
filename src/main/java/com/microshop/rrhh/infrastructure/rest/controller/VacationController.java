package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.LeaveBalanceCommandService;
import com.microshop.rrhh.application.command.VacationCommandService;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.dto.vacation.LeaveBalanceDto;
import com.microshop.rrhh.application.dto.vacation.VacationApprovalDto;
import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.query.EmployeeQueryService;
import com.microshop.rrhh.application.query.LeaveBalanceQueryService;
import com.microshop.rrhh.application.query.VacationQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
import com.microshop.users.shared.util.AppUtils;
import com.microshop.users.shared.util.SpreadsheetExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.microshop.rrhh.domain.model.VacationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.VACATIONS)
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "Gestión de vacaciones y balance")
@SecurityRequirement(name = "bearer-key")
public class VacationController {

    private final VacationCommandService vacationCommandService;
    private final VacationQueryService vacationQueryService;
    private final EmployeeQueryService employeeQueryService;

    @GetMapping("/paged")
    public ResponseEntity<Page<VacationResponseDto>> getVacationsPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VacationRequest.VacationStatus estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(vacationQueryService.getVacationsPaged(search, estado, pageable));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar solicitudes de vacaciones a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportVacations(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VacationRequest.VacationStatus estado) {
        // Trae TODAS las solicitudes que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("id").descending());
        List<VacationResponseDto> solicitudes = vacationQueryService.getVacationsPaged(search, estado, pageable).getContent();

        // Mapa employeeId -> nombre completo (una sola consulta de todos los empleados del tenant).
        Map<Long, String> nombresPorEmpleado = employeeQueryService.getAllEmployees().stream()
                .collect(Collectors.toMap(EmployeeResponseDto::id, e -> AppUtils.fullName(e.nombres(), e.apellidos())));

        List<String> cabeceras = List.of("Empleado", "Fecha Inicio", "Fecha Fin", "Días", "Motivo", "Estado");
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<List<Object>> filas = solicitudes.stream()
                .map(v -> List.<Object>of(
                        nombresPorEmpleado.getOrDefault(v.employeeId(), "Empleado #" + v.employeeId()),
                        v.fechaInicio() != null ? v.fechaInicio().format(formatoFecha) : "",
                        v.fechaFin() != null ? v.fechaFin().format(formatoFecha) : "",
                        v.dias() != null ? v.dias() : 0,
                        valorOVacio(v.motivo()),
                        v.estado() != null ? v.estado().name() : ""))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "vacaciones.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Vacaciones", cabeceras, filas);
            filename = "vacaciones.xlsx";
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
