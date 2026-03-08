package com.microshop.users.infrastructure.mapper;


import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.web.dto.CompanyRequestDto;
import com.microshop.users.infrastructure.web.dto.CompanyResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyEntity toEntity(CompanyRequestDto dto) {
        CompanyEntity entity = new CompanyEntity();
        entity.setName(dto.name());
        entity.setRuc(dto.ruc());
        entity.setActive(dto.active());
        return entity;
    }

    public CompanyResponseDto toDto(CompanyEntity entity) {
        return new CompanyResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getRuc(),
                entity.isActive());
    }
}
