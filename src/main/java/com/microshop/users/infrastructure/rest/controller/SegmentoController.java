package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.SegmentoCommandService;
import com.microshop.users.application.dto.SegmentoRequestDto;
import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.query.SegmentoQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.shared.util.SpreadsheetExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.SEGMENTS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Segmentos", description = "Gestión de segmentos de clientes")
public class SegmentoController {

    private final SegmentoCommandService commandService;
    private final SegmentoQueryService queryService;

    @GetMapping
    @Operation(summary = "Listar segmentos (paginado) con filtros avanzados: estado, tipo de cliente y rango de fecha de creación")
    public ResponseEntity<Page<SegmentoResponseDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String tipoCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET {} - search={} activo={} tipoCliente={} fechaDesde={} fechaHasta={}",
                ApiPaths.SEGMENTS, search, activo, tipoCliente, fechaDesde, fechaHasta);
        Instant desde = toInstantInicioDia(fechaDesde);
        Instant hasta = toInstantFinDia(fechaHasta);
        return ResponseEntity.ok(queryService.findAll(search, activo, tipoCliente, desde, hasta, pageable));
    }

    /** fechaCreacion es Instant (heredado de AuditEntity) -> convierte el LocalDate del date-range a inicio de día. */
    private static Instant toInstantInicioDia(LocalDate fecha) {
        return fecha != null ? fecha.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
    }

    /** Convierte el LocalDate del date-range a fin de día (23:59:59.999999999) en la zona del servidor. */
    private static Instant toInstantFinDia(LocalDate fecha) {
        return fecha != null ? fecha.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener segmento por ID")
    public ResponseEntity<SegmentoResponseDto> getById(@PathVariable Long id) {
        log.info("GET {}/{}", ApiPaths.SEGMENTS, id);
        return ResponseEntity.ok(queryService.findById(id));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar segmentos a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String tipoCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        log.info("GET {}/export - format={} search={} activo={} tipoCliente={} fechaDesde={} fechaHasta={}",
                ApiPaths.SEGMENTS, format, search, activo, tipoCliente, fechaDesde, fechaHasta);
        Instant desde = toInstantInicioDia(fechaDesde);
        Instant hasta = toInstantFinDia(fechaHasta);
        // Trae TODOS los segmentos que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("nombre").ascending());
        List<SegmentoResponseDto> segmentos = queryService.findAll(search, activo, tipoCliente, desde, hasta, pageable).getContent();

        List<String> cabeceras = List.of("Segmento", "Tipo de Cliente", "Descripción", "Clientes", "Estado");
        List<List<Object>> filas = segmentos.stream()
                .map(s -> List.<Object>of(
                        valorOVacio(s.nombre()),
                        valorOVacio(s.tipoCliente()),
                        valorOVacio(s.descripcion()),
                        s.totalClientes() != null ? s.totalClientes() : 0,
                        s.activo() ? "Activo" : "Inactivo"))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "segmentos.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Segmentos", cabeceras, filas);
            filename = "segmentos.xlsx";
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
    @Operation(summary = "Crear nuevo segmento")
    public ResponseEntity<SegmentoResponseDto> create(@RequestBody @Valid SegmentoRequestDto dto) {
        log.info("POST {} - nombre={}", ApiPaths.SEGMENTS, dto.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar segmento existente")
    public ResponseEntity<SegmentoResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid SegmentoRequestDto dto) {
        log.info("PUT {}/{}", ApiPaths.SEGMENTS, id);
        return ResponseEntity.ok(commandService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar segmento (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE {}/{}", ApiPaths.SEGMENTS, id);
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
