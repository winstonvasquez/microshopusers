package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ubigeo oficial del INEI: un distrito peruano con su provincia y departamento.
 *
 * <p>Data maestra NACIONAL, sembrada por la migración V42 (1874 filas, inmutables). No lleva
 * {@code tenant_id} a propósito: la división política del país es la misma para las seis
 * empresas, así que no hay nada que acotar por tenant y no aplica {@code @RequiresTenantAccess}.
 *
 * <p>La tabla está <b>desnormalizada</b> (repite el nombre de departamento y provincia en cada
 * fila) para que los tres niveles del select se resuelvan con un {@code DISTINCT} y sin joins.
 * Con 1874 filas fijas, el costo de la redundancia es irrelevante frente a la simplicidad.
 *
 * <p>El {@code codigo} de seis dígitos es el que exige SUNAT como punto de partida y de llegada
 * en la guía de remisión electrónica.
 */
@Entity
@Table(name = "ubigeo", schema = "dbshopusuarios")
@Getter
@NoArgsConstructor
public class UbigeoEntity {

    @Id
    @Column(name = "codigo", length = 6, nullable = false, updatable = false)
    private String codigo;

    @Column(name = "departamento_codigo", length = 2, nullable = false)
    private String departamentoCodigo;

    @Column(name = "provincia_codigo", length = 4, nullable = false)
    private String provinciaCodigo;

    @Column(name = "departamento", length = 60, nullable = false)
    private String departamento;

    @Column(name = "provincia", length = 60, nullable = false)
    private String provincia;

    @Column(name = "distrito", length = 60, nullable = false)
    private String distrito;
}
