package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.ErpParameterCommandService;
import com.microshop.users.application.dto.CatalogOptionDto;
import com.microshop.users.application.dto.SystemParameterDto;
import com.microshop.users.application.query.ErpParameterQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.SYSTEM_PARAMETERS)
@RequiredArgsConstructor
public class SystemParameterController {

    private final ErpParameterQueryService parameterQueryService;
    private final ErpParameterCommandService parameterCommandService;

    @GetMapping
    public ResponseEntity<List<SystemParameterDto>> getAllParameters() {
        return ResponseEntity.ok(parameterQueryService.getAllActiveAsDto());
    }

    /**
     * Opciones de un catálogo para poblar dropdowns (ej. /catalog/AFP).
     * Fuente única: erp_parameters con param_key = 'CATALOGO.&lt;TABLA&gt;.&lt;CODIGO&gt;'.
     */
    @GetMapping("/catalog/{tabla}")
    public ResponseEntity<List<CatalogOptionDto>> getCatalog(@PathVariable String tabla) {
        return ResponseEntity.ok(parameterQueryService.getCatalog(tabla));
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getParameter(@PathVariable String key) {
        return parameterQueryService.getGlobal(key)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.<String>notFound().build());
    }

    @PutMapping("/{key}")
    @CacheEvict(value = "erp-params", allEntries = true)
    public ResponseEntity<Void> updateParameter(@PathVariable String key, @RequestBody String value) {
        var result = parameterCommandService.updateParameter(key, value);
        return switch (result) {
            case NOT_FOUND -> ResponseEntity.<Void>notFound().build();
            case FORBIDDEN -> ResponseEntity.<Void>status(403).build();
            case OK -> ResponseEntity.<Void>ok().build();
        };
    }
}
