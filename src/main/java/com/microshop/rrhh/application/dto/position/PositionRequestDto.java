package com.microshop.rrhh.application.dto.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PositionRequestDto(
    @NotBlank(message = "{position.codigo.required}")
    @Size(max = 20, message = "{position.codigo.size}")
    String codigo,

    @NotBlank(message = "{position.nombre.required}")
    @Size(max = 100, message = "{position.nombre.size}")
    String nombre,

    @Size(max = 1000, message = "{position.descripcion.size}")
    String descripcion,

    @NotNull(message = "{position.department.required}")
    Long departmentId,

    String nivel,

    @Positive(message = "{position.salario.min.invalid}")
    BigDecimal salarioMinimo,

    @Positive(message = "{position.salario.min.invalid}")
    BigDecimal salarioMaximo,

    String requisitos
) {}
