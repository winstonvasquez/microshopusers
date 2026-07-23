package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.EmployeeCommandService;
import com.microshop.rrhh.application.dto.employee.EmployeeRequestDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.query.EmployeeQueryService;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) Employee.EmployeeStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending());
        return ResponseEntity.ok(employeeQueryService.getEmployeesPaged(search, status, pageable));
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
