package com.microshop.users.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaasPlanDto(Long id, String code, String name, String description, BigDecimal priceMonthly, BigDecimal priceAnnual, Integer maxUsers, List<String> moduleCodes) {}
