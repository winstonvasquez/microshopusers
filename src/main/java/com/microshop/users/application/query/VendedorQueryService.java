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
import com.microshop.users.shared.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorQueryService {

    private final VendedorRepository vendedorRepository;
    private final MessageSource messageSource;

    /*
     * B07 (V37): todas las consultas se acotan por empresa. El parámetro `companyId` es el tenant
     * del llamante y solo puede ser null para un SUPERADMIN (bypass explícito). Antes de esto,
     * `getAllSellers()` hacía un `findAll()` que devolvía los vendedores de TODA la plataforma y
     * `getSellerById` resolvía cualquier id sin comprobar la empresa.
     *
     * Se lanza NotFoundException (404) y no AccessDenied (403) para no confirmarle a un tenant
     * ajeno que el id existe.
     */

    @Transactional(readOnly = true)
    public VendedorResponseDto getSellerByUsuarioId(Long usuarioId, Long companyId) {
        return (companyId == null
                        ? vendedorRepository.findByUsuarioId(usuarioId)
                        : vendedorRepository.findByUsuarioIdAndCompanyId(usuarioId, companyId))
                .map(this::mapToDto)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(
                        "vendedor.profile.not.found.for.user", null, LocaleContextHolder.getLocale())));
    }

    @Transactional(readOnly = true)
    public VendedorResponseDto getSellerById(Long id, Long companyId) {
        return (companyId == null
                        ? vendedorRepository.findById(id)
                        : vendedorRepository.findByIdAndCompanyId(id, companyId))
                .map(this::mapToDto)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(
                        "vendedor.not.found", null, LocaleContextHolder.getLocale())));
    }

    @Transactional(readOnly = true)
    public List<VendedorResponseDto> getAllSellers(Long companyId) {
        return (companyId == null
                        ? vendedorRepository.findAll()
                        : vendedorRepository.findByCompanyId(companyId)).stream()
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
