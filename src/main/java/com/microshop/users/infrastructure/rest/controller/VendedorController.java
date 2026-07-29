package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.VendedorCommandService;
import com.microshop.users.application.query.VendedorQueryService;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.shared.constants.ApiPaths;
import org.springframework.security.access.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microshop.users.application.dto.VendedorRequestDto;
import com.microshop.users.application.dto.VendedorResponseDto;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.VENDEDORES)
@RequiredArgsConstructor
@Tag(name = "Vendedores", description = "Endpoints para la gestión de perfiles de vendedores")
public class VendedorController {

    private final VendedorCommandService vendedorCommandService;
    private final VendedorQueryService vendedorQueryService;

    /**
     * Resuelve el companyId a usar como scope de tenant: {@code null} SOLO para un SUPERADMIN
     * (bypass intencional). Un usuario normal SIN companyId resoluble en el JWT se rechaza
     * explícitamente — nunca cae a "sin acotar" por accidente (fail-closed, no fail-open).
     *
     * <p>B07: hasta la migración V37 la tabla `vendedor` no tenía company_id y estos endpoints no
     * recibían ni validaban empresa alguna, así que con cualquier JWT se listaban los vendedores de
     * TODA la plataforma y se podía aprobar o rechazar el de otra empresa.</p>
     */
    private Long resolveTenantScope() {
        if (SecurityContextUtils.isSuperAdmin()) return null;
        Long companyId = SecurityContextUtils.currentCompanyId();
        if (companyId == null) {
            throw new AccessDeniedException("JWT sin claim companyId — no se puede acotar por tenant");
        }
        return companyId;
    }

    @PostMapping("/users/{usuarioId}")
    @Operation(summary = "Registrar un usuario como vendedor",
               description = "Crea un perfil de vendedor para un usuario de la propia empresa")
    public ResponseEntity<VendedorResponseDto> registerSeller(
            @PathVariable Long usuarioId,
            @Valid @RequestBody VendedorRequestDto request) {
        return new ResponseEntity<>(
                vendedorCommandService.registerSeller(usuarioId, request, resolveTenantScope()),
                HttpStatus.CREATED);
    }

    @GetMapping("/users/{usuarioId}")
    @Operation(summary = "Obtener perfil de vendedor por ID de usuario", description = "Acotado a la propia empresa")
    public ResponseEntity<VendedorResponseDto> getSellerByUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(vendedorQueryService.getSellerByUsuarioId(usuarioId, resolveTenantScope()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil de vendedor por su ID", description = "Acotado a la propia empresa")
    public ResponseEntity<VendedorResponseDto> getSellerById(@PathVariable Long id) {
        return ResponseEntity.ok(vendedorQueryService.getSellerById(id, resolveTenantScope()));
    }

    @GetMapping
    @Operation(summary = "Obtener los vendedores de la propia empresa",
               description = "Un SUPERADMIN obtiene los de todas las empresas")
    public ResponseEntity<List<VendedorResponseDto>> getAllSellers() {
        return ResponseEntity.ok(vendedorQueryService.getAllSellers(resolveTenantScope()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de aprobación del vendedor",
               description = "Actualiza el estado (PENDING, APPROVED, REJECTED) de un vendedor de la propia empresa")
    public ResponseEntity<VendedorResponseDto> updateSellerStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(vendedorCommandService.updateSellerStatus(id, status, resolveTenantScope()));
    }
}
