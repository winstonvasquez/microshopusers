package com.microshop.users.application.query;

import com.microshop.users.application.dto.RolResponseDto;
import com.microshop.users.infrastructure.persistence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de consulta para roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolQueryService {

    private final RolRepository rolRepository;

    /**
     * Obtener todos los roles
     */
    @Transactional(readOnly = true)
    public List<RolResponseDto> findAll() {
        log.debug("Obteniendo todos los roles");
        return rolRepository.findAll().stream()
                .map(rol -> new RolResponseDto(
                        rol.getId(),
                        rol.getNombre(),
                        rol.getDescripcion()))
                .collect(Collectors.toList());
    }
}
