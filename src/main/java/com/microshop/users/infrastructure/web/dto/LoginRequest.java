package com.microshop.users.infrastructure.web.dto;

public record LoginRequest(String username, String password, Long companyId) {
}
