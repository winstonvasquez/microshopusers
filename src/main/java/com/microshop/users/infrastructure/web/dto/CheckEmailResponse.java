package com.microshop.users.infrastructure.web.dto;

public record CheckEmailResponse(
        boolean exists,
        String maskedEmail) {
}
