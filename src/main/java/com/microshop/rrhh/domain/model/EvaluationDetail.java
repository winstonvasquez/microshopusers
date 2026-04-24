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
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbshoprrhh", name = "evaluation_detail", indexes = {
    @Index(name = "idx_evaluation_detail_tenant", columnList = "tenant_id"),
    @Index(name = "idx_evaluation_detail_evaluation", columnList = "evaluation_id"),
    @Index(name = "idx_evaluation_detail_criteria", columnList = "criteria_id")
})
@Comment("Tabla de detalle de evaluación por criterio")
@Getter
@Setter
@ToString(exclude = {"evaluation", "criteria"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class EvaluationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    @Comment("Evaluación de desempeño")
    private PerformanceEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    @Comment("Criterio de evaluación")
    private EvaluationCriteria criteria;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    @Comment("Puntaje obtenido en este criterio")
    private BigDecimal puntaje;

    @Column(name = "comentarios", length = 1000)
    @Comment("Comentarios sobre este criterio")
    private String comentarios;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
