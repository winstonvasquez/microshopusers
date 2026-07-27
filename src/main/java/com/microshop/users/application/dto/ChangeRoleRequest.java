package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición para cambiar el rol de un usuario (endpoint restringido a SUPERADMIN).
 */
public record ChangeRoleRequest(
        @NotBlank(message = "El código de rol es obligatorio")
        String roleCode
) {}
