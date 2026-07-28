package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.employee.EmployeeRequestDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.mapper.EmployeeMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Position;
import com.microshop.rrhh.infrastructure.persistence.repository.DepartmentRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.PositionRepository;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.ImagenBinariaUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class EmployeeCommandService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public EmployeeResponseDto createEmployee(@Valid EmployeeRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        employeeRepository.findByCodigoEmpleadoAndTenantId(request.codigoEmpleado(), tenantId)
                .ifPresent(e -> {
                    throw new ConflictException(msg.get("employee.codigo.duplicate", request.codigoEmpleado()));
                });

        employeeRepository.findByDocumentoIdentidadAndTenantId(request.documentoIdentidad(), tenantId)
                .ifPresent(e -> {
                    throw new ConflictException(msg.get("employee.documento.duplicate", request.documentoIdentidad()));
                });

        Employee employee = employeeMapper.toEntity(request, tenantId);
        resolveRelations(employee, request, tenantId);

        Employee saved = employeeRepository.save(employee);
        log.info("Empleado creado: {} - Tenant: {}", saved.getId(), tenantId);
        return employeeMapper.toDto(saved);
    }

    public EmployeeResponseDto updateEmployee(Long id, @Valid EmployeeRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));

        if (!employee.getCodigoEmpleado().equals(request.codigoEmpleado())) {
            employeeRepository.findByCodigoEmpleadoAndTenantId(request.codigoEmpleado(), tenantId)
                    .ifPresent(e -> {
                        throw new ConflictException(msg.get("employee.codigo.duplicate", request.codigoEmpleado()));
                    });
        }

        employeeMapper.updateEntity(employee, request);
        resolveRelations(employee, request, tenantId);

        Employee updated = employeeRepository.save(employee);
        log.info("Empleado actualizado: {} - Tenant: {}", updated.getId(), tenantId);
        return employeeMapper.toDto(updated);
    }

    /**
     * Almacena la foto binaria del empleado. Valida MIME, tamaño (máx 500 KB) y magic bytes.
     * Calcula el MD5 del binario como ETag para caché HTTP y anula la URL externa previa.
     * El empleado se busca acotado por tenant (defensa IDOR cross-tenant).
     */
    public EmployeeResponseDto uploadFoto(Long id, MultipartFile file) {
        byte[] bytes = ImagenBinariaUtils.validarYExtraer(file);
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));

        employee.setFotoData(bytes);
        employee.setFotoMime(file.getContentType());
        employee.setFotoEtag(DigestUtils.md5DigestAsHex(bytes));
        employee.setFotoSize(bytes.length);
        employee.setFotoUrl(null);

        Employee saved = employeeRepository.save(employee);
        log.info("Foto actualizada para empleado {} - Tenant: {} ({} bytes)", id, tenantId, bytes.length);
        return employeeMapper.toDto(saved);
    }

    /**
     * Elimina la foto binaria del empleado: borra el blob y TODOS sus metadatos
     * (mime, etag, size) para que {@code serveFoto} responda 404 y la entidad quede
     * consistente. El tenant se resuelve del JWT y el lookup va acotado por él.
     */
    public EmployeeResponseDto deleteFoto(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));

        employee.setFotoData(null);
        employee.setFotoMime(null);
        employee.setFotoEtag(null);
        employee.setFotoSize(null);
        employee.setFotoUrl(null);

        Employee saved = employeeRepository.save(employee);
        log.info("Foto eliminada para empleado {} - Tenant: {}", id, tenantId);
        return employeeMapper.toDto(saved);
    }

    public void deactivateEmployee(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));

        employee.setEstado(Employee.EmployeeStatus.INACTIVO);
        employeeRepository.save(employee);
        log.info("Empleado desactivado: {} - Tenant: {}", id, tenantId);
    }

    public void deleteEmployee(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));

        employeeRepository.delete(employee);
        log.info("Empleado eliminado: {} - Tenant: {}", id, tenantId);
    }

    private void resolveRelations(Employee employee, EmployeeRequestDto request, Long tenantId) {
        if (request.departmentId() != null) {
            Department dept = departmentRepository.findByIdAndTenantId(request.departmentId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("department.not.found")));
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        if (request.positionId() != null) {
            Position pos = positionRepository.findByIdAndTenantId(request.positionId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("position.not.found")));
            employee.setPosition(pos);
        } else {
            employee.setPosition(null);
        }

        if (request.supervisorId() != null) {
            Employee sup = employeeRepository.findByIdAndTenantId(request.supervisorId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("employee.supervisor.not.found")));
            employee.setSupervisor(sup);
        } else {
            employee.setSupervisor(null);
        }
    }
}
