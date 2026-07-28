package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.employee.EmployeeRequestDto;
import com.microshop.rrhh.application.dto.employee.EmployeeResponseDto;
import com.microshop.rrhh.domain.model.Department;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Position;
import com.microshop.rrhh.shared.constants.ApiPaths;
import com.microshop.users.shared.util.AppUtils;
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
                .fotoUrl(esUrlFotoBinaria(dto.fotoUrl()) ? null : dto.fotoUrl())
                .linkedinUrl(dto.linkedinUrl())
                .nivelEducacion(dto.nivelEducacion())
                .profesion(dto.profesion())
                .universidad(dto.universidad())
                .sistemaPrevisional(dto.sistemaPrevisional() != null ? dto.sistemaPrevisional() : "ONP")
                .afpNombre(dto.afpNombre())
                .storeId(dto.storeId())
                .userId(dto.userId())
                .estado(dto.estado() != null ? dto.estado() : Employee.EmployeeStatus.ACTIVO)
                .build();
        return entity;
    }

    /**
     * Update con semántica PATCH: solo sobrescribe los campos presentes (no null) en el DTO.
     * Defensa contra pérdida de datos cuando un formulario parcial no envía todos los campos
     * (un campo ausente llega como null y NO debe borrar el valor guardado en BD).
     */
    public void updateEntity(Employee entity, EmployeeRequestDto dto) {
        if (dto.codigoEmpleado() != null) entity.setCodigoEmpleado(dto.codigoEmpleado());
        if (dto.nombres() != null) entity.setNombres(dto.nombres());
        if (dto.apellidos() != null) entity.setApellidos(dto.apellidos());
        if (dto.tipoDocumento() != null) entity.setTipoDocumento(dto.tipoDocumento());
        if (dto.documentoIdentidad() != null) entity.setDocumentoIdentidad(dto.documentoIdentidad());
        if (dto.fechaNacimiento() != null) entity.setFechaNacimiento(dto.fechaNacimiento());
        if (dto.genero() != null) entity.setGenero(dto.genero());
        if (dto.estadoCivil() != null) entity.setEstadoCivil(dto.estadoCivil());
        if (dto.nacionalidad() != null) entity.setNacionalidad(dto.nacionalidad());
        if (dto.tipoSangre() != null) entity.setTipoSangre(dto.tipoSangre());
        if (dto.fechaIngreso() != null) entity.setFechaIngreso(dto.fechaIngreso());
        if (dto.fechaSalida() != null) entity.setFechaSalida(dto.fechaSalida());
        if (dto.motivoSalida() != null) entity.setMotivoSalida(dto.motivoSalida());
        if (dto.cargo() != null) entity.setCargo(dto.cargo());
        if (dto.area() != null) entity.setArea(dto.area());
        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.telefono() != null) entity.setTelefono(dto.telefono());
        if (dto.direccion() != null) entity.setDireccion(dto.direccion());
        if (dto.distrito() != null) entity.setDistrito(dto.distrito());
        if (dto.provincia() != null) entity.setProvincia(dto.provincia());
        if (dto.departamentoGeo() != null) entity.setDepartamentoGeo(dto.departamentoGeo());
        // foto_url es SOLO para fotos externas. Si el formulario reenvía la URL calculada
        // del binario (round-trip de /hr/api/employees/{id}/foto), se conserva el valor actual.
        if (dto.fotoUrl() != null && !esUrlFotoBinaria(dto.fotoUrl())) entity.setFotoUrl(dto.fotoUrl());
        if (dto.linkedinUrl() != null) entity.setLinkedinUrl(dto.linkedinUrl());
        if (dto.nivelEducacion() != null) entity.setNivelEducacion(dto.nivelEducacion());
        if (dto.profesion() != null) entity.setProfesion(dto.profesion());
        if (dto.universidad() != null) entity.setUniversidad(dto.universidad());
        if (dto.sistemaPrevisional() != null) entity.setSistemaPrevisional(dto.sistemaPrevisional());
        if (dto.afpNombre() != null) entity.setAfpNombre(dto.afpNombre());
        if (dto.storeId() != null) entity.setStoreId(dto.storeId());
        if (dto.userId() != null) entity.setUserId(dto.userId());
        if (dto.estado() != null) entity.setEstado(dto.estado());
    }

    /**
     * URL de la foto expuesta al frontend: apunta al endpoint binario cuando la foto
     * está en BD; si no, cae a la foto_url externa guardada (fallback).
     */
    /** {@code true} si la URL es la ruta calculada de la foto binaria de este servicio. */
    private static boolean esUrlFotoBinaria(String url) {
        return url != null && url.startsWith(ApiPaths.EMPLOYEES + "/") && url.endsWith("/foto");
    }

    public static String resolveFotoUrl(Employee entity) {
        return (entity.getFotoData() != null)
                ? ApiPaths.EMPLOYEES + "/" + entity.getId() + "/foto"
                : entity.getFotoUrl();
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
                .departmentId(AppUtils.idOrNull(dept, d -> d.getId()))
                .departmentName(dept != null ? dept.getNombre() : null)
                .positionId(AppUtils.idOrNull(pos, p -> p.getId()))
                .positionName(pos != null ? pos.getNombre() : null)
                .supervisorId(AppUtils.idOrNull(sup, e -> e.getId()))
                .supervisorName(sup != null ? AppUtils.fullName(sup.getNombres(), sup.getApellidos()) : null)
                .cargo(entity.getCargo())
                .area(entity.getArea())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .distrito(entity.getDistrito())
                .provincia(entity.getProvincia())
                .departamentoGeo(entity.getDepartamentoGeo())
                .fotoUrl(resolveFotoUrl(entity))
                .linkedinUrl(entity.getLinkedinUrl())
                .nivelEducacion(entity.getNivelEducacion())
                .profesion(entity.getProfesion())
                .universidad(entity.getUniversidad())
                .sistemaPrevisional(entity.getSistemaPrevisional())
                .afpNombre(entity.getAfpNombre())
                .storeId(entity.getStoreId())
                .userId(entity.getUserId())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
