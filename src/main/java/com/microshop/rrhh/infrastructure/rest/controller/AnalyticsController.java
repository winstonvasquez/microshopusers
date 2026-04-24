package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.dto.analytics.HrAnalyticsDto;
import com.microshop.rrhh.application.query.AnalyticsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard analítico de RRHH")
@SecurityRequirement(name = "bearer-key")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener métricas del dashboard de RRHH")
    public ResponseEntity<HrAnalyticsDto> getDashboard() {
        return ResponseEntity.ok(analyticsQueryService.getDashboardAnalytics());
    }
}
