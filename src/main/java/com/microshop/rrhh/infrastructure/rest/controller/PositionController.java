package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.PositionCommandService;
import com.microshop.rrhh.application.dto.position.PositionRequestDto;
import com.microshop.rrhh.application.dto.position.PositionResponseDto;
import com.microshop.rrhh.application.query.PositionQueryService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.POSITIONS)
@RequiredArgsConstructor
@Tag(name = "Positions", description = "Gestión de puestos/cargos")
@SecurityRequirement(name = "bearer-key")
public class PositionController {

    private final PositionCommandService positionCommandService;
    private final PositionQueryService positionQueryService;

    @GetMapping("/paged")
    public ResponseEntity<Page<PositionResponseDto>> getPositionsPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return ResponseEntity.ok(positionQueryService.getPositionsPaged(search, departmentId, pageable));
    }

    @GetMapping
    @Operation(summary = "Listar todos los puestos activos")
    public ResponseEntity<List<PositionResponseDto>> getActivePositions() {
        return ResponseEntity.ok(positionQueryService.getActivePositions());
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los puestos (incluye inactivos)")
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        return ResponseEntity.ok(positionQueryService.getAllPositions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener puesto por ID")
    public ResponseEntity<PositionResponseDto> getPositionById(@PathVariable Long id) {
        return ResponseEntity.ok(positionQueryService.getPositionById(id));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Listar puestos por departamento")
    public ResponseEntity<List<PositionResponseDto>> getPositionsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(positionQueryService.getPositionsByDepartment(departmentId));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar puestos")
    public ResponseEntity<List<PositionResponseDto>> searchPositions(@RequestParam String term) {
        return ResponseEntity.ok(positionQueryService.searchPositions(term));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar puestos a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportPositions(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId) {
        // Trae TODOS los puestos que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("nombre").ascending());
        List<PositionResponseDto> puestos = positionQueryService.getPositionsPaged(search, departmentId, pageable).getContent();

        List<String> cabeceras = List.of("Código", "Nombre", "Departamento", "Nivel", "Rango Salarial", "Empleados", "Estado");
        List<List<Object>> filas = puestos.stream()
                .map(p -> List.<Object>of(
                        valorOVacio(p.codigo()),
                        valorOVacio(p.nombre()),
                        valorOVacio(p.departmentName()),
                        valorOVacio(p.nivel()),
                        rangoSalarial(p.salarioMinimo(), p.salarioMaximo()),
                        p.employeeCount(),
                        Boolean.TRUE.equals(p.activo()) ? "ACTIVO" : "INACTIVO"))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "puestos.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Puestos", cabeceras, filas);
            filename = "puestos.xlsx";
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

    /** Rango salarial legible ("min - max"), sin HTML ni símbolo de moneda; "" si ambos son null. */
    private static String rangoSalarial(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return "";
        String minTxt = min != null ? min.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "—";
        String maxTxt = max != null ? max.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "—";
        return minTxt + " - " + maxTxt;
    }

    @PostMapping
    @Operation(summary = "Crear nuevo puesto")
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionCommandService.createPosition(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar puesto")
    public ResponseEntity<PositionResponseDto> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequestDto request) {
        return ResponseEntity.ok(positionCommandService.updatePosition(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar puesto")
    public ResponseEntity<Void> deactivatePosition(@PathVariable Long id) {
        positionCommandService.deactivatePosition(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar puesto")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionCommandService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
