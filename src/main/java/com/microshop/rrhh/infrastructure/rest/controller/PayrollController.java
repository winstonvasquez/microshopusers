package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.PayrollCommandService;
import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.query.PayrollQueryService;
import com.microshop.rrhh.domain.model.Payroll;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.PAYROLL)
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Gestión de planillas")
@SecurityRequirement(name = "bearer-key")
public class PayrollController {

    private final PayrollCommandService payrollCommandService;
    private final PayrollQueryService payrollQueryService;

    @GetMapping("/period/{periodo}")
    @Operation(summary = "Listar planillas por periodo")
    public ResponseEntity<List<PayrollResponseDto>> getByPeriod(@PathVariable String periodo) {
        return ResponseEntity.ok(payrollQueryService.getByPeriod(periodo));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar planillas paginado (server-side) con filtros avanzados: periodo, "
            + "búsqueda por empleado, estado, empleado, departamento, AFP/ONP y rango de fecha de pago")
    public ResponseEntity<Page<PayrollResponseDto>> getPayrollPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Payroll.PayrollStatus estado,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String afpOnp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPagoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPagoHasta) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("periodo").descending());
        return ResponseEntity.ok(payrollQueryService.getPayrollPaged(periodo, search, estado, employeeId,
                departmentId, afpOnp, fechaPagoDesde, fechaPagoHasta, pageable));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar planillas de un empleado")
    public ResponseEntity<List<PayrollResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollQueryService.getByEmployee(employeeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener planilla por ID")
    public ResponseEntity<PayrollResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollQueryService.getById(id));
    }

    @PostMapping
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_INTERNAL)
    @Operation(summary = "Crear planilla individual")
    public ResponseEntity<PayrollResponseDto> createPayroll(@Valid @RequestBody PayrollRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollCommandService.createPayroll(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_INTERNAL)
    @Operation(summary = "Corregir planilla individual (solo en estado GENERADO)")
    public ResponseEntity<PayrollResponseDto> updatePayroll(@PathVariable Long id,
            @Valid @RequestBody PayrollRequestDto request) {
        return ResponseEntity.ok(payrollCommandService.updatePayroll(id, request));
    }

    @PostMapping("/run")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_INTERNAL)
    @Operation(summary = "Generar planillas para un periodo")
    public ResponseEntity<List<PayrollResponseDto>> generatePayrollForPeriod(@RequestParam String periodo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollCommandService.generatePayrollForPeriod(periodo));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_INTERNAL)
    @Operation(summary = "Aprobar planilla")
    public ResponseEntity<PayrollResponseDto> approvePayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollCommandService.approvePayroll(id));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize(AppConstants.Seguridad.ADMIN_OR_INTERNAL)
    @Operation(summary = "Marcar planilla como pagada")
    public ResponseEntity<PayrollResponseDto> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollCommandService.markAsPaid(id));
    }
}
