package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.dto.*;
import com.microshop.users.application.query.SaasQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.TENANT)
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "Perfil y módulos del tenant autenticado")
public class TenantController {

    private final SaasQueryService saasQueryService;

    @GetMapping("/profile")
    @Operation(summary = "Perfil de la empresa activa del usuario autenticado")
    public ResponseEntity<CompanyProfileDto> getProfile(Authentication auth) {
        Long companyId = extractCompanyId(auth);
        return ResponseEntity.ok(saasQueryService.getCompanyProfile(companyId));
    }

    @GetMapping("/modules")
    @Operation(summary = "Módulos habilitados para la empresa activa del usuario autenticado")
    public ResponseEntity<List<SaasModuleDto>> getModules(Authentication auth) {
        Long companyId = extractCompanyId(auth);
        return ResponseEntity.ok(saasQueryService.getEnabledModules(companyId));
    }

    private Long extractCompanyId(Authentication auth) {
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map && map.containsKey("companyId")) {
            Object v = map.get("companyId");
            if (v instanceof Number n) return n.longValue();
        }
        return null;
    }
}
