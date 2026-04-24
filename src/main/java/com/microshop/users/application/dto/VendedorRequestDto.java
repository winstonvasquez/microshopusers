package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VendedorRequestDto(
        @NotBlank(message = "El DNI/RUC es obligatorio") String dniRuc,

        @NotBlank(message = "El teléfono de contacto es obligatorio") String telefonoContacto) {
}
