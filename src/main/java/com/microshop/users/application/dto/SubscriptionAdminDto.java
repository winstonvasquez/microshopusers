package com.microshop.users.application.dto;

import java.time.Instant;

/** Suscripción de una empresa tal como la ve el SUPERADMIN al cambiar de plan. */
public record SubscriptionAdminDto(Long companyId, String planCode, String planName, String status, Instant trialEndsAt) {}
