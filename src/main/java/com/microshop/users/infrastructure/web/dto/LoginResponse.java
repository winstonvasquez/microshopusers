package com.microshop.users.infrastructure.web.dto;

import java.util.List;

public record LoginResponse(String token, String username, Long userId, Long activeCompanyId,
        List<Long> availableCompanyIds) {
}
