package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.ContractCommandService;
import com.microshop.rrhh.application.dto.contract.ContractRequestDto;
import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.application.query.ContractQueryService;
import com.microshop.rrhh.domain.model.Contract;
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
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.CONTRACTS)
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Gestión de contratos laborales")
@SecurityRequirement(name = "bearer-key")
public class ContractController {

    private final ContractCommandService contractCommandService;
    private final ContractQueryService contractQueryService;

    @GetMapping
    @Operation(summary = "Listar todos los contratos")
    public ResponseEntity<List<ContractResponseDto>> getAllContracts() {
        return ResponseEntity.ok(contractQueryService.getAllContracts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contrato por ID")
    public ResponseEntity<ContractResponseDto> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractQueryService.getContractById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar contratos de un empleado")
    public ResponseEntity<List<ContractResponseDto>> getContractsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(contractQueryService.getContractsByEmployee(employeeId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar contratos por estado")
    public ResponseEntity<List<ContractResponseDto>> getContractsByStatus(@PathVariable Contract.ContractStatus status) {
        return ResponseEntity.ok(contractQueryService.getContractsByStatus(status));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Listar contratos por vencer (default: 30 días)")
    public ResponseEntity<List<ContractResponseDto>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(contractQueryService.getExpiringContracts(days));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo contrato")
    public ResponseEntity<ContractResponseDto> createContract(@Valid @RequestBody ContractRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractCommandService.createContract(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contrato")
    public ResponseEntity<ContractResponseDto> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequestDto request) {
        return ResponseEntity.ok(contractCommandService.updateContract(id, request));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(summary = "Finalizar contrato")
    public ResponseEntity<ContractResponseDto> terminateContract(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(contractCommandService.terminateContract(id, body.getOrDefault("motivoFin", "")));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renovar contrato")
    public ResponseEntity<ContractResponseDto> renewContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractCommandService.renewContract(id, request));
    }
}
