package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.entity.VendedorEntity;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.infrastructure.persistence.repository.VendedorRepository;
import com.microshop.users.application.dto.VendedorRequestDto;
import com.microshop.users.application.dto.VendedorResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorCommandService {

    private final VendedorRepository vendedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageSource messageSource;

    @Transactional
    public VendedorResponseDto registerSeller(Long usuarioId, VendedorRequestDto request) {
        log.info("Registering seller for user {}", usuarioId);

        if (vendedorRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new IllegalArgumentException(messageSource.getMessage("vendedor.already.exists", null, null));
        }

        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("vendedor.user.not.found", null, null)));

        VendedorEntity vendedor = VendedorEntity.builder()
                .usuario(usuario)
                .dniRuc(request.dniRuc())
                .telefonoContacto(request.telefonoContacto())
                .estadoAprobacion("PENDING")
                .build();

        VendedorEntity saved = vendedorRepository.save(vendedor);
        return mapToDto(saved);
    }

    @Transactional
    public VendedorResponseDto updateSellerStatus(Long id, String status) {
        VendedorEntity vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("vendedor.not.found", null, null)));
        vendedor.setEstadoAprobacion(status);
        return mapToDto(vendedorRepository.save(vendedor));
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
