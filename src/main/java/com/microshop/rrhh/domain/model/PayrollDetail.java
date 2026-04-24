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
@Table(schema = "dbshoprrhh", name = "payroll_detail", indexes = {
    @Index(name = "idx_payroll_detail_tenant", columnList = "tenant_id"),
    @Index(name = "idx_payroll_detail_payroll", columnList = "payroll_id")
})
@Comment("Tabla de detalle de conceptos de planilla")
@Getter
@Setter
@ToString(exclude = {"payroll"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class PayrollDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    @Comment("Planilla")
    private Payroll payroll;

    @Column(name = "concepto", nullable = false, length = 100)
    @Comment("Concepto del pago/descuento")
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Comment("Tipo de concepto")
    private ConceptType tipo;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    @Comment("Monto del concepto")
    private BigDecimal monto;

    @Column(name = "cantidad", precision = 10, scale = 2)
    @Comment("Cantidad (para horas extras, días, etc.)")
    private BigDecimal cantidad;

    @Column(name = "tasa", precision = 5, scale = 2)
    @Comment("Tasa o porcentaje si aplica")
    private BigDecimal tasa;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ConceptType {
        INGRESO,
        DESCUENTO,
        APORTE_EMPLEADOR
    }
}
