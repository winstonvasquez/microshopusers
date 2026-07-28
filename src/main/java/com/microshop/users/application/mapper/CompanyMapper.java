package com.microshop.users.application.mapper;


import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.application.dto.CompanyRequestDto;
import com.microshop.users.application.dto.CompanyResponseDto;
import com.microshop.users.shared.constants.ApiPaths;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyEntity toEntity(CompanyRequestDto dto) {
        CompanyEntity entity = new CompanyEntity();
        entity.setName(dto.name());
        entity.setRuc(dto.ruc());
        entity.setActive(dto.active() != null ? dto.active() : true);
        entity.setLegalName(dto.legalName());
        entity.setAddress(dto.address());
        entity.setPhone(dto.phone());
        entity.setEmail(dto.email());
        entity.setLogoUrl(dto.logoUrl());
        entity.setDomain(dto.domain());
        return entity;
    }

    public CompanyResponseDto toDto(CompanyEntity entity) {
        return new CompanyResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getRuc(),
                entity.isActive(),
                entity.getLegalName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getEmail(),
                resolveLogoUrl(entity),
                entity.getDomain());
    }

    /**
     * URL del logotipo expuesta al frontend: apunta al endpoint binario cuando el logo
     * está en BD; si no, cae al logo_url externo guardado (fallback).
     */
    public static String resolveLogoUrl(CompanyEntity entity) {
        return (entity.getLogoData() != null)
                ? ApiPaths.COMPANIES + "/" + entity.getId() + "/logo"
                : entity.getLogoUrl();
    }
}
