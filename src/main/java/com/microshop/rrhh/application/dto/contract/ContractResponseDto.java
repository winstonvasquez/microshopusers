package com.microshop.rrhh.application.dto.contract;

import com.microshop.rrhh.domain.model.Contract;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ContractResponseDto(
    Long id,
    Long tenantId,
    Long employeeId,
    String employeeName,
    Contract.ContractType tipoContrato,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    BigDecimal salarioBase,
    String moneda,
    Contract.WorkingDay jornadaLaboral,
    Integer horasSemanales,
    Integer periodoPruebaMeses,
    String documentoContratoUrl,
    Contract.ContractStatus estado,
    String motivoFin,
    boolean expiringSoon,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
