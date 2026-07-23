package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.CompanyCommandService;
import com.microshop.users.application.query.CompanyQueryService;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.application.mapper.CompanyMapper;
import com.microshop.users.application.dto.*;
import com.microshop.users.config.security.RequiresTenantAccess;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.COMPANIES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Empresas", description = "Gestión del catálogo de empresas (multi-tenancy)")
public class CompanyController {

    private final CompanyCommandService companyCommandService;
    private final CompanyQueryService companyQueryService;
    private final CompanyMapper companyMapper;
    private final SaasQueryService saasQueryService;

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

    @GetMapping("/paged")
    @Operation(summary = "Listar empresas paginado (server-side)")
    public ResponseEntity<Page<CompanyResponseDto>> getCompaniesPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(companyQueryService.findPaged(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID", description = "Retorna todos los campos de la empresa")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable @NonNull Long id) {
        return companyQueryService.findFullById(id)
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

    // ── Sub-recursos por empresa ────────────────────────────────

    @GetMapping("/{id}/modules")
    @Operation(summary = "Módulos habilitados por empresa")
    @RequiresTenantAccess(allowSuperAdmin = true)
    public ResponseEntity<List<SaasModuleDto>> getCompanyModules(@PathVariable("id") Long companyId) {
        return ResponseEntity.ok(saasQueryService.getEnabledModules(companyId));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "Usuarios asignados a la empresa")
    @RequiresTenantAccess(allowSuperAdmin = true)
    public ResponseEntity<List<CompanyUserDto>> getCompanyUsers(@PathVariable("id") Long companyId) {
        return ResponseEntity.ok(companyQueryService.findUsersByCompanyId(companyId));
    }

    @GetMapping("/{id}/subscription")
    @Operation(summary = "Suscripción activa de la empresa")
    @RequiresTenantAccess(allowSuperAdmin = true)
    public ResponseEntity<CompanySubscriptionDto> getCompanySubscription(@PathVariable("id") Long companyId) {
        return companyQueryService.findSubscriptionByCompanyId(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{companyId}/modules/{moduleId}")
    @Operation(summary = "Activar/desactivar módulo para empresa")
    @RequiresTenantAccess(allowSuperAdmin = true)
    public ResponseEntity<Void> toggleCompanyModule(
            @PathVariable Long companyId,
            @PathVariable Long moduleId,
            @RequestParam boolean enabled) {
        companyCommandService.toggleModule(companyId, moduleId, enabled);
        return ResponseEntity.ok().build();
    }
}
