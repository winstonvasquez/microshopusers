package com.microshop.rrhh.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP hacia microshopusers para obtener parámetros ERP centralizados.
 */
@Component
@Slf4j
public class UsersParameterClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${microshop.services.users:http://localhost:8080}")
    private String usersUrl;

    public String getParameter(String key, String defaultValue) {
        try {
            String url = usersUrl + "/users/api/system/parameters/" + key;
            String value = restTemplate.getForObject(url, String.class);
            return (value != null && !value.isBlank()) ? value.trim() : defaultValue;
        } catch (Exception e) {
            log.warn("UsersParameterClient: no se pudo obtener '{}', usando default '{}': {}", key, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    public java.math.BigDecimal getDecimal(String key, String defaultValue) {
        String value = getParameter(key, defaultValue);
        try {
            return new java.math.BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("UsersParameterClient: valor '{}' para '{}' no es decimal, usando '{}'", value, key, defaultValue);
            return new java.math.BigDecimal(defaultValue);
        }
    }
}
