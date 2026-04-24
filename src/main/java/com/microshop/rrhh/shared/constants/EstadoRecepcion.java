package com.microshop.rrhh.shared.constants;

/**
 * Estados del ciclo de vida de una Recepción de mercancía.
 */
public final class EstadoRecepcion {

    private EstadoRecepcion() {
    }

    public static final String PENDIENTE = "PENDIENTE";
    public static final String CONFORME = "CONFORME";
    public static final String CON_DIFERENCIAS = "CON_DIFERENCIAS";
}
