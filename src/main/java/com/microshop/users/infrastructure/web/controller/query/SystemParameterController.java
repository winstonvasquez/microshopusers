package com.microshop.users.infrastructure.web.controller.query;

import com.microshop.users.infrastructure.persistence.entity.ErpParameterEntity;
import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/parameters")
@RequiredArgsConstructor
public class SystemParameterController {

    private final ErpParameterJpaRepository repository;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllParameters() {
        List<ErpParameterEntity> params = repository.findAllByIsActiveTrue();

        Map<String, String> paramsMap = params.stream()
                .collect(Collectors.toMap(
                        ErpParameterEntity::getParamKey,
                        p -> p.getParamValue() != null ? p.getParamValue() : ""));

        return ResponseEntity.ok(paramsMap);
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getParameter(@PathVariable String key) {
        return repository.findByParamKey(key)
                .map(p -> ResponseEntity.ok(p.getParamValue() != null ? p.getParamValue() : ""))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
