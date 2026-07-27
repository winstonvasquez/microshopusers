package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.SaasOnboardingCommandService;
import com.microshop.users.application.dto.*;
import com.microshop.users.application.query.LandingContentQueryService;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.SAAS)
@RequiredArgsConstructor
@Tag(name = "SaaS", description = "Registro de tenants y catálogo de planes/módulos")
public class SaasController {

    private final SaasOnboardingCommandService onboardingService;
    private final SaasQueryService saasQueryService;
    private final LandingContentQueryService landingContentQueryService;

    @PostMapping("/register")
    @Operation(summary = "Registro de nueva empresa (onboarding SaaS)")
    public ResponseEntity<SaasRegisterResponse> register(@Valid @RequestBody SaasRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.register(request));
    }

    @GetMapping("/plans")
    @Operation(summary = "Lista de planes de suscripción disponibles")
    public ResponseEntity<List<SaasPlanDto>> getPlans() {
        return ResponseEntity.ok(saasQueryService.getAllPlans());
    }

    @GetMapping("/modules")
    @Operation(summary = "Catálogo de todos los módulos ERP disponibles")
    public ResponseEntity<List<SaasModuleDto>> getModules() {
        return ResponseEntity.ok(saasQueryService.getEnabledModules(null));
    }

    @GetMapping("/landing-content")
    @Operation(summary = "Contenido editable de /portal/landing (secciones Problema, Cómo funciona, Confianza, FAQ)")
    public ResponseEntity<Map<String, Object>> getLandingContent() {
        return ResponseEntity.ok(landingContentQueryService.getAllSections());
    }
}
