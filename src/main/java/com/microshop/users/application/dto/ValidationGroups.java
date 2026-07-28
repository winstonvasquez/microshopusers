package com.microshop.users.application.dto;

/**
 * Grupos de validación Bean Validation compartidos por los DTOs del servicio.
 *
 * <p>Permiten que un mismo record sirva para alta y actualización cuando alguna
 * restricción SOLO aplica al crear (por ejemplo, la contraseña obligatoria del
 * usuario: al editar, dejarla en blanco significa "no cambiarla").</p>
 */
public final class ValidationGroups {

    private ValidationGroups() {
    }

    /** Restricciones que solo se validan en el alta (POST). */
    public interface OnCreate {
    }
}
