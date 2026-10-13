package com.microshop.rrhh.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollTest {

    @Test
    @DisplayName("Planilla de julio: la gratificación entra al neto (inafecta a AFP/ONP)")
    void netoIncluyeGratificacion() {
        Payroll p = Payroll.builder()
                .sueldoBase(new BigDecimal("2000.00"))
                .gratificacion(new BigDecimal("2000.00"))
                .montoAfpOnp(new BigDecimal("260.00")) // 13% sobre el sueldo base, sin gratificación
                .build();

        p.calculateNeto();

        assertThat(p.getNeto()).isEqualByComparingTo("3740.00");
    }
}
