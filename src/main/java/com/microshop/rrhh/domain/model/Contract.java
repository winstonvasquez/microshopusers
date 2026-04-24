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
@Table(schema = "dbshoprrhh", name = "contract", indexes = {
    @Index(name = "idx_contract_tenant", columnList = "tenant_id"),
    @Index(name = "idx_contract_employee", columnList = "employee_id"),
    @Index(name = "idx_contract_estado", columnList = "tenant_id,estado")
})
@Comment("Tabla de contratos laborales")
@Getter
@Setter
@ToString(exclude = {"employee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @Comment("Empleado del contrato")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false, length = 50)
    @Comment("Tipo de contrato")
    private ContractType tipoContrato;

    @Column(name = "fecha_inicio", nullable = false)
    @Comment("Fecha de inicio del contrato")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    @Comment("Fecha de fin del contrato (null si es indefinido)")
    private LocalDate fechaFin;

    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    @Comment("Salario base del contrato")
    private BigDecimal salarioBase;

    @Column(name = "moneda", length = 3)
    @Comment("Moneda del salario")
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "jornada_laboral", length = 50)
    @Comment("Tipo de jornada laboral")
    private WorkingDay jornadaLaboral;

    @Column(name = "horas_semanales")
    @Comment("Horas de trabajo semanales")
    private Integer horasSemanales;

    @Column(name = "periodo_prueba_meses")
    @Comment("Periodo de prueba en meses")
    private Integer periodoPruebaMeses;

    @Column(name = "documento_contrato_url", length = 500)
    @Comment("URL del documento del contrato")
    private String documentoContratoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado del contrato")
    @Builder.Default
    private ContractStatus estado = ContractStatus.ACTIVO;

    @Column(name = "motivo_fin", length = 500)
    @Comment("Motivo de finalización del contrato")
    private String motivoFin;

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

    public enum ContractType {
        INDEFINIDO,
        PLAZO_FIJO,
        TEMPORAL,
        PRACTICAS,
        LOCACION_SERVICIOS
    }

    public enum WorkingDay {
        COMPLETA,
        PARCIAL,
        REDUCIDA
    }

    public enum ContractStatus {
        ACTIVO,
        FINALIZADO,
        SUSPENDIDO,
        RENOVADO
    }
}
