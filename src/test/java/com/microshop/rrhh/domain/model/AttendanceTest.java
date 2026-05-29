package com.microshop.rrhh.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del cálculo de horas de planilla (fix 2026-05-28: aritmética exacta minutos/60 con
 * BigDecimal en vez de `double`, que introducía error IEEE-754 propagado a horasExtras × tarifa).
 */
class AttendanceTest {

    private void invokeCalculate(Attendance a) throws Exception {
        Method m = Attendance.class.getDeclaredMethod("calculateHours");
        m.setAccessible(true);
        m.invoke(a);
    }

    @Test
    @DisplayName("9h exactas → 9.0000 trabajadas, 1.0000 extra")
    void nineHours() throws Exception {
        Attendance a = Attendance.builder()
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(17, 0))
                .build();
        invokeCalculate(a);
        assertThat(a.getHorasTrabajadas()).isEqualByComparingTo("9.0000");
        assertThat(a.getHorasExtras()).isEqualByComparingTo("1.0000");
    }

    @Test
    @DisplayName("479 minutos → 7.9833 exacto (sin error de double), sin horas extra")
    void precisionNoFloatingError() throws Exception {
        Attendance a = Attendance.builder()
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(15, 59))
                .build();
        invokeCalculate(a);
        // 479/60 = 7.98333... → HALF_UP a 4 decimales = 7.9833
        assertThat(a.getHorasTrabajadas()).isEqualByComparingTo("7.9833");
        assertThat(a.getHorasExtras()).isEqualByComparingTo("0"); // < 8h, sin extra
    }

    @Test
    @DisplayName("8h exactas → sin horas extra")
    void eightHoursNoExtra() throws Exception {
        Attendance a = Attendance.builder()
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(16, 0))
                .build();
        invokeCalculate(a);
        assertThat(a.getHorasTrabajadas()).isEqualByComparingTo("8.0000");
        assertThat(a.getHorasExtras()).isEqualByComparingTo("0");
    }
}
