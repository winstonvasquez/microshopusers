package com.microshop.users.application.query;
import com.microshop.users.infrastructure.persistence.entity.VendedorEntity;
import com.microshop.users.infrastructure.persistence.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.microshop.users.application.dto.VendedorResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorQueryService {

    private final VendedorRepository vendedorRepository;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public VendedorResponseDto getSellerByUsuarioId(Long usuarioId) {
        return vendedorRepository.findByUsuarioId(usuarioId)
                .map(this::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("vendedor.profile.not.found.for.user", null, LocaleContextHolder.getLocale())));
    }

    @Transactional(readOnly = true)
    public VendedorResponseDto getSellerById(Long id) {
        return vendedorRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("vendedor.not.found", null, LocaleContextHolder.getLocale())));
    }

    @Transactional(readOnly = true)
    public List<VendedorResponseDto> getAllSellers() {
        return vendedorRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private VendedorResponseDto mapToDto(VendedorEntity entity) {
        return VendedorResponseDto.builder()
                .id(entity.getId())
                .dniRuc(entity.getDniRuc())
                .telefonoContacto(entity.getTelefonoContacto())
                .estadoAprobacion(entity.getEstadoAprobacion())
                .usuarioId(entity.getUsuario().getId())
                .username(entity.getUsuario().getUsername())
                .email(entity.getUsuario().getEmail())
                .build();
    }
}
