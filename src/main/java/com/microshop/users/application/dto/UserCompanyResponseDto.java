package com.microshop.users.application.dto;

public record UserCompanyResponseDto(
        Long id,
        Long userId,
        Long companyId,
        java.util.List<Long> roleIds) {
}
