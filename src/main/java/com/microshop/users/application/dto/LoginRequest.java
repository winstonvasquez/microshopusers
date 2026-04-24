package com.microshop.users.application.dto;

public record LoginRequest(String username, String password, Long companyId) {
}
