package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.position.PositionResponseDto;
import com.microshop.rrhh.application.mapper.PositionMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Position;
import com.microshop.rrhh.infrastructure.persistence.repository.PositionRepository;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PositionQueryService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final TenantContext tenantContext;
    private final MessageSource messageSource;

    /** Sobrecarga corta sin filtros avanzados nuevos (delega con null = no filtra). */
    public Page<PositionResponseDto> getPositionsPaged(String search, Long departmentId, Pageable pageable) {
        return getPositionsPaged(search, departmentId, null, null, pageable);
    }

    /** Listado paginado de puestos con filtros avanzados: estado (activo/inactivo) y nivel del puesto. */
    public Page<PositionResponseDto> getPositionsPaged(String search, Long departmentId, Boolean activo,
            String nivel, Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        String nivelTerm = AppUtils.searchTermOrNull(nivel);
        return positionRepository.searchPaged(tenantId, term, departmentId, activo, nivelTerm, pageable)
                .map(positionMapper::toDto);
    }

    public List<PositionResponseDto> getAllPositions() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return positionRepository.findByTenantId(tenantId).stream()
                .map(positionMapper::toDto)
                .toList();
    }

    public List<PositionResponseDto> getActivePositions() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return positionRepository.findByTenantIdAndActivo(tenantId, true).stream()
                .map(positionMapper::toDto)
                .toList();
    }

    public PositionResponseDto getPositionById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Position position = positionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("position.not.found", null, Locale.getDefault())));
        return positionMapper.toDto(position);
    }

    public List<PositionResponseDto> getPositionsByDepartment(Long departmentId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return positionRepository.findByTenantIdAndDepartmentId(tenantId, departmentId).stream()
                .map(positionMapper::toDto)
                .toList();
    }

    public List<PositionResponseDto> searchPositions(String term) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return positionRepository.searchByTenantIdAndTerm(tenantId, term).stream()
                .map(positionMapper::toDto)
                .toList();
    }

    public long countPositions() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return positionRepository.countByTenantId(tenantId);
    }
}
