package com.microshop.users.application.query;

import com.microshop.users.application.dto.SystemParameterDto;
import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErpParameterQueryService {

    private final ErpParameterJpaRepository repository;

    /** Busca un parámetro global (tenant_id IS NULL) */
    @Cacheable(value = "erp-params", key = "#key")
    public Optional<String> getGlobal(String key) {
        return repository.findByParamKeyAndTenantIdIsNull(key)
                .map(e -> e.getParamValue() != null ? e.getParamValue() : "");
    }

    /** Busca por tenant, con fallback a global */
    public String getForTenant(String key, String tenantId) {
        return repository.findByParamKeyAndTenantId(key, tenantId)
                .map(e -> e.getParamValue() != null ? e.getParamValue() : "")
                .orElseGet(() -> getGlobal(key).orElse(""));
    }

    /** Retorna todos los parámetros activos globales como mapa */
    @Cacheable(value = "erp-params", key = "'all-active'")
    public Map<String, String> getAllActive() {
        return repository.findAllByIsActiveTrueAndTenantIdIsNull().stream()
                .collect(Collectors.toMap(
                        e -> e.getParamKey(),
                        e -> e.getParamValue() != null ? e.getParamValue() : "",
                        (v1, v2) -> v1));
    }

    /** Retorna todos los parámetros activos globales como lista de DTOs con metadata. */
    public List<SystemParameterDto> getAllActiveAsDto() {
        return repository.findAllByIsActiveTrueAndTenantIdIsNull().stream()
                .map(e -> new SystemParameterDto(
                        e.getParamKey(),
                        e.getParamValue() != null ? e.getParamValue() : "",
                        e.getParamDescription() != null ? e.getParamDescription() : "",
                        Boolean.TRUE.equals(e.getEditable()),
                        e.getTipo() != null ? e.getTipo() : "text",
                        e.getParamGroup() != null ? e.getParamGroup() : ""))
                .toList();
    }
}
