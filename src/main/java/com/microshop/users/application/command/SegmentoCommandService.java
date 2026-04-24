package com.microshop.users.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.application.dto.SegmentoRequestDto;
import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.mapper.SegmentoMapper;
import com.microshop.users.infrastructure.persistence.repository.SegmentoRepository;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        var entity = segmentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(msg.get("segmento.not.found", id)));

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
        var entity = segmentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(msg.get("segmento.not.found", id)));
        entity.setActivo(false);
        segmentoRepository.save(entity);
        log.info("Segmento desactivado (soft delete): {}", id);
    }
}
