package com.microshop.rrhh.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollHorasExtraTest {

    @Test
    @DisplayName("Un día con 4 h extra paga 2×1.25 + 2×1.35 sobre el valor hora")
    void cuatroHorasEnUnDia() {
        BigDecimal monto = PayrollCommandService.calcularMontoHorasExtras(
                List.of(new BigDecimal("4")), new BigDecimal("10"));
        assertThat(monto).isEqualByComparingTo("52.00"); // (2.5 + 2.7) × 10
    }

    @Test
    @DisplayName("El tope de 2 h al 25% es por registro, no por mes")
    void topeEsDiario() {
        BigDecimal monto = PayrollCommandService.calcularMontoHorasExtras(
                List.of(new BigDecimal("2"), new BigDecimal("1.5"), new BigDecimal("0")), new BigDecimal("10"));
        assertThat(monto).isEqualByComparingTo("43.75"); // (2 + 1.5) × 1.25 × 10: ningún día pasa de 2 h → todo al tramo 1
    }
}
