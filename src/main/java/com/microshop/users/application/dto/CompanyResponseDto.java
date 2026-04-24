package com.microshop.users.application.dto;

public record CompanyResponseDto(
        Long id,
        String name,
        String ruc,
        boolean isActive,
        String legalName,
        String address,
        String phone,
        String email,
        String logoUrl,
        String domain) {

    /** Constructor compacto para las projections JPQL existentes (4 campos). */
    public CompanyResponseDto(Long id, String name, String ruc, boolean isActive) {
        this(id, name, ruc, isActive, null, null, null, null, null, null);
    }
}
