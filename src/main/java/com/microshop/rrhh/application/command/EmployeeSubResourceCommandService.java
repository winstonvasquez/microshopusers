package com.microshop.rrhh.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.application.dto.employee.*;
import com.microshop.rrhh.application.mapper.EmployeeSubResourceMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
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
public class EmployeeSubResourceCommandService {

    private final EmployeeRepository employeeRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final DependentRepository dependentRepository;
    private final DocumentRepository documentRepository;
    private final SalaryRepository salaryRepository;
    private final TenantContext tenantContext;
    private final MessageHelper msg;
    private final EmployeeSubResourceMapper mapper;

    // ── Emergency Contacts ────────────────────────────────────────────────────

    public EmergencyContactDto.Response createEmergencyContact(Long employeeId, @Valid EmergencyContactDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Employee employee = findEmployee(employeeId, tenantId);

        EmergencyContact entity = EmergencyContact.builder()
                .tenantId(tenantId)
                .employee(employee)
                .nombreCompleto(request.nombreCompleto())
                .relacion(request.relacion())
                .telefono(request.telefono())
                .telefonoAlternativo(request.telefonoAlternativo())
                .direccion(request.direccion())
                .esPrincipal(request.esPrincipal() != null ? request.esPrincipal() : false)
                .build();

        EmergencyContact saved = emergencyContactRepository.save(entity);
        log.info("Contacto emergencia creado: {} para empleado {} - Tenant: {}", saved.getId(), employeeId, tenantId);
        return mapper.toEmergencyContactDto(saved);
    }

    public EmergencyContactDto.Response updateEmergencyContact(Long id, @Valid EmergencyContactDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        EmergencyContact entity = emergencyContactRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("emergency.contact.not.found")));

        entity.setNombreCompleto(request.nombreCompleto());
        entity.setRelacion(request.relacion());
        entity.setTelefono(request.telefono());
        entity.setTelefonoAlternativo(request.telefonoAlternativo());
        entity.setDireccion(request.direccion());
        if (request.esPrincipal() != null) entity.setEsPrincipal(request.esPrincipal());

        EmergencyContact updated = emergencyContactRepository.save(entity);
        return mapper.toEmergencyContactDto(updated);
    }

    public void deleteEmergencyContact(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        EmergencyContact entity = emergencyContactRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("emergency.contact.not.found")));
        emergencyContactRepository.delete(entity);
    }

    // ── Dependents ────────────────────────────────────────────────────────────

    public DependentDto.Response createDependent(Long employeeId, @Valid DependentDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Employee employee = findEmployee(employeeId, tenantId);

        Dependent entity = Dependent.builder()
                .tenantId(tenantId)
                .employee(employee)
                .nombreCompleto(request.nombreCompleto())
                .relacion(request.relacion())
                .fechaNacimiento(request.fechaNacimiento())
                .documentoIdentidad(request.documentoIdentidad())
                .genero(request.genero())
                .esBeneficiarioSeguro(request.esBeneficiarioSeguro() != null ? request.esBeneficiarioSeguro() : false)
                .esCargaFamiliar(request.esCargaFamiliar() != null ? request.esCargaFamiliar() : true)
                .build();

        Dependent saved = dependentRepository.save(entity);
        log.info("Dependiente creado: {} para empleado {} - Tenant: {}", saved.getId(), employeeId, tenantId);
        return mapper.toDependentDto(saved);
    }

    public DependentDto.Response updateDependent(Long id, @Valid DependentDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Dependent entity = dependentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("dependent.not.found")));

        entity.setNombreCompleto(request.nombreCompleto());
        entity.setRelacion(request.relacion());
        entity.setFechaNacimiento(request.fechaNacimiento());
        entity.setDocumentoIdentidad(request.documentoIdentidad());
        entity.setGenero(request.genero());
        if (request.esBeneficiarioSeguro() != null) entity.setEsBeneficiarioSeguro(request.esBeneficiarioSeguro());
        if (request.esCargaFamiliar() != null) entity.setEsCargaFamiliar(request.esCargaFamiliar());

        Dependent updated = dependentRepository.save(entity);
        return mapper.toDependentDto(updated);
    }

    public void deleteDependent(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Dependent entity = dependentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("dependent.not.found")));
        dependentRepository.delete(entity);
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    public DocumentDto.Response createDocument(Long employeeId, @Valid DocumentDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Employee employee = findEmployee(employeeId, tenantId);

        Document entity = Document.builder()
                .tenantId(tenantId)
                .employee(employee)
                .tipoDocumento(request.tipoDocumento())
                .nombreArchivo(request.nombreArchivo())
                .descripcion(request.descripcion())
                .urlArchivo(request.urlArchivo())
                .fechaEmision(request.fechaEmision())
                .fechaVencimiento(request.fechaVencimiento())
                .build();

        Document saved = documentRepository.save(entity);
        log.info("Documento creado: {} para empleado {} - Tenant: {}", saved.getId(), employeeId, tenantId);
        return mapper.toDocumentDto(saved);
    }

    public DocumentDto.Response updateDocument(Long id, @Valid DocumentDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Document entity = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("document.not.found")));

        entity.setTipoDocumento(request.tipoDocumento());
        entity.setNombreArchivo(request.nombreArchivo());
        entity.setDescripcion(request.descripcion());
        entity.setUrlArchivo(request.urlArchivo());
        entity.setFechaEmision(request.fechaEmision());
        entity.setFechaVencimiento(request.fechaVencimiento());

        Document updated = documentRepository.save(entity);
        return mapper.toDocumentDto(updated);
    }

    public void deleteDocument(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Document entity = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("document.not.found")));
        documentRepository.delete(entity);
    }

    // ── Salary History ────────────────────────────────────────────────────────

    public SalaryDto.Response createSalaryRecord(Long employeeId, @Valid SalaryDto.Request request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Employee employee = findEmployee(employeeId, tenantId);

        // Close previous current salary
        salaryRepository.findCurrentSalary(tenantId, employeeId)
                .ifPresent(current -> {
                    current.setFechaFin(request.fechaInicio().minusDays(1));
                    salaryRepository.save(current);
                });

        Salary entity = Salary.builder()
                .tenantId(tenantId)
                .employee(employee)
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .salarioBase(request.salarioBase())
                .moneda(request.moneda() != null ? request.moneda() : "PEN")
                .motivo(request.motivo())
                .porcentajeIncremento(request.porcentajeIncremento())
                .build();

        Salary saved = salaryRepository.save(entity);
        log.info("Registro salarial creado: {} para empleado {} - Tenant: {}", saved.getId(), employeeId, tenantId);
        return mapper.toSalaryDto(saved);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Employee findEmployee(Long employeeId, Long tenantId) {
        return employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("employee.not.found")));
    }
}
