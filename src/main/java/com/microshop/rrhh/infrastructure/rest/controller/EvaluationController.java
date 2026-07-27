package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.EvaluationCommandService;
import com.microshop.rrhh.application.dto.evaluation.*;
import com.microshop.rrhh.application.query.EvaluationQueryService;
import com.microshop.rrhh.domain.model.PerformanceEvaluation;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.EVALUATIONS)
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "Gestión de evaluaciones de desempeño")
@SecurityRequirement(name = "bearer-key")
public class EvaluationController {

    private final EvaluationCommandService evaluationCommandService;
    private final EvaluationQueryService evaluationQueryService;

    @GetMapping
    @Operation(summary = "Listar evaluaciones (paginado, con filtros de estado/tipo/rango de fecha)")
    public ResponseEntity<Page<EvaluationResponseDto>> getAll(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) PerformanceEvaluation.EvaluationStatus estado,
            @RequestParam(required = false) PerformanceEvaluation.EvaluationType tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvaluacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvaluacionHasta) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return ResponseEntity.ok(evaluationQueryService.getAllPaged(
                estado, tipo, fechaEvaluacionDesde, fechaEvaluacionHasta, pageable));
    }

    /** Campos por los que se permite ordenar (whitelist anti PropertyReference/500). */
    private static final List<String> CAMPOS_ORDENABLES =
            List.of("fechaEvaluacion", "periodo", "puntaje", "createdAt");

    /** Parsea "campo,dir" (ej. "fechaEvaluacion,asc"); si es inválido usa fechaEvaluacion DESC. */
    private static Sort resolveSort(String sort) {
        Sort porDefecto = Sort.by("fechaEvaluacion").descending();
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
    @Operation(summary = "Obtener evaluación por ID")
    public ResponseEntity<EvaluationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationQueryService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar evaluaciones de un empleado")
    public ResponseEntity<List<EvaluationResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(evaluationQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/period/{periodo}")
    @Operation(summary = "Listar evaluaciones por periodo")
    public ResponseEntity<List<EvaluationResponseDto>> getByPeriod(@PathVariable String periodo) {
        return ResponseEntity.ok(evaluationQueryService.getByPeriod(periodo));
    }

    @GetMapping("/evaluador/{evaluadorId}")
    @Operation(summary = "Listar evaluaciones asignadas a un evaluador")
    public ResponseEntity<List<EvaluationResponseDto>> getByEvaluador(@PathVariable Long evaluadorId) {
        return ResponseEntity.ok(evaluationQueryService.getByEvaluador(evaluadorId));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar evaluaciones a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportEvaluations(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) PerformanceEvaluation.EvaluationStatus estado,
            @RequestParam(required = false) PerformanceEvaluation.EvaluationType tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvaluacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvaluacionHasta) {
        // Trae TODAS las evaluaciones que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, resolveSort(null));
        List<EvaluationResponseDto> evaluaciones = evaluationQueryService.getAllPaged(
                estado, tipo, fechaEvaluacionDesde, fechaEvaluacionHasta, pageable).getContent();

        List<String> cabeceras = List.of("Empleado", "Evaluador", "Período", "Tipo", "Fecha", "Puntaje", "Estado");
        DateTimeFormatter fechaFormato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<List<Object>> filas = evaluaciones.stream()
                .map(e -> List.<Object>of(
                        e.employeeName() != null ? e.employeeName() : "Emp #" + e.employeeId(),
                        e.evaluadorName() != null ? e.evaluadorName() : "Emp #" + e.evaluadorId(),
                        valorOVacio(e.periodo()),
                        valorOVacio(e.tipoEvaluacion()),
                        e.fechaEvaluacion() != null ? e.fechaEvaluacion().format(fechaFormato) : "",
                        e.puntaje() != null ? e.puntaje() : "",
                        valorOVacio(e.estado())))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "evaluaciones.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Evaluaciones", cabeceras, filas);
            filename = "evaluaciones.xlsx";
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
    @Operation(summary = "Crear evaluación")
    public ResponseEntity<EvaluationResponseDto> create(@Valid @RequestBody EvaluationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationCommandService.createEvaluation(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evaluación")
    public ResponseEntity<EvaluationResponseDto> update(@PathVariable Long id, @Valid @RequestBody EvaluationRequestDto request) {
        return ResponseEntity.ok(evaluationCommandService.updateEvaluation(id, request));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Completar evaluación")
    public ResponseEntity<EvaluationResponseDto> complete(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.completeEvaluation(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar evaluación")
    public ResponseEntity<EvaluationResponseDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.approveEvaluation(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar evaluación")
    public ResponseEntity<EvaluationResponseDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationCommandService.cancelEvaluation(id));
    }

    // ── Criteria ────────────────────────────────────────────────────────────

    @GetMapping("/criteria")
    @Operation(summary = "Listar todos los criterios de evaluación")
    public ResponseEntity<List<EvaluationCriteriaResponseDto>> getAllCriteria() {
        return ResponseEntity.ok(evaluationQueryService.getAllCriteria());
    }

    @GetMapping("/criteria/active")
    @Operation(summary = "Listar criterios activos")
    public ResponseEntity<List<EvaluationCriteriaResponseDto>> getActiveCriteria() {
        return ResponseEntity.ok(evaluationQueryService.getActiveCriteria());
    }

    @GetMapping("/criteria/{id}")
    @Operation(summary = "Obtener criterio por ID")
    public ResponseEntity<EvaluationCriteriaResponseDto> getCriteriaById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationQueryService.getCriteriaById(id));
    }

    @PostMapping("/criteria")
    @Operation(summary = "Crear criterio de evaluación")
    public ResponseEntity<EvaluationCriteriaResponseDto> createCriteria(@Valid @RequestBody EvaluationCriteriaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationCommandService.createCriteria(request));
    }

    @PutMapping("/criteria/{id}")
    @Operation(summary = "Actualizar criterio de evaluación")
    public ResponseEntity<EvaluationCriteriaResponseDto> updateCriteria(@PathVariable Long id, @Valid @RequestBody EvaluationCriteriaRequestDto request) {
        return ResponseEntity.ok(evaluationCommandService.updateCriteria(id, request));
    }

    @PatchMapping("/criteria/{id}/deactivate")
    @Operation(summary = "Desactivar criterio de evaluación")
    public ResponseEntity<Void> deactivateCriteria(@PathVariable Long id) {
        evaluationCommandService.deactivateCriteria(id);
        return ResponseEntity.noContent().build();
    }
}
