package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Cambio de plan/estado de la suscripción de una empresa — solo SUPERADMIN. */
public record SubscriptionChangeRequest(
        @NotBlank(message = "El código del plan es obligatorio") String planCode,
        @NotBlank(message = "El estado es obligatorio") String status) {}
