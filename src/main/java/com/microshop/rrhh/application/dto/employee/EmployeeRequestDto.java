package com.microshop.rrhh.application.dto.employee;
import lombok.Builder;

import com.microshop.rrhh.domain.model.Employee;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Builder
public record EmployeeRequestDto(
    @NotBlank(message = "{employee.codigoEmpleado.required}")
    @Size(max = 20, message = "{employee.codigoEmpleado.size}")
    String codigoEmpleado,

    @NotBlank(message = "{employee.nombres.required}")
    @Size(max = 100, message = "{employee.nombres.size}")
    String nombres,

    @NotBlank(message = "{employee.apellidos.required}")
    @Size(max = 100, message = "{employee.apellidos.size}")
    String apellidos,

    @Size(max = 20)
    String tipoDocumento,

    @NotBlank(message = "{employee.documentoIdentidad.required}")
    @Size(max = 20, message = "{employee.documentoIdentidad.size}")
    String documentoIdentidad,

    LocalDate fechaNacimiento,

    Employee.Gender genero,

    Employee.MaritalStatus estadoCivil,

    @Size(max = 50)
    String nacionalidad,

    @Size(max = 5)
    String tipoSangre,

    @NotNull(message = "{employee.fechaIngreso.required}")
    @PastOrPresent(message = "{employee.fechaIngreso.pastOrPresent}")
    LocalDate fechaIngreso,

    LocalDate fechaSalida,

    @Size(max = 500)
    String motivoSalida,

    Long departmentId,

    Long positionId,

    Long supervisorId,

    @Size(max = 100, message = "{employee.cargo.size}")
    String cargo,

    @Size(max = 100, message = "{employee.area.size}")
    String area,

    @Email(message = "{employee.email.valid}")
    @Size(max = 100, message = "{employee.email.size}")
    String email,

    @Size(max = 20, message = "{employee.telefono.size}")
    String telefono,

    @Size(max = 500)
    String direccion,

    @Size(max = 100)
    String distrito,

    @Size(max = 100)
    String provincia,

    @Size(max = 100)
    String departamentoGeo,

    @Size(max = 500)
    String fotoUrl,

    @Size(max = 500)
    String linkedinUrl,

    @Size(max = 50)
    String nivelEducacion,

    @Size(max = 100)
    String profesion,

    @Size(max = 200)
    String universidad,

    @Size(max = 10)
    String sistemaPrevisional,

    @Size(max = 20)
    String afpNombre,

    Long storeId,

    Employee.EmployeeStatus estado
) {}
