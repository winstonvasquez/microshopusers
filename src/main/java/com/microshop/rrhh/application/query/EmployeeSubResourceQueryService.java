package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.employee.*;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmployeeSubResourceQueryService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final DependentRepository dependentRepository;
    private final DocumentRepository documentRepository;
    private final SalaryRepository salaryRepository;
    private final TenantContext tenantContext;

    // ── Emergency Contacts ────────────────────────────────────────────────────

    public List<EmergencyContactDto.Response> getEmergencyContacts(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return emergencyContactRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(this::toEmergencyContactDto)
                .toList();
    }

    // ── Dependents ────────────────────────────────────────────────────────────

    public List<DependentDto.Response> getDependents(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return dependentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(this::toDependentDto)
                .toList();
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    public List<DocumentDto.Response> getDocuments(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return documentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(this::toDocumentDto)
                .toList();
    }

    // ── Salary History ────────────────────────────────────────────────────────

    public List<SalaryDto.Response> getSalaryHistory(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return salaryRepository.findByTenantIdAndEmployeeIdOrderByFechaInicioDesc(tenantId, employeeId).stream()
                .map(this::toSalaryDto)
                .toList();
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private EmergencyContactDto.Response toEmergencyContactDto(EmergencyContact e) {
        return EmergencyContactDto.Response.builder()
                .id(e.getId())
                .employeeId(e.getEmployee().getId())
                .nombreCompleto(e.getNombreCompleto())
                .relacion(e.getRelacion())
                .telefono(e.getTelefono())
                .telefonoAlternativo(e.getTelefonoAlternativo())
                .direccion(e.getDireccion())
                .esPrincipal(e.getEsPrincipal())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private DependentDto.Response toDependentDto(Dependent d) {
        return DependentDto.Response.builder()
                .id(d.getId())
                .employeeId(d.getEmployee().getId())
                .nombreCompleto(d.getNombreCompleto())
                .relacion(d.getRelacion())
                .fechaNacimiento(d.getFechaNacimiento())
                .documentoIdentidad(d.getDocumentoIdentidad())
                .genero(d.getGenero())
                .esBeneficiarioSeguro(d.getEsBeneficiarioSeguro())
                .esCargaFamiliar(d.getEsCargaFamiliar())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DocumentDto.Response toDocumentDto(Document doc) {
        return DocumentDto.Response.builder()
                .id(doc.getId())
                .employeeId(doc.getEmployee().getId())
                .tipoDocumento(doc.getTipoDocumento())
                .nombreArchivo(doc.getNombreArchivo())
                .descripcion(doc.getDescripcion())
                .urlArchivo(doc.getUrlArchivo())
                .fechaEmision(doc.getFechaEmision())
                .fechaVencimiento(doc.getFechaVencimiento())
                .estado(doc.getEstado())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private SalaryDto.Response toSalaryDto(Salary s) {
        Employee approver = s.getAprobadoPor();
        return SalaryDto.Response.builder()
                .id(s.getId())
                .employeeId(s.getEmployee().getId())
                .fechaInicio(s.getFechaInicio())
                .fechaFin(s.getFechaFin())
                .salarioBase(s.getSalarioBase())
                .moneda(s.getMoneda())
                .motivo(s.getMotivo())
                .porcentajeIncremento(s.getPorcentajeIncremento())
                .aprobadoPorId(approver != null ? approver.getId() : null)
                .aprobadoPorName(approver != null ? approver.getNombres() + " " + approver.getApellidos() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
