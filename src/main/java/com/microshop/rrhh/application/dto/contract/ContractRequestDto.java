package com.microshop.rrhh.application.dto.contract;

import com.microshop.rrhh.domain.model.Contract;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ContractRequestDto(
    @NotNull(message = "{contract.employee.not.found}")
    Long employeeId,

    @NotNull(message = "{contract.tipo.required}")
    Contract.ContractType tipoContrato,

    @NotNull(message = "{contract.fechaInicio.required}")
    LocalDate fechaInicio,

    LocalDate fechaFin,

    @NotNull(message = "{contract.salarioBase.required}")
    @Positive(message = "{contract.salarioBase.min}")
    BigDecimal salarioBase,

    String moneda,

    Contract.WorkingDay jornadaLaboral,

    Integer horasSemanales,

    Integer periodoPruebaMeses,

    @Size(max = 500)
    String documentoContratoUrl
) {}
