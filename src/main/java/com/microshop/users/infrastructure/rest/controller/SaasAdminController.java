package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.LandingContentCommandService;
import com.microshop.users.application.command.SaasPlanCommandService;
import com.microshop.users.application.dto.LandingSectionUpdateRequest;
import com.microshop.users.application.dto.SaasPlanAdminDto;
import com.microshop.users.application.dto.SaasPlanAdminRequest;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD admin de planes SaaS — configuración global de la plataforma (precios, límites,
 * módulos incluidos por plan). Restringido a SUPERADMIN (ver SecurityConfig: matchers de
 * /users/api/saas/admin/** van ANTES del permitAll de /users/api/saas/**).
 */
@RestController
@RequestMapping(ApiPaths.SAAS + "/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SaaS Admin", description = "Configuración de planes SaaS — solo SUPERADMIN")
public class SaasAdminController {

    private final SaasPlanCommandService saasPlanCommandService;
    private final SaasQueryService saasQueryService;
    private final LandingContentCommandService landingContentCommandService;

    @GetMapping("/plans")
    @Operation(summary = "Lista todos los planes (incluye inactivos)")
    public ResponseEntity<List<SaasPlanAdminDto>> getAllPlans() {
        return ResponseEntity.ok(saasQueryService.getAllPlansForAdmin());
    }

    @PostMapping("/plans")
    @Operation(summary = "Crear plan SaaS")
    public ResponseEntity<SaasPlanAdminDto> createPlan(@RequestBody @Valid SaasPlanAdminRequest request) {
        log.info("POST /saas/admin/plans - Creando plan {}", request.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(saasPlanCommandService.createPlan(request));
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Editar plan SaaS (precios, límite de usuarios, módulos incluidos)")
    public ResponseEntity<SaasPlanAdminDto> updatePlan(@PathVariable Long id, @RequestBody @Valid SaasPlanAdminRequest request) {
        log.info("PUT /saas/admin/plans/{} - Actualizando plan", id);
        return ResponseEntity.ok(saasPlanCommandService.updatePlan(id, request));
    }

    @PatchMapping("/plans/{id}/deactivate")
    @Operation(summary = "Desactivar plan (soft — nunca DELETE físico, hay suscripciones con FK al plan)")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        log.info("PATCH /saas/admin/plans/{}/deactivate", id);
        saasPlanCommandService.setActive(id, false);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/plans/{id}/activate")
    @Operation(summary = "Reactivar un plan previamente desactivado")
    public ResponseEntity<Void> activatePlan(@PathVariable Long id) {
        log.info("PATCH /saas/admin/plans/{}/activate", id);
        saasPlanCommandService.setActive(id, true);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/landing-content")
    @Operation(summary = "Editar UNA sección de contenido de /portal/landing")
    public ResponseEntity<Void> updateLandingContent(@RequestBody @Valid LandingSectionUpdateRequest request) {
        log.info("PUT /saas/admin/landing-content - Sección {}", request.sectionKey());
        landingContentCommandService.updateSection(request.sectionKey(), request.content());
        return ResponseEntity.ok().build();
    }
}
