package com.microshop.users.application.dto;

public record CheckEmailResponse(
        boolean exists,
        String maskedEmail) {
}
