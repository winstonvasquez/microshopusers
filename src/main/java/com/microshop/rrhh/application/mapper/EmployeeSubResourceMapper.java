package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.employee.DependentDto;
import com.microshop.rrhh.application.dto.employee.DocumentDto;
import com.microshop.rrhh.application.dto.employee.EmergencyContactDto;
import com.microshop.rrhh.application.dto.employee.SalaryDto;
import com.microshop.rrhh.domain.model.Dependent;
import com.microshop.rrhh.domain.model.Document;
import com.microshop.rrhh.domain.model.EmergencyContact;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Salary;
import com.microshop.users.shared.util.AppUtils;
import org.springframework.stereotype.Component;

/**
 * Mapeo de los sub-recursos del empleado. Extraído de
 * {@code EmployeeSubResourceCommandService} y {@code EmployeeSubResourceQueryService}, que
 * tenían los cuatro métodos duplicados byte a byte. Sin cambios de comportamiento.
 */
@Component
public class EmployeeSubResourceMapper {

    public EmergencyContactDto.Response toEmergencyContactDto(EmergencyContact e) {
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

    public DependentDto.Response toDependentDto(Dependent d) {
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

    public DocumentDto.Response toDocumentDto(Document doc) {
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

    public SalaryDto.Response toSalaryDto(Salary s) {
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
                .aprobadoPorId(AppUtils.idOrNull(approver, e -> e.getId()))
                .aprobadoPorName(approver != null ? AppUtils.fullName(approver.getNombres(), approver.getApellidos()) : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
