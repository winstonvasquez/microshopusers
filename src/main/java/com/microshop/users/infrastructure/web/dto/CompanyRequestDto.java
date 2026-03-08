package com.microshop.users.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequestDto(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres") String name,

        @NotBlank(message = "El RUC es obligatorio") @Size(max = 20, message = "El RUC no puede exceder los 20 caracteres") String ruc,

        boolean active) {
    public CompanyRequestDto {
        // active default handling if needed, but record constructor is minimal
    }
}
