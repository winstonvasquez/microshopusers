package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.EmployeeCommandService;
import com.microshop.rrhh.application.dto.employee.EmployeeRequestDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.query.EmployeeQueryService;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
import com.microshop.users.shared.util.AppUtils;
import com.microshop.users.shared.util.SpreadsheetExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.EMPLOYEES)
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Gestión de empleados")
@SecurityRequirement(name = "bearer-key")
public class EmployeeController {

    private final EmployeeCommandService employeeCommandService;
    private final EmployeeQueryService employeeQueryService;

    @GetMapping
    @Operation(summary = "Listar todos los empleados")
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeQueryService.getAllEmployees());
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar empleados paginado (search + estado, server-side)")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployeesPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Employee.EmployeeStatus status) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return ResponseEntity.ok(employeeQueryService.getEmployeesPaged(search, status, pageable));
    }

    /** Campos por los que se permite ordenar (whitelist anti PropertyReference/500). */
    private static final List<String> CAMPOS_ORDENABLES =
            List.of("apellidos", "nombres", "codigoEmpleado", "createdAt", "fechaIngreso");

    /** Parsea "campo,dir" (ej. "createdAt,desc"); si es inválido usa apellidos ASC. */
    private static Sort resolveSort(String sort) {
        Sort porDefecto = Sort.by("apellidos").ascending();
        if (sort == null || sort.isBlank()) {
            return porDefecto;
        }
        String[] partes = sort.split(",");
        String campo = partes[0].trim();
        if (!CAMPOS_ORDENABLES.contains(campo)) {
            return porDefecto;
        }
        Sort.Direction dir = partes.length > 1 && "desc".equalsIgnoreCase(partes[1].trim())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, campo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeQueryService.getEmployeeById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar empleados por estado")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByStatus(@PathVariable Employee.EmployeeStatus status) {
        return ResponseEntity.ok(employeeQueryService.getEmployeesByStatus(status));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar empleados")
    public ResponseEntity<List<EmployeeResponseDto>> searchEmployees(@RequestParam String term) {
        return ResponseEntity.ok(employeeQueryService.searchEmployees(term));
    }

    @GetMapping("/count")
    @Operation(summary = "Contar empleados totales")
    public ResponseEntity<Long> countEmployees() {
        return ResponseEntity.ok(employeeQueryService.countEmployees());
    }

    @GetMapping("/count/active")
    @Operation(summary = "Contar empleados activos")
    public ResponseEntity<Long> countActiveEmployees() {
        return ResponseEntity.ok(employeeQueryService.countActiveEmployees());
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar empleados a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportEmployees(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Employee.EmployeeStatus status) {
        // Trae TODOS los empleados que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("apellidos").ascending());
        List<EmployeeResponseDto> empleados = employeeQueryService.getEmployeesPaged(search, status, pageable).getContent();

        List<String> cabeceras = List.of("Código", "Nombre Completo", "DNI/Doc", "Departamento", "Puesto", "Estado");
        List<List<Object>> filas = empleados.stream()
                .map(e -> List.<Object>of(
                        valorOVacio(e.codigoEmpleado()),
                        AppUtils.fullName(e.nombres(), e.apellidos()),
                        valorOVacio(e.documentoIdentidad()),
                        valorOVacio(e.departmentName() != null ? e.departmentName() : e.area()),
                        valorOVacio(e.positionName() != null ? e.positionName() : e.cargo()),
                        e.estado() != null ? e.estado().name() : ""))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "empleados.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Empleados", cabeceras, filas);
            filename = "empleados.xlsx";
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

    @GetMapping("/report/export")
    @Operation(summary = "Exportar reporte RRHH (admin/reportes) a XLSX o CSV — mismas columnas que la vista")
    public ResponseEntity<byte[]> exportReporteRrhh(@RequestParam(defaultValue = "xlsx") String format) {
        // Replica exactamente lo que arma admin/pages/reportes/reportes-rrhh.component.ts:
        // todos los empleados del tenant (sin filtros server-side, igual que getAllEmployees()).
        List<EmployeeResponseDto> empleados = employeeQueryService.getAllEmployees();

        List<String> cabeceras = List.of("Codigo", "Nombres", "Apellidos", "DNI", "Cargo", "Area", "Estado", "Fecha Ingreso");
        List<List<Object>> filas = empleados.stream()
                .map(e -> List.<Object>of(
                        valorOVacio(e.codigoEmpleado()),
                        valorOVacio(e.nombres()),
                        valorOVacio(e.apellidos()),
                        valorOVacio(e.documentoIdentidad()),
                        valorOVacio(e.cargo()),
                        valorOVacio(e.area()),
                        e.estado() != null ? e.estado().name() : "",
                        e.fechaIngreso() != null ? e.fechaIngreso().toString() : ""))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "reporte-rrhh.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Reporte RRHH", cabeceras, filas);
            filename = "reporte-rrhh.xlsx";
            contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(bytes);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo empleado")
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeCommandService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto request) {
        return ResponseEntity.ok(employeeCommandService.updateEmployee(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar empleado")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable Long id) {
        employeeCommandService.deactivateEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empleado")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeCommandService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
