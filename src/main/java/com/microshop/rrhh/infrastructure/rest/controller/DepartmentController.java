package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.DepartmentCommandService;
import com.microshop.rrhh.application.dto.department.DepartmentRequestDto;
import com.microshop.rrhh.application.dto.department.DepartmentResponseDto;
import com.microshop.rrhh.application.query.DepartmentQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.DEPARTMENTS)
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Gestión de departamentos/áreas")
@SecurityRequirement(name = "bearer-key")
public class DepartmentController {

    private final DepartmentCommandService departmentCommandService;
    private final DepartmentQueryService departmentQueryService;

    @GetMapping("/paged")
    public ResponseEntity<Page<DepartmentResponseDto>> getDepartmentsPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean activo) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return ResponseEntity.ok(departmentQueryService.getDepartmentsPaged(search, activo, pageable));
    }

    /** Campos por los que se permite ordenar (whitelist anti PropertyReference/500). */
    private static final List<String> CAMPOS_ORDENABLES =
            List.of("nombre", "codigo", "createdAt");

    /** Parsea "campo,dir" (ej. "createdAt,desc"); si es inválido usa nombre ASC. */
    private static Sort resolveSort(String sort) {
        Sort porDefecto = Sort.by("nombre").ascending();
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

    @GetMapping
    @Operation(summary = "Listar todos los departamentos activos")
    public ResponseEntity<List<DepartmentResponseDto>> getActiveDepartments() {
        return ResponseEntity.ok(departmentQueryService.getActiveDepartments());
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los departamentos (incluye inactivos)")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentQueryService.getAllDepartments());
    }

    @GetMapping("/tree")
    @Operation(summary = "Obtener árbol jerárquico de departamentos")
    public ResponseEntity<List<DepartmentResponseDto>> getDepartmentTree() {
        return ResponseEntity.ok(departmentQueryService.getDepartmentTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener departamento por ID")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentQueryService.getDepartmentById(id));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Obtener sub-departamentos")
    public ResponseEntity<List<DepartmentResponseDto>> getSubDepartments(@PathVariable Long id) {
        return ResponseEntity.ok(departmentQueryService.getSubDepartments(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar departamentos")
    public ResponseEntity<List<DepartmentResponseDto>> searchDepartments(@RequestParam String term) {
        return ResponseEntity.ok(departmentQueryService.searchDepartments(term));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar departamentos a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportDepartments(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo) {
        // Trae TODOS los departamentos que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("nombre").ascending());
        List<DepartmentResponseDto> departamentos = departmentQueryService.getDepartmentsPaged(search, activo, pageable).getContent();

        List<String> cabeceras = List.of("Código", "Nombre", "Dept. Padre", "Jefe", "Empleados", "Puestos", "Estado");
        List<List<Object>> filas = departamentos.stream()
                .map(d -> List.<Object>of(
                        valorOVacio(d.codigo()),
                        valorOVacio(d.nombre()),
                        valorOVacio(d.parentName()),
                        valorOVacio(d.managerName()),
                        d.employeeCount(),
                        d.positionCount(),
                        Boolean.TRUE.equals(d.activo()) ? "ACTIVO" : "INACTIVO"))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "departments.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Departamentos", cabeceras, filas);
            filename = "departments.xlsx";
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

    @PostMapping
    @Operation(summary = "Crear nuevo departamento")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentCommandService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar departamento")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDto request) {
        return ResponseEntity.ok(departmentCommandService.updateDepartment(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar departamento")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable Long id) {
        departmentCommandService.deactivateDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar departamento")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentCommandService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
