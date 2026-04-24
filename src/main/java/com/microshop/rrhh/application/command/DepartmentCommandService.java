package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.department.DepartmentRequestDto;
import com.microshop.rrhh.application.dto.department.DepartmentResponseDto;
import com.microshop.rrhh.application.mapper.DepartmentMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.DepartmentRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class DepartmentCommandService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public DepartmentResponseDto createDepartment(@Valid DepartmentRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        departmentRepository.findByCodigoAndTenantId(request.codigo(), tenantId)
                .ifPresent(d -> {
                    throw new ConflictException(msg.get("department.codigo.duplicate", request.codigo()));
                });

        Department department = departmentMapper.toEntity(request, tenantId);

        if (request.managerId() != null) {
            Employee manager = employeeRepository.findByIdAndTenantId(request.managerId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));
            department.setManager(manager);
        }

        if (request.parentId() != null) {
            Department parent = departmentRepository.findByIdAndTenantId(request.parentId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("department.parent.not.found")));
            department.setParent(parent);
        }

        Department saved = departmentRepository.save(department);
        log.info("Departamento creado: {} - Tenant: {}", saved.getId(), tenantId);
        return departmentMapper.toDto(saved);
    }

    public DepartmentResponseDto updateDepartment(Long id, @Valid DepartmentRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Department department = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("department.not.found")));

        if (!department.getCodigo().equals(request.codigo())) {
            departmentRepository.findByCodigoAndTenantId(request.codigo(), tenantId)
                    .ifPresent(d -> {
                        throw new ConflictException(msg.get("department.codigo.duplicate", request.codigo()));
                    });
        }

        departmentMapper.updateEntity(department, request);

        if (request.managerId() != null) {
            Employee manager = employeeRepository.findByIdAndTenantId(request.managerId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BusinessException(msg.get("department.parent.self"));
            }
            Department parent = departmentRepository.findByIdAndTenantId(request.parentId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("department.parent.not.found")));
            department.setParent(parent);
        } else {
            department.setParent(null);
        }

        Department updated = departmentRepository.save(department);
        log.info("Departamento actualizado: {} - Tenant: {}", updated.getId(), tenantId);
        return departmentMapper.toDto(updated);
    }

    public void deactivateDepartment(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Department department = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("department.not.found")));

        department.setActivo(false);
        departmentRepository.save(department);
        log.info("Departamento desactivado: {} - Tenant: {}", id, tenantId);
    }

    public void deleteDepartment(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Department department = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("department.not.found")));

        departmentRepository.delete(department);
        log.info("Departamento eliminado: {} - Tenant: {}", id, tenantId);
    }
}
