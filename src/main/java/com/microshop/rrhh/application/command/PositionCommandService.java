package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.position.PositionRequestDto;
import com.microshop.rrhh.application.dto.position.PositionResponseDto;
import com.microshop.rrhh.application.mapper.PositionMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Position;
import com.microshop.rrhh.infrastructure.persistence.repository.DepartmentRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.PositionRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class PositionCommandService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionMapper positionMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public PositionResponseDto createPosition(@Valid PositionRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        positionRepository.findByCodigoAndTenantId(request.codigo(), tenantId)
                .ifPresent(p -> {
                    throw new ConflictException(msg.get("position.codigo.duplicate", request.codigo()));
                });

        Department department = departmentRepository.findByIdAndTenantId(request.departmentId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("position.department.not.found")));

        validateSalaryRange(request);

        Position position = positionMapper.toEntity(request, tenantId);
        position.setDepartment(department);

        Position saved = positionRepository.save(position);
        log.info("Puesto creado: {} - Tenant: {}", saved.getId(), tenantId);
        return positionMapper.toDto(saved);
    }

    public PositionResponseDto updatePosition(Long id, @Valid PositionRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Position position = positionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("position.not.found")));

        if (!position.getCodigo().equals(request.codigo())) {
            positionRepository.findByCodigoAndTenantId(request.codigo(), tenantId)
                    .ifPresent(p -> {
                        throw new ConflictException(msg.get("position.codigo.duplicate", request.codigo()));
                    });
        }

        Department department = departmentRepository.findByIdAndTenantId(request.departmentId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("position.department.not.found")));

        validateSalaryRange(request);

        positionMapper.updateEntity(position, request);
        position.setDepartment(department);

        Position updated = positionRepository.save(position);
        log.info("Puesto actualizado: {} - Tenant: {}", updated.getId(), tenantId);
        return positionMapper.toDto(updated);
    }

    public void deactivatePosition(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Position position = positionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("position.not.found")));

        position.setActivo(false);
        positionRepository.save(position);
        log.info("Puesto desactivado: {} - Tenant: {}", id, tenantId);
    }

    public void deletePosition(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Position position = positionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("position.not.found")));

        positionRepository.delete(position);
        log.info("Puesto eliminado: {} - Tenant: {}", id, tenantId);
    }

    private void validateSalaryRange(PositionRequestDto request) {
        if (request.salarioMinimo() != null && request.salarioMaximo() != null
                && request.salarioMinimo().compareTo(request.salarioMaximo()) > 0) {
            throw new BusinessException(msg.get("position.salario.range.invalid"));
        }
    }
}
