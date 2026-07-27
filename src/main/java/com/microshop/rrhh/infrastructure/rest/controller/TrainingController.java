package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.TrainingCommandService;
import com.microshop.rrhh.application.dto.training.*;
import com.microshop.rrhh.application.query.TrainingQueryService;
import com.microshop.rrhh.domain.model.Training;
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
@RequestMapping(ApiPaths.TRAININGS)
@RequiredArgsConstructor
@Tag(name = "Trainings", description = "Gestión de capacitaciones")
@SecurityRequirement(name = "bearer-key")
public class TrainingController {

    private final TrainingCommandService trainingCommandService;
    private final TrainingQueryService trainingQueryService;

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping
    @Operation(summary = "Listar capacitaciones paginado con filtros avanzados: búsqueda, estado, instructor y rangos de fecha inicio/fin (server-side)")
    public ResponseEntity<Page<TrainingResponseDto>> getAll(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Training.TrainingStatus estado,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return ResponseEntity.ok(trainingQueryService.getTrainingsPaged(search, estado, instructor,
                fechaInicioDesde, fechaInicioHasta, fechaFinDesde, fechaFinHasta, pageable));
    }

    /** Campos por los que se permite ordenar (whitelist anti PropertyReference/500). */
    private static final List<String> CAMPOS_ORDENABLES =
            List.of("fechaInicio", "fechaFin", "nombre", "createdAt");

    /** Parsea "campo,dir" (ej. "fechaInicio,desc"); si es inválido usa fechaInicio DESC. */
    private static Sort resolveSort(String sort) {
        Sort porDefecto = Sort.by("fechaInicio").descending();
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

    @GetMapping("/export")
    @Operation(summary = "Exportar capacitaciones a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportTrainings(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Training.TrainingStatus estado,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta) {
        // Mismos filtros que la lista del frontend (antes solo recibía 'estado' y perdía el resto).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("fechaInicio").descending());
        List<TrainingResponseDto> capacitaciones = trainingQueryService.getTrainingsPaged(search, estado, instructor,
                fechaInicioDesde, fechaInicioHasta, fechaFinDesde, fechaFinHasta, pageable).getContent();

        List<String> cabeceras = List.of("Curso", "Instructor", "Inicio", "Fin", "Horas", "Partic.", "Estado");
        List<List<Object>> filas = capacitaciones.stream()
                .map(t -> List.<Object>of(
                        valorOVacio(t.nombre()),
                        valorOVacio(t.instructor()),
                        t.fechaInicio() != null ? t.fechaInicio().format(FECHA_FORMATO) : "",
                        t.fechaFin() != null ? t.fechaFin().format(FECHA_FORMATO) : "",
                        t.duracionHoras() != null ? t.duracionHoras() + "h" : "",
                        t.participantes(),
                        valorOVacio(t.estado())))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "capacitaciones.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Capacitaciones", cabeceras, filas);
            filename = "capacitaciones.xlsx";
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
