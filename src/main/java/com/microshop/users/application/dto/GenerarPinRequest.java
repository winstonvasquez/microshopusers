package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Generar el PIN del POS exige reautenticarse con la contraseña, igual que cambiarla. */
public record GenerarPinRequest(@NotBlank(message = "La contraseña actual es obligatoria") String currentPassword) {}
