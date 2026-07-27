package com.microshop.users.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/** Petición de alta/edición de un plan SaaS — restringido a SUPERADMIN (ver SecurityConfig). */
public record SaasPlanAdminRequest(
        @NotBlank(message = "El código del plan es obligatorio")
        String code,

        @NotBlank(message = "El nombre del plan es obligatorio")
        String name,

        String description,

        @NotNull(message = "El precio mensual es obligatorio")
        BigDecimal priceMonthly,

        @NotNull(message = "El precio anual es obligatorio")
        BigDecimal priceAnnual,

        @NotNull(message = "El límite de usuarios es obligatorio")
        @Min(value = 1, message = "El límite de usuarios debe ser al menos 1")
        Integer maxUsers,

        /** Códigos de módulo (SaasModuleEntity.code) que este plan incluye — reemplaza el set completo en cada update. */
        List<String> moduleCodes
) {}
