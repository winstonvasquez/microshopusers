package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.dto.SystemParameterDto;
import com.microshop.users.application.query.ErpParameterQueryService;
import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
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
    private final ErpParameterJpaRepository repository;

    @GetMapping
    public ResponseEntity<List<SystemParameterDto>> getAllParameters() {
        return ResponseEntity.ok(parameterQueryService.getAllActiveAsDto());
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
        var paramOpt = repository.findByParamKeyAndTenantIdIsNull(key);
        if (paramOpt.isEmpty()) {
            return ResponseEntity.<Void>notFound().build();
        }
        var param = paramOpt.get();
        if (!Boolean.TRUE.equals(param.getEditable())) {
            return ResponseEntity.<Void>status(403).build();
        }
        param.setParamValue(value.trim().replace("\"", ""));
        repository.save(param);
        return ResponseEntity.<Void>ok().build();
    }
}
