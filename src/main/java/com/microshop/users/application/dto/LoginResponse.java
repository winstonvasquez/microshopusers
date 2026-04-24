package com.microshop.users.application.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String username,
    Long userId,
    Long activeCompanyId,
    List<Long> availableCompanyIds,
    List<String> enabledModules
) {}
