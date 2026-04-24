package com.microshop.users.application.dto;

import jakarta.validation.constraints.*;

public record SaasRegisterRequest(
    @NotBlank String companyName,
    @NotBlank @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 dígitos") String ruc,
    @NotBlank @Email String adminEmail,
    @NotBlank @Size(min = 8) String adminPassword,
    @NotBlank String adminNombres,
    @NotBlank String adminApellidos,
    String planCode
) {
    public String resolvedPlanCode() {
        return (planCode == null || planCode.isBlank()) ? "STARTER" : planCode;
    }
}
