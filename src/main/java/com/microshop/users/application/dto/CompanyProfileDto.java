package com.microshop.users.application.dto;

import java.util.List;

public record CompanyProfileDto(
    Long id,
    String name,
    String ruc,
    String legalName,
    String address,
    String phone,
    String email,
    String logoUrl,
    String planCode,
    String subscriptionStatus,
    List<SaasModuleDto> enabledModules
) {}
