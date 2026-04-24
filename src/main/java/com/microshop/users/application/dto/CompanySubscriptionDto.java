package com.microshop.users.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CompanySubscriptionDto(
    Long subscriptionId,
    String planCode,
    String planName,
    String planDescription,
    String status,
    Instant startsAt,
    Instant endsAt,
    Instant trialEndsAt,
    BigDecimal priceMonthly,
    BigDecimal priceAnnual,
    Integer maxUsers
) {}
