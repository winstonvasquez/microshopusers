package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.attendance.AttendanceRequestDto;
import com.microshop.rrhh.application.dto.attendance.AttendanceResponseDto;
import com.microshop.rrhh.domain.model.Attendance;
import com.microshop.rrhh.domain.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public Attendance toEntity(AttendanceRequestDto dto, Long tenantId, Employee employee) {
        return Attendance.builder()
                .tenantId(tenantId)
                .employee(employee)
                .fecha(dto.fecha())
                .horaEntrada(dto.horaEntrada())
                .horaSalida(dto.horaSalida())
                .tipoRegistro(dto.tipoRegistro())
                .observaciones(dto.observaciones())
                .justificacion(dto.justificacion())
                .ubicacionEntrada(dto.ubicacionEntrada())
                .ubicacionSalida(dto.ubicacionSalida())
                .build();
    }

    public AttendanceResponseDto toDto(Attendance entity) {
        Employee emp = entity.getEmployee();
        return AttendanceResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeName(emp != null ? emp.getNombres() + " " + emp.getApellidos() : null)
                .fecha(entity.getFecha())
                .horaEntrada(entity.getHoraEntrada())
                .horaSalida(entity.getHoraSalida())
                .horasTrabajadas(entity.getHorasTrabajadas())
                .horasExtras(entity.getHorasExtras())
                .tipoRegistro(entity.getTipoRegistro())
                .observaciones(entity.getObservaciones())
                .justificacion(entity.getJustificacion())
                .ubicacionEntrada(entity.getUbicacionEntrada())
                .ubicacionSalida(entity.getUbicacionSalida())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
