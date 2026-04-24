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

@Entity
@Table(schema = "dbshoprrhh", name = "training_participation", indexes = {
    @Index(name = "idx_training_part_tenant", columnList = "tenant_id"),
    @Index(name = "idx_training_part_training", columnList = "tenant_id,training_id"),
    @Index(name = "idx_training_part_employee", columnList = "tenant_id,employee_id")
})
@Comment("Tabla de participación en capacitaciones")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class TrainingParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "training_id", nullable = false)
    @Comment("ID del curso")
    private Long trainingId;

    @Column(name = "employee_id", nullable = false)
    @Comment("ID del empleado")
    private Long employeeId;

    @Column(name = "fecha_inscripcion", nullable = false)
    @Comment("Fecha de inscripción")
    private LocalDate fechaInscripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado de participación")
    @Builder.Default
    private ParticipationStatus estado = ParticipationStatus.INSCRITO;

    @Column(name = "asistencia_porcentaje", precision = 5, scale = 2)
    @Comment("Porcentaje de asistencia")
    private BigDecimal asistenciaPorcentaje;

    @Column(name = "nota_final", precision = 5, scale = 2)
    @Comment("Nota final obtenida")
    private BigDecimal notaFinal;

    @Column(name = "aprobado")
    @Comment("Indica si aprobó el curso")
    private Boolean aprobado;

    @Column(name = "certificado_emitido")
    @Comment("Indica si se emitió certificado")
    @Builder.Default
    private Boolean certificadoEmitido = false;

    @Column(name = "comentarios", length = 500)
    @Comment("Comentarios adicionales")
    private String comentarios;

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

    public enum ParticipationStatus {
        INSCRITO,
        EN_CURSO,
        COMPLETADO,
        ABANDONADO,
        REPROBADO
    }
}
