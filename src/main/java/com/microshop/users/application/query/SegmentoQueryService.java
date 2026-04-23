package com.microshop.users.application.query;

import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.mapper.SegmentoMapper;
import com.microshop.users.infrastructure.persistence.repository.SegmentoRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentoQueryService {

    private final SegmentoRepository segmentoRepository;
    private final SegmentoMapper segmentoMapper;

    @Transactional(readOnly = true)
    public Page<SegmentoResponseDto> findAll(String search, Pageable pageable) {
        log.debug("Listando segmentos activos — search: {}", search);
        String searchParam = (search != null && !search.isBlank()) ? search : "";
        return segmentoRepository
                .findAllActiveWithSearch(searchParam, pageable)
                .map(segmentoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public SegmentoResponseDto findById(Long id) {
        log.debug("Buscando segmento ID: {}", id);
        return segmentoRepository.findById(id)
                .filter(s -> s.isActivo())
                .map(segmentoMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Segmento no encontrado: " + id));
    }
}
