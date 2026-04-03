package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.ThemePreferenceService;
import com.microshop.users.application.query.ErpParameterQueryService;
import com.microshop.users.application.query.ThemePreferenceQueryService;
import com.microshop.users.infrastructure.persistence.entity.ThemeSeasonalEntity;
import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
import com.microshop.users.infrastructure.persistence.repository.ThemeSeasonalRepository;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de temas de la tienda pública.
 * GET /api/themes/active es público — no requiere autenticación.
 * PUT /api/themes/active persiste preferencia de usuario si hay sesión activa, o en erp_parameters si no hay.
 * PUT /api/themes/company requiere autenticación (admin).
 */
@RestController
@RequestMapping(ApiPaths.THEMES)
@RequiredArgsConstructor
@Tag(name = "Temas", description = "Gestión del tema visual de la tienda pública")
public class ThemeController {

    private static final String PARAM_SEASONAL_ENABLED = "SHOP_THEME_SEASONAL_ENABLED";
    private static final String DEFAULT_THEME         = "dark";

    /** Mapea el parámetro de módulo a la clave en erp_parameters. */
    private static String paramKeyForModule(String module) {
        return switch (module == null ? "shop" : module.toLowerCase()) {
            case "admin" -> "ADMIN_ACTIVE_THEME";
            case "pos"   -> "POS_ACTIVE_THEME";
            default      -> "SHOP_ACTIVE_THEME";
        };
    }

    /** Extrae el companyId del contexto JWT (inyectado por JwtAuthenticationFilter en authentication.details). */
    private static Long extractCompanyId(Authentication auth) {
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map && map.containsKey("companyId")) {
            Object v = map.get("companyId");
            if (v instanceof Number n) return n.longValue();
        }
        return null;
    }

    private final ErpParameterQueryService    paramQueryService;
    private final ErpParameterJpaRepository   erpParamRepository;
    private final ThemeSeasonalRepository     themeSeasonalRepository;
    private final ThemePreferenceQueryService themeQueryService;
    private final ThemePreferenceService      themeCommandService;

    /**
     * Devuelve el tema activo para el módulo indicado.
     * Resolución: seasonal > user preference > company default > erp_parameters global.
     *
     * @param module      shop (default) | admin | pos
     * @param userDetails inyectado por Spring Security si el request lleva JWT
     * @param auth        inyectado por Spring Security para extraer companyId del JWT
     */
    @GetMapping("/active")
    @Operation(summary = "Obtener tema activo por módulo", description = "Endpoint público. ?module=shop|admin|pos (default: shop)")
    public ResponseEntity<Map<String, Object>> getActiveTheme(
            @RequestParam(value = "module", required = false, defaultValue = "shop") String module,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication auth) {

        String paramKey = paramKeyForModule(module);

        // Los temas estacionales solo aplican al módulo shop
        if ("shop".equalsIgnoreCase(module)) {
            boolean seasonalEnabled = paramQueryService
                    .getGlobal(PARAM_SEASONAL_ENABLED)
                    .map(v -> "true".equalsIgnoreCase(v.trim()))
                    .orElse(false);

            if (seasonalEnabled) {
                LocalDate today = LocalDate.now();
                List<ThemeSeasonalEntity> active = themeSeasonalRepository.findActiveThemesForDate(today);
                if (!active.isEmpty()) {
                    ThemeSeasonalEntity seasonal = active.get(0);
                    return ResponseEntity.ok(Map.of(
                            "themeKey",         seasonal.getThemeKey(),
                            "isSeasonalActive", true,
                            "seasonalName",     seasonal.getName()
                    ));
                }
            }
        }

        // Preferencia de usuario (si hay sesión activa)
        if (userDetails != null) {
            try {
                Long userId    = themeQueryService.resolveUserId(userDetails.getUsername());
                Long companyId = extractCompanyId(auth);
                if (companyId == null) companyId = 0L;
                String userKey = themeQueryService.resolveTheme(userId, companyId, module);
                if (userKey != null && !userKey.isBlank()) {
                    return ResponseEntity.ok(Map.of(
                            "themeKey",         userKey,
                            "isSeasonalActive", false,
                            "seasonalName",     ""
                    ));
                }
            } catch (Exception ignored) {
                // Usuario no resuelto — continúa con tema global
            }
        }

        String themeKey = paramQueryService
                .getGlobal(paramKey)
                .filter(v -> !v.isBlank())
                .orElse(DEFAULT_THEME);

        return ResponseEntity.ok(Map.of(
                "themeKey",         themeKey,
                "isSeasonalActive", false,
                "seasonalName",     ""
        ));
    }

    /**
     * Actualiza el tema activo para el módulo indicado.
     * Si hay sesión activa, persiste la preferencia del usuario en BD.
     * Si no hay sesión, actualiza el parámetro global en erp_parameters (admin).
     *
     * @param body { "themeKey": "fresh-mint", "module": "shop", "companyId": "1" }
     */
    @PutMapping("/active")
    @CacheEvict(value = "themes", allEntries = true)
    @Operation(summary = "Cambiar tema activo por módulo", description = "Con sesión: persiste preferencia de usuario. Sin sesión: actualiza parámetro global (admin).")
    public ResponseEntity<Void> setActiveTheme(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication auth) {

        String themeKey = body.get("themeKey");
        if (themeKey == null || themeKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String module = body.getOrDefault("module", "shop");

        if (userDetails != null) {
            try {
                Long userId    = themeQueryService.resolveUserId(userDetails.getUsername());
                // Prioridad: companyId del JWT > companyId del body > 0L (global)
                Long companyId = extractCompanyId(auth);
                if (companyId == null) {
                    String companyStr = body.get("companyId");
                    companyId = (companyStr != null) ? Long.parseLong(companyStr) : 0L;
                }
                themeCommandService.saveUserPreference(userId, companyId, module, themeKey.trim());
                return ResponseEntity.<Void>ok().build();
            } catch (Exception ignored) {
                // Fallo silencioso — intenta actualizar parámetro global
            }
        }

        // Fallback: actualizar parámetro global (comportamiento original)
        String paramKey = paramKeyForModule(module);
        var paramOpt = erpParamRepository.findByParamKeyAndTenantIdIsNull(paramKey);
        if (paramOpt.isEmpty()) {
            return ResponseEntity.<Void>notFound().build();
        }
        var param = paramOpt.get();
        param.setParamValue(themeKey.trim());
        erpParamRepository.save(param);
        return ResponseEntity.<Void>ok().build();
    }

    /**
     * Guarda el tema base de empresa (solo ADMIN).
     *
     * @param body { "themeKey": "...", "module": "shop", "companyId": "1" }
     */
    @PutMapping("/company")
    @Operation(summary = "Guardar tema base de empresa (solo ADMIN)")
    public ResponseEntity<Void> setCompanyTheme(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        String themeKey = body.get("themeKey");
        String module   = body.getOrDefault("module", "shop");

        // companyId del JWT tiene prioridad; fallback al body
        Long companyId = extractCompanyId(auth);
        if (companyId == null) {
            String companyStr = body.get("companyId");
            if (themeKey == null || themeKey.isBlank() || companyStr == null) {
                return ResponseEntity.badRequest().build();
            }
            companyId = Long.parseLong(companyStr);
        }
        if (themeKey == null || themeKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        themeCommandService.saveCompanyTheme(companyId, module, themeKey.trim());
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina la preferencia de tema del usuario para un módulo.
     * El cliente vuelve al tema de empresa o al global.
     */
    @DeleteMapping("/active")
    @Operation(summary = "Eliminar preferencia de tema del usuario")
    public ResponseEntity<Void> deleteUserTheme(
            @RequestParam(value = "module", required = false, defaultValue = "shop") String module,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication auth) {

        if (userDetails == null) return ResponseEntity.status(401).build();
        Long userId    = themeQueryService.resolveUserId(userDetails.getUsername());
        Long companyId = extractCompanyId(auth);
        if (companyId == null) {
            String companyIdStr = (body != null) ? body.get("companyId") : null;
            companyId = (companyIdStr != null) ? Long.parseLong(companyIdStr) : 0L;
        }
        themeCommandService.deleteUserPreference(userId, companyId, module);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos los temas estacionales activos (para la UI de admin).
     */
    @GetMapping("/seasonal")
    @Operation(summary = "Listar temas estacionales", description = "Requiere autenticación")
    public ResponseEntity<List<ThemeSeasonalEntity>> getSeasonalThemes() {
        return ResponseEntity.ok(
                themeSeasonalRepository.findAllByActiveTrueOrderByStartDateAsc()
        );
    }
}
