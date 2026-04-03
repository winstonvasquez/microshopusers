package com.microshop.users.application.dto;

/**
 * Representa un parámetro del sistema con metadata para la UI de administración.
 */
public record SystemParameterDto(
        String key,
        String value,
        String description,
        boolean editable,
        String tipo,
        String group
) {}
