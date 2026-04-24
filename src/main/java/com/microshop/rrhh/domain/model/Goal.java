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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbshoprrhh", name = "goal", indexes = {
    @Index(name = "idx_goal_tenant", columnList = "tenant_id"),
    @Index(name = "idx_goal_employee", columnList = "employee_id"),
    @Index(name = "idx_goal_estado", columnList = "tenant_id,estado")
})
@Comment("Tabla de metas/objetivos")
@Getter
@Setter
@ToString(exclude = {"employee", "asignadoPor"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @Comment("Empleado")
    private Employee employee;

    @Column(name = "titulo", nullable = false, length = 200)
    @Comment("Título de la meta")
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    @Comment("Descripción de la meta")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    @Comment("Fecha de inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    @Comment("Fecha de fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado de la meta")
    @Builder.Default
    private GoalStatus estado = GoalStatus.EN_PROGRESO;

    @Column(name = "porcentaje_avance", precision = 5, scale = 2)
    @Comment("Porcentaje de avance")
    @Builder.Default
    private BigDecimal porcentajeAvance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 20)
    @Comment("Prioridad de la meta")
    private Priority prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por")
    @Comment("Empleado que asignó la meta")
    private Employee asignadoPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Comment("Fecha de última actualización")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum GoalStatus {
        EN_PROGRESO,
        COMPLETADO,
        CANCELADO,
        RETRASADO
    }

    public enum Priority {
        ALTA,
        MEDIA,
        BAJA
    }
}
