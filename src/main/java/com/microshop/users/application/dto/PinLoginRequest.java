package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;

/** PIN del desbloqueo del POS. Va en el cuerpo, no en la URL, para que no quede en logs de acceso. */
public record PinLoginRequest(@NotBlank(message = "El PIN es obligatorio") String pin) {}
