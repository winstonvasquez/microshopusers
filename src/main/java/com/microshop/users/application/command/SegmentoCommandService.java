package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.dto.SegmentoRequestDto;
import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.mapper.SegmentoMapper;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.infrastructure.persistence.entity.SegmentoEntity;
import com.microshop.users.infrastructure.persistence.repository.SegmentoRepository;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentoCommandService {

    private final SegmentoRepository segmentoRepository;
    private final SegmentoMapper segmentoMapper;
    private final MessageHelper msg;

    @Transactional
    public SegmentoResponseDto create(SegmentoRequestDto dto) {
        log.info("Creando segmento: {}", dto.nombre());
        if (segmentoRepository.existsByNombreAndActivoTrue(dto.nombre())) {
            throw new ConflictException(msg.get("segmento.nombre.exists", dto.nombre()));
        }
        var entity = segmentoMapper.toEntity(dto);
        entity = segmentoRepository.save(entity);
        log.info("Segmento creado con ID: {}", entity.getId());
        return segmentoMapper.toDto(entity);
    }

    @Transactional
    public SegmentoResponseDto update(Long id, SegmentoRequestDto dto) {
        log.info("Actualizando segmento ID: {}", id);
        var entity = findScopedOrThrow(id);

        if (segmentoRepository.existsByNombreAndActivoTrueAndIdNot(dto.nombre(), id)) {
            throw new ConflictException(msg.get("segmento.nombre.exists", dto.nombre()));
        }

        segmentoMapper.updateEntity(entity, dto);
        entity = segmentoRepository.save(entity);
        log.info("Segmento actualizado: {}", id);
        return segmentoMapper.toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Eliminando segmento ID: {}", id);
        var entity = findScopedOrThrow(id);
        entity.setActivo(false);
        segmentoRepository.save(entity);
        log.info("Segmento desactivado (soft delete): {}", id);
    }

    /**
     * Lookup scoped por tenant (defensa IDOR) para update/delete. Si el segmento no pertenece al
     * tenant resuelto, falla con NotFoundException (nunca 403) para no confirmar existencia ajena.
     */
    private SegmentoEntity findScopedOrThrow(Long id) {
        Long companyId = resolveTenantScope();
        var found = companyId == null
                ? segmentoRepository.findById(id)
                : segmentoRepository.findByIdAndCompanyId(id, companyId);
        return found.orElseThrow(() -> new NotFoundException(msg.get("segmento.not.found", id)));
    }

    /**
     * Resuelve el companyId por el que acotar, fail-closed (misma semántica que
     * UserController.resolveTenantScope()). SUPERADMIN hace bypass intencional (retorna null).
     */
    private Long resolveTenantScope() {
        if (SecurityContextUtils.isSuperAdmin()) {
            return null;
        }
        Long companyId = SecurityContextUtils.currentCompanyId();
        if (companyId == null) {
            throw new AccessDeniedException("JWT sin claim companyId — no se puede acotar por tenant");
        }
        return companyId;
    }
}
