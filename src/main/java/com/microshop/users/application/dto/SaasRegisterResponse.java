package com.microshop.users.application.dto;

import java.util.List;

public record SaasRegisterResponse(
    Long companyId,
    Long userId,
    String token,
    String username,
    String planCode,
    String subscriptionStatus,
    List<String> enabledModules
) {}
