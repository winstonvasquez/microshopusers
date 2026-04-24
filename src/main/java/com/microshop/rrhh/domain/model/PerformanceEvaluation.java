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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbshoprrhh", name = "performance_evaluation", indexes = {
    @Index(name = "idx_evaluation_tenant", columnList = "tenant_id"),
    @Index(name = "idx_evaluation_employee", columnList = "tenant_id,employee_id")
})
@Comment("Tabla de evaluaciones de desempeño")
@Getter
@Setter
@ToString(exclude = {"employee", "evaluador", "details"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class PerformanceEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @Comment("Empleado evaluado")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id", nullable = false)
    @Comment("Evaluador")
    private Employee evaluador;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvaluationDetail> details = new ArrayList<>();

    @Column(name = "periodo", nullable = false, length = 7)
    @Comment("Periodo de evaluación (YYYY-MM)")
    private String periodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evaluacion", length = 50)
    @Comment("Tipo de evaluación")
    @Builder.Default
    private EvaluationType tipoEvaluacion = EvaluationType.ANUAL;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    @Comment("Puntaje total de la evaluación")
    private BigDecimal puntaje;

    @Column(name = "comentarios", length = 2000)
    @Comment("Comentarios generales")
    private String comentarios;

    @Column(name = "fortalezas", length = 1000)
    @Comment("Fortalezas identificadas")
    private String fortalezas;

    @Column(name = "areas_mejora", length = 1000)
    @Comment("Áreas de mejora")
    private String areasMejora;

    @Column(name = "plan_mejora", columnDefinition = "TEXT")
    @Comment("Plan de mejora")
    private String planMejora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Comment("Estado de la evaluación")
    @Builder.Default
    private EvaluationStatus estado = EvaluationStatus.BORRADOR;

    @Column(name = "fecha_evaluacion", nullable = false)
    @Comment("Fecha de la evaluación")
    private LocalDate fechaEvaluacion;

    @Column(name = "proxima_revision")
    @Comment("Fecha de próxima revisión")
    private LocalDate proximaRevision;

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

    public enum EvaluationType {
        ANUAL,
        SEMESTRAL,
        TRIMESTRAL,
        PERIODO_PRUEBA,
        PROMOCION
    }

    public enum EvaluationStatus {
        BORRADOR,
        COMPLETADA,
        APROBADA,
        CANCELADA
    }
}
