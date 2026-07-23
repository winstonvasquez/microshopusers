package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.DepartmentCommandService;
import com.microshop.rrhh.application.dto.department.DepartmentRequestDto;
import com.microshop.rrhh.application.dto.department.DepartmentResponseDto;
import com.microshop.rrhh.application.query.DepartmentQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) Boolean activo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return ResponseEntity.ok(departmentQueryService.getDepartmentsPaged(search, activo, pageable));
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
