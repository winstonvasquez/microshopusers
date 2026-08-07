package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.entity.VendedorEntity;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.infrastructure.persistence.repository.VendedorRepository;
import com.microshop.users.application.dto.VendedorRequestDto;
import com.microshop.users.application.mapper.VendedorMapper;
import com.microshop.users.application.dto.VendedorResponseDto;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
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
    private final UserCompanyRepository userCompanyRepository;
    private final MessageSource messageSource;
    private final VendedorMapper vendedorMapper;

    /**
     * Registra un perfil de vendedor. {@code companyIdDelLlamante} es el tenant del solicitante y
     * solo puede ser null para un SUPERADMIN, en cuyo caso la empresa se deriva de la membresía
     * activa del usuario destino.
     *
     * <p>B07 (V37): además de sellar el {@code company_id} en la fila, se comprueba que el usuario
     * destino PERTENEZCA a la empresa del llamante. Sin esto, un ADMIN podía crear perfiles de
     * vendedor sobre usuarios de otras empresas.</p>
     */
    @Transactional
    public VendedorResponseDto registerSeller(Long usuarioId, VendedorRequestDto request, Long companyIdDelLlamante) {
        log.info("Registering seller for user {} (tenant {})", usuarioId, companyIdDelLlamante);

        if (vendedorRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new ConflictException(messageSource.getMessage("vendedor.already.exists", null, null));
        }

        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage("vendedor.user.not.found", null, null)));

        Long companyId = resolverEmpresaDelVendedor(usuarioId, companyIdDelLlamante);

        VendedorEntity vendedor = VendedorEntity.builder()
                .usuario(usuario)
                .companyId(companyId)
                .dniRuc(request.dniRuc())
                .telefonoContacto(request.telefonoContacto())
                .estadoAprobacion("PENDING")
                .build();

        VendedorEntity saved = vendedorRepository.save(vendedor);
        return vendedorMapper.mapToDto(saved);
    }

    /**
     * La empresa del vendedor es la del llamante, tras verificar que el usuario destino es de esa
     * misma empresa. Para un SUPERADMIN (sin scope) se deriva de la membresía activa del usuario.
     */
    private Long resolverEmpresaDelVendedor(Long usuarioId, Long companyIdDelLlamante) {
        if (companyIdDelLlamante != null) {
            if (!userCompanyRepository.existsByUsuarioIdAndCompanyId(usuarioId, companyIdDelLlamante)) {
                // 404 y no 403: no se confirma la existencia del usuario a un tenant ajeno.
                throw new NotFoundException(messageSource.getMessage("vendedor.user.not.found", null, null));
            }
            return companyIdDelLlamante;
        }
        return userCompanyRepository.findByUsuarioId(usuarioId).stream()
                .filter(UserCompanyEntity::isActive)
                .map(uc -> uc.getCompany().getId())
                .min(Long::compareTo)
                .orElseThrow(() -> new BusinessException(
                        "El usuario no pertenece a ninguna empresa activa: no se puede determinar el tenant del vendedor"));
    }

    /** B07: el cambio de estado se acota por empresa; antes aprobaba/rechazaba vendedores ajenos. */
    @Transactional
    public VendedorResponseDto updateSellerStatus(Long id, String status, Long companyId) {
        VendedorEntity vendedor = (companyId == null
                        ? vendedorRepository.findById(id)
                        : vendedorRepository.findByIdAndCompanyId(id, companyId))
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage("vendedor.not.found", null, null)));
        vendedor.setEstadoAprobacion(status);
        return vendedorMapper.mapToDto(vendedorRepository.save(vendedor));
    }
}
