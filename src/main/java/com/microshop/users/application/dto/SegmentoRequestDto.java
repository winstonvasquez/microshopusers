package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar un segmento de clientes.
 */
public record SegmentoRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String descripcion,

        @NotBlank(message = "El color es obligatorio")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un valor hexadecimal válido (ej: #d7132a)")
        String color,

        @NotBlank(message = "El tipo de cliente es obligatorio")
        String tipoCliente,

        boolean activo
) {}
