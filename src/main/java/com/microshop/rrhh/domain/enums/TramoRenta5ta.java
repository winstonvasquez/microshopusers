package com.microshop.rrhh.domain.enums;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tramos del Impuesto a la Renta de 5ta Categoría (art. 53 LIR), expresados
 * en UIT (Unidad Impositiva Tributaria) sobre el exceso deducible.
 *
 * <p><b>Fuente única (Fase 1 — "cimientos", audit-hardening-2026-05-28):</b>
 * hoy estos tramos están hardcodeados en dos lugares que SÍ coinciden entre sí:
 * {@code PayrollCommandService.calcularRenta5ta()} (arrays {@code limits}/{@code rates})
 * y {@code ConfiguracionController.getConfiguracionRemunerativa()}
 * (lista de {@code ConfiguracionRemunerativaDto.TramoRenta5ta}). No se
 * detectó divergencia de cifras entre ambas copias.</p>
 *
 * <p>El último tramo usa {@link Integer#MAX_VALUE} como centinela de
 * "sin límite superior" (en el código original se usa {@code Double.MAX_VALUE}
 * o simplemente no se evalúa límite superior en el loop).</p>
 *
 * <p>Esta clase todavía NO reemplaza a las dos copias existentes (Fase 1 es
 * puramente aditiva); la migración de consumidores ocurre en F2.</p>
 */
public enum TramoRenta5ta {

    TRAMO_1(0, 5, new BigDecimal("0.08")),
    TRAMO_2(5, 20, new BigDecimal("0.14")),
    TRAMO_3(20, 35, new BigDecimal("0.17")),
    TRAMO_4(35, 45, new BigDecimal("0.20")),
    TRAMO_5(45, Integer.MAX_VALUE, new BigDecimal("0.30"));

    /** Lista inmutable de los tramos en orden ascendente, lista para iterar. */
    private static final List<TramoRenta5ta> ORDENADOS = List.of(values());

    private final int desdeUit;
    private final int hastaUit;
    private final BigDecimal tasa;

    TramoRenta5ta(int desdeUit, int hastaUit, BigDecimal tasa) {
        this.desdeUit = desdeUit;
        this.hastaUit = hastaUit;
        this.tasa = tasa;
    }

    public int desdeUit() {
        return desdeUit;
    }

    public int hastaUit() {
        return hastaUit;
    }

    public BigDecimal tasa() {
        return tasa;
    }

    /** Tramos en orden ascendente (0-5, 5-20, 20-35, 35-45, 45+ UIT), listos para iterar. */
    public static List<TramoRenta5ta> ordenados() {
        return ORDENADOS;
    }
}
