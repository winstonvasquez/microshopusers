package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.mapper.EmployeeMapper;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.config.security.TenantContext;
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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmployeeQueryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final TenantContext tenantContext;
    private final MessageSource messageSource;

    public List<EmployeeResponseDto> getAllEmployees() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return employeeRepository.findByTenantId(tenantId).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDto getEmployeeById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("employee.not.found", null, Locale.getDefault())));
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeResponseDto> getEmployeesByStatus(Employee.EmployeeStatus status) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return employeeRepository.findByTenantIdAndEstado(tenantId, status).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDto> searchEmployees(String searchTerm) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return employeeRepository.searchByTenantIdAndTerm(tenantId, searchTerm).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    /** Listado paginado server-side con búsqueda + filtro de estado opcionales. */
    public Page<EmployeeResponseDto> getEmployeesPaged(String search, Employee.EmployeeStatus estado, Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        return employeeRepository.searchPaged(tenantId, term, estado, pageable)
                .map(employeeMapper::toDto);
    }

    public long countEmployees() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return employeeRepository.countByTenantId(tenantId);
    }

    public long countActiveEmployees() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return employeeRepository.countByTenantIdAndEstado(tenantId, Employee.EmployeeStatus.ACTIVO);
    }

    /** Resuelve el ID del empleado vinculado al usuario autenticado actual (portal de autoservicio). */
    public Long resolveCurrentEmployeeId() {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();
        if (userId == null) {
            throw new NotFoundException("Usuario no autenticado");
        }
        return employeeRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(e -> e.getId())
                .orElseThrow(() -> new NotFoundException("No se encontró un empleado vinculado al usuario actual"));
    }
}
