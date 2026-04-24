package com.microshop.rrhh.application.dto.employee;

import com.microshop.rrhh.domain.model.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DocumentDto {

    private DocumentDto() {}

    @Builder
    public record Request(
        @NotNull
        Document.DocumentType tipoDocumento,

        @NotBlank @Size(max = 200)
        String nombreArchivo,

        @Size(max = 500)
        String descripcion,

        @NotBlank @Size(max = 500)
        String urlArchivo,

        LocalDate fechaEmision,

        LocalDate fechaVencimiento
    ) {}

    @Builder
    public record Response(
        Long id,
        Long employeeId,
        Document.DocumentType tipoDocumento,
        String nombreArchivo,
        String descripcion,
        String urlArchivo,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        Document.DocumentStatus estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
