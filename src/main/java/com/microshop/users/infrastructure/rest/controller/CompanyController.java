package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.CompanyCommandService;
import com.microshop.users.application.query.CompanyQueryService;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.application.mapper.CompanyMapper;
import com.microshop.users.application.dto.*;
import com.microshop.users.config.security.RequiresTenantAccess;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.shared.constants.AppConstants;
import com.microshop.users.shared.util.SpreadsheetExporter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Resuelve el companyId a usar como scope de tenant: {@code null} SOLO para un SUPERADMIN
     * (bypass intencional, sin acotar). Un ADMIN normal SIN companyId resoluble en el JWT
     * es rechazado explícitamente — nunca cae a "sin acotar" por accidente (fail-closed, no fail-open).
     */
    private Long resolveTenantScope() {
        if (SecurityContextUtils.isSuperAdmin()) return null;
        Long companyId = SecurityContextUtils.currentCompanyId();
        if (companyId == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "JWT sin claim companyId — no se puede acotar por tenant");
        }
        return companyId;
    }

    @PostMapping
    @Operation(summary = "Crear empresa", description = "Registra una nueva empresa con su RUC")
    public ResponseEntity<CompanyResponseDto> createCompany(
            @RequestBody @Valid CompanyRequestDto companyDto) {
        CompanyEntity company = companyMapper.toEntity(companyDto);
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.createCompany(company)));
    }

    @GetMapping
    @Operation(summary = "Listar empresas", description = "Retorna todas las empresas registradas (SUPERADMIN) o solo la propia (ADMIN)")
    public ResponseEntity<List<CompanyResponseDto>> getAllCompanies() {
        return ResponseEntity.ok(companyQueryService.findAll(resolveTenantScope()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar empresas paginado (server-side)")
    public ResponseEntity<Page<CompanyResponseDto>> getCompaniesPaged(
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.Paginacion.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionHasta,
            @RequestParam(required = false) String planCode,
            @RequestParam(required = false) String subscriptionStatus,
            @RequestParam(required = false) Long rubroId,
            @RequestParam(required = false) Boolean conDominio) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        // LocalDate (yyyy-MM-dd, lo que envía el date-range del frontend) -> Instant día completo.
        Instant desde = fechaCreacionDesde != null ? fechaCreacionDesde.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant hasta = fechaCreacionHasta != null ? fechaCreacionHasta.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;
        return ResponseEntity.ok(companyQueryService.findPaged(search, active, desde, hasta,
                planCode, subscriptionStatus, rubroId, conDominio, pageable, resolveTenantScope()));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar empresas a XLSX o CSV (generado en el backend, datos limpios sin HTML)")
    public ResponseEntity<byte[]> exportCompanies(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionHasta,
            @RequestParam(required = false) String planCode,
            @RequestParam(required = false) String subscriptionStatus,
            @RequestParam(required = false) Long rubroId,
            @RequestParam(required = false) Boolean conDominio) {
        // Trae TODAS las empresas que matcheen los mismos filtros que la lista (sin paginación real),
        // acotado por tenant salvo SUPERADMIN (mismo scope que getCompaniesPaged).
        Pageable pageable = PageRequest.of(0, 100000, Sort.by("name").ascending());
        // LocalDate (yyyy-MM-dd, lo que envía el date-range del frontend) -> Instant día completo.
        Instant desde = fechaCreacionDesde != null ? fechaCreacionDesde.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant hasta = fechaCreacionHasta != null ? fechaCreacionHasta.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;
        List<CompanyResponseDto> empresas = companyQueryService.findPaged(search, active, desde, hasta,
                planCode, subscriptionStatus, rubroId, conDominio, pageable, resolveTenantScope()).getContent();

        List<String> cabeceras = List.of("Nombre", "RUC", "Razón Social", "Email", "Estado");
        List<List<Object>> filas = empresas.stream()
                .map(c -> List.<Object>of(
                        valorOVacio(c.name()),
                        valorOVacio(c.ruc()),
                        valorOVacio(c.legalName()),
                        valorOVacio(c.email()),
                        c.isActive() ? "Activo" : "Inactivo"))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "empresas.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Empresas", cabeceras, filas);
            filename = "empresas.xlsx";
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

    // Los tres endpoints por {id} llevan @RequiresTenantAccess(paramName = "id"): aquí el id del
    // path ES el companyId, así que el aspecto puede compararlo contra el del JWT. Hasta
    // 2026-07-28 no lo llevaban y SecurityConfig solo exigía el ROL (ADMIN|SUPERADMIN) sobre
    // /users/api/companies/**, sin la identidad del tenant: cualquier ADMIN podía leer, reescribir
    // (incluido `domain`, con el que se resuelve el tenant en el checkout de invitado) o desactivar
    // la empresa de otro cliente del SaaS cambiando el id de la URL. Verificado con
    // AislamientoMultiTenantTest, que fallaba antes de este cambio.

    @GetMapping("/{id}")
    @RequiresTenantAccess(paramName = "id", allowSuperAdmin = true)
    @Operation(summary = "Obtener empresa por ID", description = "Retorna todos los campos de la empresa propia")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable @NonNull Long id) {
        return companyQueryService.findFullById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @RequiresTenantAccess(paramName = "id", allowSuperAdmin = true)
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<CompanyResponseDto> updateCompany(@PathVariable Long id,
            @RequestBody @Valid CompanyRequestDto companyDto) {
        CompanyEntity company = companyMapper.toEntity(companyDto);
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.updateCompany(id, company)));
    }

    @DeleteMapping("/{id}")
    @RequiresTenantAccess(paramName = "id", allowSuperAdmin = true)
    @Operation(summary = "Desactivar empresa (soft delete)")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyCommandService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    // ── Logotipo binario almacenado en BD (V36) ─────────────────

    /**
     * GET /users/api/companies/{id}/logo — PÚBLICO (el logo se pinta en login, tienda
     * y documentos, antes de tener sesión). Cacheado con ETag + Cache-Control.
     */
    @GetMapping("/{id}/logo")
    @Operation(summary = "Servir logotipo binario de la empresa", description = "Público, cacheado con ETag")
    public ResponseEntity<byte[]> getCompanyLogo(
            @PathVariable @NonNull Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        return companyQueryService.serveLogo(id, ifNoneMatch);
    }

    /** POST /users/api/companies/{id}/logo — multipart protegido. Valida tipo, tamaño y magic bytes. */
    @PostMapping(value = "/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresTenantAccess(allowSuperAdmin = true)
    @Operation(summary = "Subir logotipo binario de la empresa", description = "JPEG/PNG/WebP, máx 500 KB")
    public ResponseEntity<CompanyResponseDto> uploadCompanyLogo(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.uploadLogo(companyId, file)));
    }

    /** DELETE /users/api/companies/{id}/logo — borra el binario del logotipo y sus metadatos. */
    @DeleteMapping("/{companyId}/logo")
    @RequiresTenantAccess(allowSuperAdmin = true)
    @Operation(summary = "Eliminar logotipo binario de la empresa",
               description = "Borra el blob y sus metadatos (mime/etag/size); la empresa queda sin logo.")
    public ResponseEntity<CompanyResponseDto> deleteCompanyLogo(@PathVariable Long companyId) {
        log.info("DELETE /users/api/companies/{}/logo - Eliminando logotipo", companyId);
        return ResponseEntity.ok(companyMapper.toDto(companyCommandService.deleteLogo(companyId)));
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

    /**
     * Suspende o reactiva una empresa, tocando ÚNICAMENTE su estado.
     *
     * <p>Antes esto sólo se podía hacer de dos maneras, y las dos son malas. Con
     * {@code DELETE /{id}}, que hace un soft delete pero se llama «eliminar» y no puede reactivar. O
     * con {@code PUT /{id}} reenviando el DTO completo con el flag girado, que es lo que hacía el
     * método {@code toggleActive()} del frontend —y ése es el problema real: {@code updateCompany}
     * sobrescribe ocho campos de golpe, así que cualquier campo que la fila de la lista no llevara se
     * ponía a null. En particular el {@code domain}, que es justamente el campo que hoy está vacío en
     * las siete empresas y del que depende la resolución de tenant del checkout de invitado. Girar el
     * estado no debe poder borrar datos de la empresa.</p>
     *
     * <p>SUPERADMIN, no {@code @RequiresTenantAccess(allowSuperAdmin = true)} como el DELETE:
     * suspender es una operación de plataforma. Que el ADMIN de una empresa pueda desactivarse a sí
     * mismo es un accidente del soft delete, no una capacidad que convenga extender.</p>
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Operation(summary = "Suspender o reactivar una empresa (solo el estado)",
               description = "Operación de plataforma: no toca ningún otro campo de la empresa.")
    public ResponseEntity<CompanyResponseDto> cambiarEstado(
            @PathVariable Long id,
            @RequestBody @Valid CambiarEstadoEmpresaRequest request) {
        log.info("PATCH /api/companies/{}/estado - activa={}", id, request.activa());
        return ResponseEntity.ok(
                companyMapper.toDto(companyCommandService.cambiarEstado(id, request.activa())));
    }
}
