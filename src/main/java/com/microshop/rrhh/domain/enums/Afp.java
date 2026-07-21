package com.microshop.rrhh.domain.enums;

import java.math.BigDecimal;

/**
 * AFP (Administradora de Fondo de Pensiones) del sistema privado de pensiones
 * peruano y sus tasas vigentes desglosadas por componente.
 *
 * <p><b>Fuente única (Fase 1 — "cimientos", audit-hardening-2026-05-28):</b>
 * hoy existen DOS copias divergentes de estas cifras:</p>
 * <ul>
 *   <li>{@code PayrollCommandService.AFP_RATE_DEFAULTS} — tasa total plana por AFP
 *       (fallback si no hay parámetro ERP {@code AFP_RATE_<NOMBRE>}).</li>
 *   <li>{@code ConfiguracionController.getConfiguracionRemunerativa()} — tasas
 *       desglosadas en {@code ConfiguracionRemunerativaDto.AfpTasas(jubilacion,
 *       seguroInvalidez, comision)}.</li>
 * </ul>
 * <p>Este enum usa las cifras DESGLOSADAS de {@code ConfiguracionController}
 * (más precisas), tal como indica el criterio de la Fase 1. Divergencia
 * detectada al sumar los 3 componentes vs. la tasa plana de
 * {@code PayrollCommandService} (diferencias de ~0.1 a ~0.9 puntos porcentuales
 * por AFP): INTEGRA 0.12622 vs 0.1214, PRIMA 0.12911 vs 0.1218,
 * PROFUTURO 0.13429 vs 0.1254, HABITAT 0.12615 vs 0.1270.</p>
 *
 * <p>Mapeo de campos respecto a {@code ConfiguracionRemunerativaDto.AfpTasas}:
 * {@link #comision} = {@code comision} (comisión de administración de la AFP);
 * {@link #seguro} = {@code seguroInvalidez} (prima de seguro de invalidez,
 * sobrevivencia y gastos de sepelio); {@link #prima} = {@code jubilacion}
 * (10% flat, aporte obligatorio a la Cuenta Individual de Capitalización —
 * se nombra "prima" aquí por completar la tripleta pedida en el contrato de
 * Fase 1; NO confundir con "prima de seguro", que es {@link #seguro}).</p>
 *
 * <p>Esta clase todavía NO reemplaza a las dos copias existentes (Fase 1 es
 * puramente aditiva); la migración de consumidores ocurre en F2.</p>
 */
public enum Afp {

    INTEGRA(new BigDecimal("0.00874"), new BigDecimal("0.10"), new BigDecimal("0.01748")),
    PRIMA(new BigDecimal("0.01069"), new BigDecimal("0.10"), new BigDecimal("0.01842")),
    PROFUTURO(new BigDecimal("0.01587"), new BigDecimal("0.10"), new BigDecimal("0.01842")),
    HABITAT(new BigDecimal("0.00773"), new BigDecimal("0.10"), new BigDecimal("0.01842"));

    private final BigDecimal comision;
    private final BigDecimal prima;
    private final BigDecimal seguro;

    Afp(BigDecimal comision, BigDecimal prima, BigDecimal seguro) {
        this.comision = comision;
        this.prima = prima;
        this.seguro = seguro;
    }

    public BigDecimal comision() {
        return comision;
    }

    public BigDecimal prima() {
        return prima;
    }

    public BigDecimal seguro() {
        return seguro;
    }

    /** Tasa total de descuento sobre la base computable: comisión + prima (aporte) + seguro. */
    public BigDecimal tasaTotal() {
        return comision.add(prima).add(seguro);
    }
}
