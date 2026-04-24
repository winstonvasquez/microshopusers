package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.PayrollCommandService;
import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.query.PayrollQueryService;
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
    @Operation(summary = "Crear planilla individual")
    public ResponseEntity<PayrollResponseDto> createPayroll(@Valid @RequestBody PayrollRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollCommandService.createPayroll(request));
    }

    @PostMapping("/run")
    @Operation(summary = "Generar planillas para un periodo")
    public ResponseEntity<List<PayrollResponseDto>> generatePayrollForPeriod(@RequestParam String periodo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollCommandService.generatePayrollForPeriod(periodo));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar planilla")
    public ResponseEntity<PayrollResponseDto> approvePayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollCommandService.approvePayroll(id));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Marcar planilla como pagada")
    public ResponseEntity<PayrollResponseDto> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollCommandService.markAsPaid(id));
    }
}
