package com.microshop.users.application.dto;

/**
 * Opción de un nivel del ubigeo para poblar un dropdown.
 *
 * <p>Deliberadamente igual en forma a {@link CatalogOptionDto} ({@code codigo} + etiqueta), para
 * que el frontend pueda reutilizar el mismo tipo de select que ya usa con los catálogos de
 * {@code erp_parameters} sin un mapeo aparte.
 *
 * @param codigo código INEI: 2 dígitos para departamento, 4 para provincia, 6 para distrito
 * @param valor  nombre visible ("Lima", "Santiago de Surco")
 */
public record UbigeoOptionDto(String codigo, String valor) {
}
