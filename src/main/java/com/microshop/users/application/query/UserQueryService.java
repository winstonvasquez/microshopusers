package com.microshop.users.application.query;

import com.microshop.users.application.mapper.UserMapper;
import com.microshop.users.application.dto.UserExportRowDto;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.infrastructure.persistence.entity.PolicyRoleEntity;
import com.microshop.users.infrastructure.persistence.repository.PolicyRoleRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryService {

    private final UsuarioRepository usuarioRepository;
    private final PolicyRoleRepository policyRoleRepository;
    private final UserMapper userMapper;

    /** Sobrecarga corta (compatibilidad hacia atrás): sin filtros nuevos de búsqueda/estado/documento. */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable, Long companyId, Long rolId,
            Instant fechaCreacionDesde, Instant fechaCreacionHasta) {
        return findAll(pageable, companyId, null, rolId, null, null, fechaCreacionDesde, fechaCreacionHasta);
    }

    /**
     * Lista usuarios paginados con filtros avanzados. Si {@code companyId} es no-nulo, acota a
     * esa empresa (defensa cross-tenant: un ADMIN normal no debe ver usuarios de otras empresas).
     * Pasar {@code null} solo desde llamadas de SUPERADMIN. Usado tanto por admin/users como por
     * admin/reportes-clientes (mismo endpoint GET /api/users).
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable, Long companyId, String search, Long rolId,
            Boolean activo, String tipoDocumento, Instant fechaCreacionDesde, Instant fechaCreacionHasta) {
        String term = AppUtils.searchTermOrNull(search);
        String tipoDoc = AppUtils.searchTermOrNull(tipoDocumento);
        log.debug("Fetching users with pagination: {}, companyId={}, search={}, rolId={}, activo={}, tipoDocumento={}, "
                        + "fechaCreacionDesde={}, fechaCreacionHasta={}",
                pageable, companyId, term, rolId, activo, tipoDoc, fechaCreacionDesde, fechaCreacionHasta);
        Page<com.microshop.users.infrastructure.persistence.entity.UsuarioEntity> page = companyId != null
                ? usuarioRepository.findByCompanyIdFiltered(companyId, term, rolId, activo, tipoDoc, fechaCreacionDesde, fechaCreacionHasta, pageable)
                : usuarioRepository.findAllFiltered(term, rolId, activo, tipoDoc, fechaCreacionDesde, fechaCreacionHasta, pageable);
        return page.map(userMapper::toDto);
    }

    /** Ver {@link #findAll(Pageable, Long)} — misma acotacion, sin paginar. */
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll(Long companyId) {
        log.debug("Fetching all users, companyId={}", companyId);
        var usuarios = companyId != null
                ? usuarioRepository.findAllByCompanyId(companyId)
                : usuarioRepository.findAll();
        return usuarios.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return usuarioRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return usuarioRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    // findByRol(Long) se eliminó: era el único camino de listado sin acotar por empresa y dejaba
    // GET /api/users/by-rol/{rolId} filtrando PII cross-tenant. Su llamador (UserController) usa
    // ahora findAll(...) con resolveTenantScope() y el filtro rolId, que respeta las membresías.

    /**
     * Obtiene todos los usuarios con los campos que necesita el reporte de
     * exportación (admin/reportes-clientes): activo, fecha de creación cruda
     * y nombres/apellidos, que {@link UserResponseDto} no expone.
     */
    @Transactional(readOnly = true)
    public List<UserExportRowDto> findAllForExport(Long companyId) {
        log.debug("Fetching all users for export report, companyId={}", companyId);
        var usuarios = companyId != null
                ? usuarioRepository.findAllByCompanyId(companyId)
                : usuarioRepository.findAll();
        return usuarios.stream()
                .map(u -> {
                    var persona = u.getPersona();
                    return new UserExportRowDto(
                            u.getId(),
                            u.getUsername(),
                            u.getEmail(),
                            persona != null ? persona.getNombres() : null,
                            persona != null ? persona.getApellidos() : null,
                            u.isActivo(),
                            u.getFechaCreacion());
                })
                .collect(Collectors.toList());
    }

    /** Obtiene el rol y las políticas asignadas para el usuario autenticado. */
    @Transactional(readOnly = true)
    public Map<String, Object> getMyPermissions(String username) {
        log.debug("Fetching permissions for user: {}", username);

        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();

        var rol = usuario.getRol();
        boolean isStandardCustomer = "CUSTOMER".equalsIgnoreCase(rol.getNombre())
                || "USER".equalsIgnoreCase(rol.getNombre());

        List<PolicyRoleEntity> policyRoles = policyRoleRepository.findByRolIdWithPolicy(rol.getId());

        var permissions = policyRoles.stream()
                .map(pr -> Map.of(
                        "codigo", pr.getPolicy().getCodigo(),
                        "nombre", pr.getPolicy().getNombre(),
                        "efecto", pr.getPolicy().getEfecto()
                ))
                .toList();

        return Map.of(
                "rolNombre", rol.getNombre(),
                "rolDescripcion", rol.getDescripcion() != null ? rol.getDescripcion() : "",
                "isStandardCustomer", isStandardCustomer,
                "permissions", permissions
        );
    }
}
