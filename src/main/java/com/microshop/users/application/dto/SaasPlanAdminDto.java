package com.microshop.users.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** Igual que {@link SaasPlanDto} pero incluye isActive — solo para el panel admin (SUPERADMIN). */
public record SaasPlanAdminDto(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal priceMonthly,
        BigDecimal priceAnnual,
        Integer maxUsers,
        List<String> moduleCodes,
        boolean isActive
) {}
