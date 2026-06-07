package com.microshop.users.application.dto;

/**
 * Resultado de la verificación del PIN de un supervisor para autorizar
 * operaciones sensibles en el POS (ej: descuentos por encima del umbral).
 *
 * <p>Por seguridad, ante PIN inválido, rol no autorizador o empresa no
 * coincidente se devuelve {@code authorized=false} sin detalle del usuario,
 * para no filtrar qué PINs existen ni a quién pertenecen.</p>
 */
public record SupervisorAuthResponse(
        boolean authorized,
        Long userId,
        String username,
        String rol) {
}
