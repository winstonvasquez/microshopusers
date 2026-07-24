package com.microshop.rrhh.config.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entidad de revisión de Hibernate Envers para el módulo RRHH.
 *
 * Reemplaza a {@code DefaultRevisionEntity} para calificar EXPLÍCITAMENTE la tabla de
 * revisiones {@code revinfo} y su secuencia {@code revinfo_seq} en el schema
 * {@code dbshoprrhh}. Motivo: la conexión usa {@code currentSchema=dbshopusuarios}, y la
 * entidad de revisión por defecto no lleva schema, por lo que Hibernate emitía
 * {@code nextval('revinfo_seq')} sin calificar y fallaba (la secuencia solo existe en
 * {@code dbshoprrhh}). Todas las FK {@code *_aud.rev} apuntan a {@code dbshoprrhh.revinfo},
 * así que este es el hogar correcto y consistente de las revisiones.
 *
 * {@code allocationSize = 50} coincide con el {@code increment_by} real de la secuencia
 * (optimizador pooled), para no romper su avance histórico.
 */
@Entity
@Table(schema = "dbshoprrhh", name = "revinfo")
@RevisionEntity
public class RrhhRevisionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revinfoGenerator")
    @SequenceGenerator(name = "revinfoGenerator", schema = "dbshoprrhh",
            sequenceName = "revinfo_seq", allocationSize = 50)
    @RevisionNumber
    @Column(name = "rev")
    private int id;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private long timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RrhhRevisionEntity that)) return false;
        return id == that.id && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp);
    }

    @Override
    public String toString() {
        return "RrhhRevisionEntity(id=" + id + ", revisionDate=" + getRevisionDate() + ")";
    }
}
