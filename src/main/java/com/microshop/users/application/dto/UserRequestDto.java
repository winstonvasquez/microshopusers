package com.microshop.users.application.dto;

import com.microshop.users.application.dto.ValidationGroups.OnCreate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para crear o actualizar un usuario
 */
public record UserRequestDto(
        @NotBlank(message = "El username es obligatorio") @Size(max = 50, message = "El username no puede exceder los 50 caracteres") String username,

        @NotBlank(message = "El email es obligatorio") @Email(message = "El email debe ser válido") @Size(max = 100, message = "El email no puede exceder los 100 caracteres") String email,

        /*
         * La contraseña SOLO es obligatoria al crear (grupo OnCreate). En el update se
         * envía en blanco cuando no se quiere cambiar — UserCommandService ignora el
         * valor vacío y valida la longitud mínima únicamente si viene con contenido.
         */
        @NotBlank(message = "La contraseña es obligatoria", groups = OnCreate.class) @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres", groups = OnCreate.class) String password,

        @NotNull(message = "El rol es obligatorio") Long rolId,

        /**
         * Estado activo/inactivo del usuario (borrado lógico). Opcional: si llega
         * {@code null} en un update NO se modifica el estado actual; en el alta,
         * {@code null} equivale a activo.
         */
        Boolean activo,

        // Datos de persona
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres") String nombres,

        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres") String apellidos,

        @NotBlank(message = "El tipo de documento es obligatorio") @Size(max = 20, message = "El tipo de documento no puede exceder los 20 caracteres") String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio") @Size(max = 15, message = "El número de documento no puede exceder los 15 caracteres") String numeroDocumento,

        @NotNull(message = "La fecha de nacimiento es obligatoria") LocalDate fechaNacimiento) {
}
