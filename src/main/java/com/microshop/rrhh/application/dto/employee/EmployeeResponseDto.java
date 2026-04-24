package com.microshop.rrhh.application.dto.employee;
import lombok.Builder;

import com.microshop.rrhh.domain.model.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record EmployeeResponseDto(
    Long id,
    Long tenantId,
    String codigoEmpleado,
    String nombres,
    String apellidos,
    String tipoDocumento,
    String documentoIdentidad,
    LocalDate fechaNacimiento,
    Employee.Gender genero,
    Employee.MaritalStatus estadoCivil,
    String nacionalidad,
    String tipoSangre,
    LocalDate fechaIngreso,
    LocalDate fechaSalida,
    String motivoSalida,
    Long departmentId,
    String departmentName,
    Long positionId,
    String positionName,
    Long supervisorId,
    String supervisorName,
    String cargo,
    String area,
    String email,
    String telefono,
    String direccion,
    String distrito,
    String provincia,
    String departamentoGeo,
    String fotoUrl,
    String linkedinUrl,
    String nivelEducacion,
    String profesion,
    String universidad,
    String sistemaPrevisional,
    String afpNombre,
    Long storeId,
    Employee.EmployeeStatus estado,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
