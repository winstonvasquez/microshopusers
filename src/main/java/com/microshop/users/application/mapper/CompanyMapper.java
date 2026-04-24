package com.microshop.users.application.mapper;


import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.application.dto.CompanyRequestDto;
import com.microshop.users.application.dto.CompanyResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyEntity toEntity(CompanyRequestDto dto) {
        CompanyEntity entity = new CompanyEntity();
        entity.setName(dto.name());
        entity.setRuc(dto.ruc());
        entity.setActive(dto.active());
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
                entity.getLogoUrl(),
                entity.getDomain());
    }
}
