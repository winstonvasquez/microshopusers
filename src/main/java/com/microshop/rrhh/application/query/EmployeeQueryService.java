package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.application.mapper.EmployeeMapper;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.AppUtils;
import com.microshop.users.shared.util.ImagenBinariaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /**
     * Sirve la foto binaria del empleado con headers ETag y Cache-Control.
     * Endpoint PÚBLICO (se pinta en fichas y directorios sin sesión activa), por eso NO
     * usa TenantContext: solo expone el binario, nunca datos personales del empleado.
     * Retorna 304 si el ETag del cliente sigue vigente y 404 si no hay foto en BD.
     */
    public ResponseEntity<byte[]> serveFoto(Long id, String ifNoneMatch) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null || employee.getFotoData() == null) {
            return ResponseEntity.notFound().build();
        }

        String etag = "\"" + employee.getFotoEtag() + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, ImagenBinariaUtils.resolveContentType(employee.getFotoMime()))
                .header(HttpHeaders.ETAG, etag)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(employee.getFotoData());
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
        return getEmployeesPaged(search, estado, null, null, null, pageable);
    }

    /** Listado paginado server-side con búsqueda + filtro de estado + departamento + rango de fecha de ingreso opcionales. */
    public Page<EmployeeResponseDto> getEmployeesPaged(String search, Employee.EmployeeStatus estado,
                                                        Long departmentId, LocalDate fechaIngresoDesde,
                                                        LocalDate fechaIngresoHasta, Pageable pageable) {
        return getEmployeesPaged(search, estado, departmentId, null, null, null, null, null, null, null, null,
                fechaIngresoDesde, fechaIngresoHasta, null, null, null, null, pageable);
    }

    /**
     * Listado paginado server-side con TODOS los filtros avanzados (2026-07-27): búsqueda, estado,
     * departamento, puesto, supervisor, tipo de documento, sistema previsional, AFP, género, estado
     * civil, tipo de contrato vigente y rangos de fecha de ingreso / salida / nacimiento.
     */
    public Page<EmployeeResponseDto> getEmployeesPaged(String search, Employee.EmployeeStatus estado,
                                                        Long departmentId, Long positionId, Long supervisorId,
                                                        String tipoDocumento, String sistemaPrevisional, String afpNombre,
                                                        Employee.Gender genero, Employee.MaritalStatus estadoCivil,
                                                        Contract.ContractType tipoContrato,
                                                        LocalDate fechaIngresoDesde, LocalDate fechaIngresoHasta,
                                                        LocalDate fechaSalidaDesde, LocalDate fechaSalidaHasta,
                                                        LocalDate fechaNacimientoDesde, LocalDate fechaNacimientoHasta,
                                                        Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String term = AppUtils.searchTermOrNull(search);
        return employeeRepository.searchPaged(tenantId, term, estado, departmentId, positionId, supervisorId,
                        AppUtils.searchTermOrNull(tipoDocumento), AppUtils.searchTermOrNull(sistemaPrevisional),
                        AppUtils.searchTermOrNull(afpNombre), genero, estadoCivil,
                        tipoContrato, Contract.ContractStatus.ACTIVO,
                        fechaIngresoDesde, fechaIngresoHasta, fechaSalidaDesde, fechaSalidaHasta,
                        fechaNacimientoDesde, fechaNacimientoHasta, pageable)
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
            throw new NotFoundException("El usuario no tiene un empleado asociado");
        }
        return employeeRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(e -> e.getId())
                .orElseThrow(() -> new NotFoundException("El usuario no tiene un empleado asociado"));
    }
}
