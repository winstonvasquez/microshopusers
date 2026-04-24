package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.department.DepartmentResponseDto;
import com.microshop.rrhh.application.mapper.DepartmentMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.infrastructure.persistence.repository.DepartmentRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DepartmentQueryService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final TenantContext tenantContext;
    private final MessageSource messageSource;

    public List<DepartmentResponseDto> getAllDepartments() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.findByTenantId(tenantId).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    public List<DepartmentResponseDto> getActiveDepartments() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.findByTenantIdAndActivo(tenantId, true).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    public DepartmentResponseDto getDepartmentById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Department department = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("department.not.found", null, Locale.getDefault())));
        return departmentMapper.toDto(department);
    }

    public List<DepartmentResponseDto> getDepartmentTree() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.findByTenantIdAndParentIsNull(tenantId).stream()
                .map(dept -> departmentMapper.toDto(dept, true))
                .toList();
    }

    public List<DepartmentResponseDto> getSubDepartments(Long parentId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.findByTenantIdAndParentId(tenantId, parentId).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    public List<DepartmentResponseDto> searchDepartments(String term) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.searchByTenantIdAndTerm(tenantId, term).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    public long countDepartments() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.countByTenantId(tenantId);
    }

    public long countActiveDepartments() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return departmentRepository.countByTenantIdAndActivo(tenantId, true);
    }
}
