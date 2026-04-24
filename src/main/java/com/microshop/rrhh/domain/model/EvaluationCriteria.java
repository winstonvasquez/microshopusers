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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbshoprrhh", name = "evaluation_criteria", indexes = {
    @Index(name = "idx_evaluation_criteria_tenant", columnList = "tenant_id")
})
@Comment("Tabla de criterios de evaluación")
@Getter
@Setter
@ToString(exclude = {"evaluationDetails"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class EvaluationCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre del criterio")
    private String nombre;

    @Column(name = "descripcion", length = 500)
    @Comment("Descripción del criterio")
    private String descripcion;

    @Column(name = "peso_porcentaje", nullable = false, precision = 5, scale = 2)
    @Comment("Peso del criterio en la evaluación total")
    private BigDecimal pesoPorcentaje;

    @Column(name = "puntaje_minimo", precision = 5, scale = 2)
    @Comment("Puntaje mínimo")
    @Builder.Default
    private BigDecimal puntajeMinimo = BigDecimal.ZERO;

    @Column(name = "puntaje_maximo", precision = 5, scale = 2)
    @Comment("Puntaje máximo")
    @Builder.Default
    private BigDecimal puntajeMaximo = new BigDecimal("100");

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL)
    @Builder.Default
    private List<EvaluationDetail> evaluationDetails = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    @Comment("Indica si el criterio está activo")
    @Builder.Default
    private Boolean activo = true;

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
}
