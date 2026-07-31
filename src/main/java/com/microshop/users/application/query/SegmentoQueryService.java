package com.microshop.users.application.query;

import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.mapper.SegmentoMapper;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.infrastructure.persistence.entity.SegmentoEntity;
import com.microshop.users.infrastructure.persistence.repository.SegmentoRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentoQueryService {

    private final SegmentoRepository segmentoRepository;
    private final SegmentoMapper segmentoMapper;

    /** Sobrecarga corta (compat): mantiene la firma previa para llamadores que no filtran. */
    @Transactional(readOnly = true)
    public Page<SegmentoResponseDto> findAll(String search, Pageable pageable) {
        return findAll(search, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SegmentoResponseDto> findAll(String search, Boolean activo, String tipoCliente,
                                              Instant fechaDesde, Instant fechaHasta, Pageable pageable) {
        log.debug("Listando segmentos — search: {}, activo: {}, tipoCliente: {}", search, activo, tipoCliente);
        String searchParam = blankToEmpty(search);
        String tipoClienteParam = blankToEmpty(tipoCliente);
        Long companyId = resolveTenantScope();
        Page<SegmentoEntity> page = companyId == null
                ? segmentoRepository.findAllActiveWithSearch(searchParam, activo, tipoClienteParam, fechaDesde, fechaHasta, pageable)
                : segmentoRepository.findAllActiveWithSearchScoped(searchParam, activo, tipoClienteParam, fechaDesde, fechaHasta, companyId, pageable);
        return page.map(segmentoMapper::toDto);
    }

    /** Normaliza un parámetro String: null o en blanco -> cadena vacía (centinela usado en la query). */
    private static String blankToEmpty(String value) {
        return (value != null && !value.isBlank()) ? value : "";
    }

    @Transactional(readOnly = true)
    public SegmentoResponseDto findById(Long id) {
        log.debug("Buscando segmento ID: {}", id);
        Long companyId = resolveTenantScope();
        var found = companyId == null
                ? segmentoRepository.findById(id)
                : segmentoRepository.findByIdAndCompanyId(id, companyId);
        return found
                .filter(s -> s.isActivo())
                .map(segmentoMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Segmento no encontrado: " + id));
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
