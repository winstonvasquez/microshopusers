package com.microshop.users.application.dto;

/**
 * DTO para las peticiones de cambio de tema (usuario y empresa).
 *
 * @param themeKey  clave del tema (p. ej. "dark", "fresh-mint")
 * @param module    módulo al que aplica: "shop" | "admin" | "pos" (default "shop")
 * @param companyId empresa a la que aplica (opcional; tiene prioridad el JWT)
 */
public record ThemeRequestDto(
        String themeKey,
        String module,
        String companyId
) {}
