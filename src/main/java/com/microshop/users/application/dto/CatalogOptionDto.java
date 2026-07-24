package com.microshop.users.application.dto;

/**
 * Opción de un catálogo (dropdown) servida desde erp_parameters.
 * Convención de clave: param_key = 'CATALOGO.&lt;TABLA&gt;.&lt;CODIGO&gt;', param_value = etiqueta.
 *
 * @param codigo valor que se persiste (ej. "INTEGRA", "AFP", "PEN")
 * @param valor  etiqueta visible en la UI (ej. "Integra", "S/ (PEN)")
 */
public record CatalogOptionDto(String codigo, String valor) {
}
