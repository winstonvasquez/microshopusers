package com.microshop.users.infrastructure.mapper;

import com.microshop.users.infrastructure.web.dto.UserCompanyResponseDto;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserCompanyMapper {

    public UserCompanyResponseDto toDto(UserCompanyEntity entity) {
       List<Long> roleIds = entity.getRoles().stream()
                .map(role -> role.getRol().getId())
                .collect(Collectors.toList());

        return new UserCompanyResponseDto(
                entity.getId(),
                entity.getUsuario().getId(),
                entity.getCompany().getId(),
                roleIds);
    }
}
