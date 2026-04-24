package com.microshop.rrhh.application.dto.employee;

import com.microshop.rrhh.domain.model.Dependent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DependentDto {

    private DependentDto() {}

    @Builder
    public record Request(
        @NotBlank @Size(max = 200)
        String nombreCompleto,

        @NotNull
        Dependent.Relationship relacion,

        @NotNull
        LocalDate fechaNacimiento,

        @Size(max = 20)
        String documentoIdentidad,

        Dependent.Gender genero,

        Boolean esBeneficiarioSeguro,

        Boolean esCargaFamiliar
    ) {}

    @Builder
    public record Response(
        Long id,
        Long employeeId,
        String nombreCompleto,
        Dependent.Relationship relacion,
        LocalDate fechaNacimiento,
        String documentoIdentidad,
        Dependent.Gender genero,
        Boolean esBeneficiarioSeguro,
        Boolean esCargaFamiliar,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
