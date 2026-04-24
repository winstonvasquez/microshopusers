package com.microshop.rrhh.application.dto.payroll;

import com.microshop.rrhh.domain.model.PayrollDetail;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PayrollDetailDto(
    Long id,
    String concepto,
    PayrollDetail.ConceptType tipo,
    BigDecimal monto,
    BigDecimal cantidad,
    BigDecimal tasa
) {}
