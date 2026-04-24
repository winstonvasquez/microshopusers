package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.employee.EmployeeRequestDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Position;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequestDto dto, Long tenantId) {
        Employee entity = Employee.builder()
                .tenantId(tenantId)
                .codigoEmpleado(dto.codigoEmpleado())
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .tipoDocumento(dto.tipoDocumento() != null ? dto.tipoDocumento() : "DNI")
                .documentoIdentidad(dto.documentoIdentidad())
                .fechaNacimiento(dto.fechaNacimiento())
                .genero(dto.genero())
                .estadoCivil(dto.estadoCivil())
                .nacionalidad(dto.nacionalidad())
                .tipoSangre(dto.tipoSangre())
                .fechaIngreso(dto.fechaIngreso())
                .fechaSalida(dto.fechaSalida())
                .motivoSalida(dto.motivoSalida())
                .cargo(dto.cargo())
                .area(dto.area())
                .email(dto.email())
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .distrito(dto.distrito())
                .provincia(dto.provincia())
                .departamentoGeo(dto.departamentoGeo())
                .fotoUrl(dto.fotoUrl())
                .linkedinUrl(dto.linkedinUrl())
                .nivelEducacion(dto.nivelEducacion())
                .profesion(dto.profesion())
                .universidad(dto.universidad())
                .sistemaPrevisional(dto.sistemaPrevisional() != null ? dto.sistemaPrevisional() : "ONP")
                .afpNombre(dto.afpNombre())
                .storeId(dto.storeId())
                .estado(dto.estado() != null ? dto.estado() : Employee.EmployeeStatus.ACTIVO)
                .build();
        return entity;
    }

    public void updateEntity(Employee entity, EmployeeRequestDto dto) {
        entity.setCodigoEmpleado(dto.codigoEmpleado());
        entity.setNombres(dto.nombres());
        entity.setApellidos(dto.apellidos());
        if (dto.tipoDocumento() != null) entity.setTipoDocumento(dto.tipoDocumento());
        entity.setDocumentoIdentidad(dto.documentoIdentidad());
        entity.setFechaNacimiento(dto.fechaNacimiento());
        entity.setGenero(dto.genero());
        entity.setEstadoCivil(dto.estadoCivil());
        entity.setNacionalidad(dto.nacionalidad());
        entity.setTipoSangre(dto.tipoSangre());
        entity.setFechaIngreso(dto.fechaIngreso());
        entity.setFechaSalida(dto.fechaSalida());
        entity.setMotivoSalida(dto.motivoSalida());
        entity.setCargo(dto.cargo());
        entity.setArea(dto.area());
        entity.setEmail(dto.email());
        entity.setTelefono(dto.telefono());
        entity.setDireccion(dto.direccion());
        entity.setDistrito(dto.distrito());
        entity.setProvincia(dto.provincia());
        entity.setDepartamentoGeo(dto.departamentoGeo());
        entity.setFotoUrl(dto.fotoUrl());
        entity.setLinkedinUrl(dto.linkedinUrl());
        entity.setNivelEducacion(dto.nivelEducacion());
        entity.setProfesion(dto.profesion());
        entity.setUniversidad(dto.universidad());
        if (dto.sistemaPrevisional() != null) entity.setSistemaPrevisional(dto.sistemaPrevisional());
        entity.setAfpNombre(dto.afpNombre());
        entity.setStoreId(dto.storeId());
        if (dto.estado() != null) {
            entity.setEstado(dto.estado());
        }
    }

    public EmployeeResponseDto toDto(Employee entity) {
        Department dept = entity.getDepartment();
        Position pos = entity.getPosition();
        Employee sup = entity.getSupervisor();

        return EmployeeResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .codigoEmpleado(entity.getCodigoEmpleado())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .tipoDocumento(entity.getTipoDocumento())
                .documentoIdentidad(entity.getDocumentoIdentidad())
                .fechaNacimiento(entity.getFechaNacimiento())
                .genero(entity.getGenero())
                .estadoCivil(entity.getEstadoCivil())
                .nacionalidad(entity.getNacionalidad())
                .tipoSangre(entity.getTipoSangre())
                .fechaIngreso(entity.getFechaIngreso())
                .fechaSalida(entity.getFechaSalida())
                .motivoSalida(entity.getMotivoSalida())
                .departmentId(dept != null ? dept.getId() : null)
                .departmentName(dept != null ? dept.getNombre() : null)
                .positionId(pos != null ? pos.getId() : null)
                .positionName(pos != null ? pos.getNombre() : null)
                .supervisorId(sup != null ? sup.getId() : null)
                .supervisorName(sup != null ? sup.getNombres() + " " + sup.getApellidos() : null)
                .cargo(entity.getCargo())
                .area(entity.getArea())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .distrito(entity.getDistrito())
                .provincia(entity.getProvincia())
                .departamentoGeo(entity.getDepartamentoGeo())
                .fotoUrl(entity.getFotoUrl())
                .linkedinUrl(entity.getLinkedinUrl())
                .nivelEducacion(entity.getNivelEducacion())
                .profesion(entity.getProfesion())
                .universidad(entity.getUniversidad())
                .sistemaPrevisional(entity.getSistemaPrevisional())
                .afpNombre(entity.getAfpNombre())
                .storeId(entity.getStoreId())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
