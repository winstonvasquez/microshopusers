package com.microshop.rrhh.application.dto.configuracion;

import java.util.List;

/**
 * Configuración de parámetros remunerativos vigentes.
 * UIT, tasas AFP, ONP, ESSALUD y tramos de Renta de 5ta Categoría.
 */
public record ConfiguracionRemunerativaDto(
    int anio,
    double uit,
    double essaludTasa,
    double onpTasa,
    double asignacionFamiliar,
    List<TramoRenta5ta> tramosRenta5ta,
    AfpTasas integra,
    AfpTasas prima,
    AfpTasas profuturo,
    AfpTasas habitat
) {
    public record TramoRenta5ta(double desdeUit, double hastaUit, double tasa) {}
    public record AfpTasas(double jubilacion, double seguroInvalidez, double comision) {}
}
