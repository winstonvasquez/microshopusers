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
@Table(schema = "dbshoprrhh", name = "salary", indexes = {
    @Index(name = "idx_salary_tenant", columnList = "tenant_id"),
    @Index(name = "idx_salary_employee", columnList = "employee_id"),
    @Index(name = "idx_salary_fecha", columnList = "tenant_id,employee_id,fecha_inicio")
})
@Comment("Tabla de historial salarial")
@Getter
@Setter
@ToString(exclude = {"employee", "aprobadoPor"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Salary {

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

    @Column(name = "fecha_inicio", nullable = false)
    @Comment("Fecha de inicio del salario")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    @Comment("Fecha de fin del salario")
    private LocalDate fechaFin;

    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    @Comment("Salario base")
    private BigDecimal salarioBase;

    @Column(name = "moneda", length = 3)
    @Comment("Moneda del salario")
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", length = 100)
    @Comment("Motivo del cambio salarial")
    private SalaryChangeReason motivo;

    @Column(name = "porcentaje_incremento", precision = 5, scale = 2)
    @Comment("Porcentaje de incremento")
    private BigDecimal porcentajeIncremento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    @Comment("Empleado que aprobó el cambio")
    private Employee aprobadoPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SalaryChangeReason {
        INCREMENTO,
        PROMOCION,
        AJUSTE_MERCADO,
        CAMBIO_PUESTO,
        NEGOCIACION,
        AJUSTE_INFLACION
    }
}
