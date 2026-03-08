package com.microshop.users.infrastructure.web.dto;

public record UserCompanyResponseDto(
        Long id,
        Long userId,
        Long companyId,
        java.util.List<Long> roleIds) {
}
