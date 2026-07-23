package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.ContractCommandService;
import com.microshop.rrhh.application.dto.contract.ContractRequestDto;
import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.application.query.ContractQueryService;
import com.microshop.rrhh.domain.model.Contract;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.CONTRACTS)
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Gestión de contratos laborales")
@SecurityRequirement(name = "bearer-key")
public class ContractController {

    private final ContractCommandService contractCommandService;
    private final ContractQueryService contractQueryService;

    @GetMapping("/paged")
    public ResponseEntity<Page<ContractResponseDto>> getContractsPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Contract.ContractStatus status,
            @RequestParam(required = false) Contract.ContractType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(contractQueryService.getContractsPaged(search, status, type, pageable));
    }

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

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/export")
    @Operation(summary = "Exportar contratos a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportContracts(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Contract.ContractStatus status,
            @RequestParam(required = false) Contract.ContractType type) {
        // Trae TODOS los contratos que matcheen los mismos filtros que la lista (sin paginación real).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("id").descending());
        List<ContractResponseDto> contratos = contractQueryService.getContractsPaged(search, status, type, pageable).getContent();

        List<String> cabeceras = List.of("Empleado", "Tipo", "Inicio", "Fin", "Salario", "Jornada", "Estado");
        List<List<Object>> filas = contratos.stream()
                .map(c -> List.<Object>of(
                        valorOVacio(c.employeeName()),
                        tipoLabel(c.tipoContrato()),
                        c.fechaInicio() != null ? c.fechaInicio().format(FECHA_FMT) : "",
                        c.fechaFin() != null ? c.fechaFin().format(FECHA_FMT) : "—",
                        salarioLabel(c.moneda(), c.salarioBase()),
                        jornadaLabel(c.jornadaLaboral()),
                        estadoLabel(c.estado(), c.expiringSoon())))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "contratos.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Contratos", cabeceras, filas);
            filename = "contratos.xlsx";
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

    private static String tipoLabel(Contract.ContractType tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case INDEFINIDO -> "Indefinido";
            case PLAZO_FIJO -> "Plazo Fijo";
            case TEMPORAL -> "Temporal";
            case PRACTICAS -> "Prácticas";
            case LOCACION_SERVICIOS -> "Locación de Servicios";
        };
    }

    private static String jornadaLabel(Contract.WorkingDay jornada) {
        if (jornada == null) {
            return "";
        }
        return switch (jornada) {
            case COMPLETA -> "Tiempo Completo";
            case PARCIAL -> "Medio Tiempo";
            case REDUCIDA -> "Por Horas";
        };
    }

    private static String estadoLabel(Contract.ContractStatus estado, boolean expiringSoon) {
        if (estado == null) {
            return "";
        }
        String base = switch (estado) {
            case ACTIVO -> "Activo";
            case FINALIZADO -> "Finalizado";
            case SUSPENDIDO -> "Suspendido";
            case RENOVADO -> "Renovado";
        };
        return expiringSoon ? base + " (Por vencer)" : base;
    }

    private static String salarioLabel(String moneda, java.math.BigDecimal salarioBase) {
        String simbolo = "USD".equalsIgnoreCase(moneda) ? "$" : "S/";
        java.math.BigDecimal monto = salarioBase != null ? salarioBase : java.math.BigDecimal.ZERO;
        return simbolo + " " + monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
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
