package com.microshop.rrhh.application.dto.employee;

import com.microshop.rrhh.domain.model.EmergencyContact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

public final class EmergencyContactDto {

    private EmergencyContactDto() {}

    @Builder
    public record Request(
        @NotBlank @Size(max = 200)
        String nombreCompleto,

        @NotNull
        EmergencyContact.Relationship relacion,

        @NotBlank @Size(max = 20)
        String telefono,

        @Size(max = 20)
        String telefonoAlternativo,

        @Size(max = 500)
        String direccion,

        Boolean esPrincipal
    ) {}

    @Builder
    public record Response(
        Long id,
        Long employeeId,
        String nombreCompleto,
        EmergencyContact.Relationship relacion,
        String telefono,
        String telefonoAlternativo,
        String direccion,
        Boolean esPrincipal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
