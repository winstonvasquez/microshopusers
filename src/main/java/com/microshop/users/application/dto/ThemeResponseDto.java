package com.microshop.users.application.dto;

/**
 * DTO de respuesta con el tema activo para un módulo.
 *
 * @param themeKey         clave del tema activo
 * @param isSeasonalActive indica si el tema fue activado por campaña estacional
 * @param seasonalName     nombre de la campaña (vacío si no hay campaña activa)
 */
public record ThemeResponseDto(
        String themeKey,
        boolean isSeasonalActive,
        String seasonalName
) {}
