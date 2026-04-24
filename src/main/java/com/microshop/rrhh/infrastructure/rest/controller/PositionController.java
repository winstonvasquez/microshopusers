package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.PositionCommandService;
import com.microshop.rrhh.application.dto.position.PositionRequestDto;
import com.microshop.rrhh.application.dto.position.PositionResponseDto;
import com.microshop.rrhh.application.query.PositionQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.POSITIONS)
@RequiredArgsConstructor
@Tag(name = "Positions", description = "Gestión de puestos/cargos")
@SecurityRequirement(name = "bearer-key")
public class PositionController {

    private final PositionCommandService positionCommandService;
    private final PositionQueryService positionQueryService;

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
