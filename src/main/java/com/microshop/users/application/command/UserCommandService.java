package com.microshop.users.application.command;


import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.dto.UserRequestDto;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.infrastructure.persistence.entity.RolEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.application.mapper.UserMapper;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.infrastructure.persistence.repository.PersonaRepository;
import com.microshop.users.infrastructure.persistence.repository.RolRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.constants.AppConstants;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper msg;


    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        log.info("Creating user: {}", dto.username());
        validateNewUser(dto);

        var rol = findRol(dto.rolId());
        var persona = userMapper.toPersonaEntity(dto);
        persona = personaRepository.save(persona);

        var usuario = UsuarioEntity.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .rol(rol)
                .persona(persona)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("User created successfully: {}", usuario.getId());

        return userMapper.toDto(usuario);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        log.info("Updating user: {}", id);

        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("user.not.found", id)));

        validateUpdateUser(usuario, dto);

        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());

        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (!usuario.getRol().getId().equals(dto.rolId())) {
            var rol = findRol(dto.rolId());
            // Aunque este endpoint acepta ADMIN o SUPERADMIN (ver SecurityConfig), otorgar
            // el rol SUPERADMIN en si mismo queda reservado a un caller que YA es SUPERADMIN —
            // si no, un ADMIN normal podria auto-otorgarse SUPERADMIN via este update generico,
            // saltandose la restriccion mas estricta del endpoint dedicado PUT /users/{id}/role.
            if (AppConstants.Seguridad.SUPERADMIN.equalsIgnoreCase(rol.getNombre())
                    && !SecurityContextUtils.isSuperAdmin()) {
                throw new AccessDeniedException(
                        "Solo un SUPERADMIN puede otorgar el rol SUPERADMIN");
            }
            usuario.setRol(rol);
        }

        var persona = usuario.getPersona();
        userMapper.updatePersonaFromDto(dto, persona);
        personaRepository.save(persona);

        usuario = usuarioRepository.save(usuario);
        log.info("User updated successfully: {}", id);

        return userMapper.toDto(usuario);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException(msg.get("user.not.found", id));
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Cambia SOLO el rol de un usuario (endpoint dedicado, separado de {@link #updateUser}
     * para poder proteger la asignacion de SUPERADMIN con una restriccion mas estricta
     * que el resto del CRUD de usuarios — ver SecurityConfig).
     */
    @Transactional
    public UserResponseDto changeRole(Long id, String rolNombre) {
        log.info("Changing role for user {} to {}", id, rolNombre);

        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        var rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("user.role.not.found", rolNombre)));

        usuario.setRol(rol);
        usuario = usuarioRepository.save(usuario);
        log.info("Role changed successfully for user {}: now {}", id, rolNombre);

        return userMapper.toDto(usuario);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        log.info("Password changed successfully for user: {}", username);
    }

    private RolEntity findRol(Long rolId) {
        return rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException(msg.get("user.role.not.found", rolId)));
    }

    private void validateNewUser(UserRequestDto dto) {
        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException(msg.get("user.username.exists", dto.username()));
        }
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException(msg.get("user.email.exists", dto.email()));
        }
        if (personaRepository.findByNumeroDocumento(dto.numeroDocumento()).isPresent()) {
            throw new IllegalArgumentException(msg.get("user.document.exists", dto.numeroDocumento()));
        }
    }

    private void validateUpdateUser(UsuarioEntity current, UserRequestDto dto) {
        if (!current.getUsername().equals(dto.username()) && usuarioRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException(msg.get("user.username.exists", dto.username()));
        }
        if (!current.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException(msg.get("user.email.exists", dto.email()));
        }

        var persona = current.getPersona();
        if (!persona.getNumeroDocumento().equals(dto.numeroDocumento()) &&
                personaRepository.findByNumeroDocumento(dto.numeroDocumento()).isPresent()) {
            throw new IllegalArgumentException(msg.get("user.document.exists", dto.numeroDocumento()));
        }
    }
}
