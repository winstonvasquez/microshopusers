package com.microshop.users.infrastructure.web.dto;

public record CompanyResponseDto(
        Long id,
        String name,
        String ruc,
        boolean isActive) {
}
