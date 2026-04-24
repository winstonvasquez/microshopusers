package com.microshop.rrhh.shared.constants;

/**
 * Estados del ciclo de vida de una Orden de Compra.
 * BORRADOR → PENDIENTE → APROBADA → RECIBIDA
 * ↘ CANCELADA (desde BORRADOR o PENDIENTE)
 */
public final class EstadoOrdenCompra {

    private EstadoOrdenCompra() {
    }

    public static final String BORRADOR = "BORRADOR";
    public static final String PENDIENTE = "PENDIENTE";
    public static final String APROBADA = "APROBADA";
    public static final String ENVIADA = "ENVIADA";
    public static final String RECIBIDA = "RECIBIDA";
    public static final String CANCELADA = "CANCELADA";
}
