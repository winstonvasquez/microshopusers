package com.microshop.users.application.query;

import com.microshop.users.application.mapper.UserMapper;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryService {

    private final UsuarioRepository usuarioRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(Pageable pageable) {
        log.debug("Fetching users with pagination: {}", pageable);
        return usuarioRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        log.debug("Fetching all users");
        return usuarioRepository.findAll().stream()
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

    @Transactional(readOnly = true)
    public List<UserResponseDto> findByRol(Long rolId) {
        log.debug("Fetching users by role: {}", rolId);
        return usuarioRepository.findByRolId(rolId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
