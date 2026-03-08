package com.microshop.users.infrastructure.web.controller;

import com.microshop.users.application.command.CompanyCommandService;
import com.microshop.users.application.query.CompanyQueryService;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.mapper.CompanyMapper;
import com.microshop.users.infrastructure.web.dto.CompanyRequestDto;
import com.microshop.users.infrastructure.web.dto.CompanyResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Empresas", description = "Gestión del catálogo de empresas (multi-tenancy)")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validación"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
public class CompanyController {

    private final CompanyCommandService companyCommandService;
    private final CompanyQueryService companyQueryService;
    private final CompanyMapper companyMapper;

    @PostMapping
    @Operation(summary = "Crear empresa", description = "Registra una nueva empresa con su RUC")
    public ResponseEntity<CompanyResponseDto> createCompany(
            @RequestBody @Valid CompanyRequestDto companyDto) {
        CompanyEntity company = companyMapper.toEntity(companyDto);
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.createCompany(company)));
    }

    @GetMapping
    @Operation(summary = "Listar empresas", description = "Retorna todas las empresas registradas")
    public ResponseEntity<List<CompanyResponseDto>> getAllCompanies() {
        return ResponseEntity.ok(companyQueryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable @NonNull Long id) {
        return companyQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<CompanyResponseDto> updateCompany(@PathVariable Long id,
            @RequestBody @Valid CompanyRequestDto companyDto) {
        CompanyEntity company = companyMapper.toEntity(companyDto);
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.updateCompany(id, company)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar empresa (soft delete)")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyCommandService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
