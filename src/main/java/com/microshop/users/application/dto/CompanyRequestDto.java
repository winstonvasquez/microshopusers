package com.microshop.users.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String name,

        @NotBlank(message = "El RUC es obligatorio")
        @Size(max = 20, message = "El RUC no puede exceder los 20 caracteres")
        @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 dígitos numéricos")
        String ruc,

        boolean active,

        @Size(max = 200, message = "La razón social no puede exceder los 200 caracteres")
        String legalName,

        @Size(max = 300, message = "La dirección no puede exceder los 300 caracteres")
        String address,

        @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
        String phone,

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        String email,

        @Size(max = 500, message = "La URL del logo no puede exceder los 500 caracteres")
        String logoUrl,

        @Size(max = 100, message = "El dominio no puede exceder los 100 caracteres")
        String domain) {
}
