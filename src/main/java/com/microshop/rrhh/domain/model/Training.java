package com.microshop.rrhh.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(schema = "dbshoprrhh", name = "training", indexes = {
    @Index(name = "idx_training_tenant", columnList = "tenant_id")
})
@Comment("Tabla de cursos de capacitación")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "nombre", nullable = false, length = 200)
    @Comment("Nombre del curso")
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    @Comment("Descripción del curso")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    @Comment("Fecha de inicio del curso")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    @Comment("Fecha de fin del curso")
    private LocalDate fechaFin;

    @Column(name = "instructor", length = 200)
    @Comment("Nombre del instructor")
    private String instructor;

    @Column(name = "duracion_horas")
    @Comment("Duración en horas")
    private Integer duracionHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado del curso")
    @Builder.Default
    private TrainingStatus estado = TrainingStatus.PLANIFICADO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    @Comment("Fecha de última actualización")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    public enum TrainingStatus {
        PLANIFICADO,
        EN_CURSO,
        COMPLETADO,
        CANCELADO
    }
}
