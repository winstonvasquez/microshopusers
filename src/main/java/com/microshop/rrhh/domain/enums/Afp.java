package com.microshop.rrhh.domain.enums;

import java.math.BigDecimal;

/**
 * AFP (Administradora de Fondo de Pensiones) del sistema privado de pensiones
 * peruano y sus tasas vigentes desglosadas por componente.
 *
 * <p><b>FUENTE ÚNICA de las tasas AFP</b> (consolidación 2026-07-21,
 * audit-hardening-2026-05-28). Antes existían DOS copias divergentes y ambas
 * DESACTUALIZADAS: {@code PayrollCommandService.AFP_RATE_DEFAULTS} (planas) y
 * {@code ConfiguracionController} (desglosadas con seguro ~1.75-1.84%). Ambos
 * consumidores ahora derivan de este enum.</p>
 *
 * <p><b>Valores oficiales SBS/SPP — devengue MAYO 2026</b>
 * (fuentes: portal SBS sbs.gob.pe, comparadores AFP 2026):</p>
 * <ul>
 *   <li>Aporte obligatorio a la CIC: <b>10%</b> (igual para todas).</li>
 *   <li>Prima de seguro de invalidez/sobrevivencia: <b>1.37%</b> (igual para
 *       todas; fijada por licitación SBS cada ~2 años).</li>
 *   <li>Comisión POR FLUJO: Habitat 1.47%, Integra 1.55%, Prima 1.60%,
 *       Profuturo 1.69%.</li>
 * </ul>
 * <p>Total resultante: Habitat 12.84%, Integra 12.92%, Prima 12.97%,
 * Profuturo 13.06%.</p>
 *
 * <p><b>⚠️ VERIFICAR ANTES DE PRODUCCIÓN (contabilidad/RRHH):</b></p>
 * <ol>
 *   <li>Las tasas AFP cambian por resolución SBS (la prima cada ~2 años; las
 *       comisiones por licitación). Confirmar el período de devengue vigente.</li>
 *   <li>Esta consolidación asume <b>comisión por flujo</b> (no comisión sobre
 *       saldo / "mixta"). Si el afiliado está en el esquema mixto, la comisión
 *       difiere.</li>
 *   <li>La prima de seguro se aplica sobre la remuneración asegurable con
 *       <b>tope</b> (RMA). El cálculo de planilla actual aplica la tasa total
 *       sobre la base sin ese tope (simplificación PREEXISTENTE) → para sueldos
 *       altos sobreestima ligeramente el seguro. Fix del tope = tarea aparte.</li>
 *   <li>En producción, preferir los parámetros ERP {@code AFP_RATE_<NOMBRE>}
 *       (editables por empresa/período); estos valores son solo el fallback.</li>
 * </ol>
 *
 * <p>Mapeo de campos: {@link #comision} = comisión de administración (por flujo);
 * {@link #seguro} = prima de seguro de invalidez/sobrevivencia/sepelio;
 * {@link #prima} = aporte obligatorio 10% a la CIC (nombrado "prima" por la
 * tripleta del contrato; NO es la "prima de seguro", que es {@link #seguro}).</p>
 */
public enum Afp {

    // Comisión POR FLUJO + aporte obligatorio (10%) + prima de seguro (1.37%, igual para todas).
    // Valores oficiales SBS/SPP, devengue mayo 2026. Ver javadoc para fuente y advertencias.
    INTEGRA(new BigDecimal("0.0155"), new BigDecimal("0.10"), new BigDecimal("0.0137")),
    PRIMA(new BigDecimal("0.0160"), new BigDecimal("0.10"), new BigDecimal("0.0137")),
    PROFUTURO(new BigDecimal("0.0169"), new BigDecimal("0.10"), new BigDecimal("0.0137")),
    HABITAT(new BigDecimal("0.0147"), new BigDecimal("0.10"), new BigDecimal("0.0137"));

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
