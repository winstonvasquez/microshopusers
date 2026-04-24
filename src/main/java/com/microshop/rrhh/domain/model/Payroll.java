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
@Table(schema = "dbshoprrhh", name = "payroll", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_tenant_employee_periodo", 
                         columnNames = {"tenant_id", "employee_id", "periodo"})
    },
    indexes = {
        @Index(name = "idx_payroll_tenant", columnList = "tenant_id"),
        @Index(name = "idx_payroll_employee_periodo", columnList = "tenant_id,employee_id,periodo")
    }
)
@Comment("Tabla de planillas de pago")
@Getter
@Setter
@ToString(exclude = {"employee", "details"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Payroll {

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

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayrollDetail> details = new ArrayList<>();

    @Column(name = "periodo", nullable = false, length = 7)
    @Comment("Periodo de la planilla (YYYY-MM)")
    private String periodo;

    @Column(name = "sueldo_base", nullable = false, precision = 10, scale = 2)
    @Comment("Sueldo base del empleado")
    private BigDecimal sueldoBase;

    @Column(name = "bonos", precision = 10, scale = 2)
    @Comment("Bonos y otros ingresos")
    @Builder.Default
    private BigDecimal bonos = BigDecimal.ZERO;

    @Column(name = "descuentos", precision = 10, scale = 2)
    @Comment("Descuentos aplicados")
    @Builder.Default
    private BigDecimal descuentos = BigDecimal.ZERO;

    @Column(name = "afp_onp", length = 50)
    @Comment("Sistema de pensiones (AFP u ONP)")
    private String afpOnp;

    @Column(name = "monto_afp_onp", precision = 10, scale = 2)
    @Comment("Monto de AFP u ONP")
    @Builder.Default
    private BigDecimal montoAfpOnp = BigDecimal.ZERO;

    @Column(name = "essalud", precision = 10, scale = 2)
    @Comment("Aporte a EsSalud")
    @Builder.Default
    private BigDecimal essalud = BigDecimal.ZERO;

    @Column(name = "renta_quinta", precision = 10, scale = 2)
    @Comment("Impuesto a la renta de quinta categoría")
    @Builder.Default
    private BigDecimal rentaQuinta = BigDecimal.ZERO;

    @Column(name = "cts", precision = 10, scale = 2)
    @Comment("Compensación por Tiempo de Servicios")
    @Builder.Default
    private BigDecimal cts = BigDecimal.ZERO;

    @Column(name = "gratificacion", precision = 10, scale = 2)
    @Comment("Gratificación")
    @Builder.Default
    private BigDecimal gratificacion = BigDecimal.ZERO;

    @Column(name = "asignacion_familiar", precision = 10, scale = 2)
    @Comment("Asignación familiar")
    @Builder.Default
    private BigDecimal asignacionFamiliar = BigDecimal.ZERO;

    @Column(name = "dias_trabajados")
    @Comment("Días trabajados en el periodo")
    private Integer diasTrabajados;

    @Column(name = "horas_extras", precision = 5, scale = 2)
    @Comment("Horas extras trabajadas")
    @Builder.Default
    private BigDecimal horasExtras = BigDecimal.ZERO;

    @Column(name = "monto_horas_extras", precision = 10, scale = 2)
    @Comment("Monto por horas extras")
    @Builder.Default
    private BigDecimal montoHorasExtras = BigDecimal.ZERO;

    @Column(name = "neto", nullable = false, precision = 10, scale = 2)
    @Comment("Monto neto a pagar")
    private BigDecimal neto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado de la planilla")
    @Builder.Default
    private PayrollStatus estado = PayrollStatus.GENERADO;

    @Column(name = "fecha_pago")
    @Comment("Fecha de pago efectivo")
    private LocalDate fechaPago;

    @Column(name = "pago_id")
    @Comment("ID del pago en tesorería")
    private Long pagoId;

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
        calculateNeto();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNeto();
    }

    private void calculateNeto() {
        if (sueldoBase != null) {
            BigDecimal totalIngresos = sueldoBase
                .add(bonos != null ? bonos : BigDecimal.ZERO)
                .add(montoHorasExtras != null ? montoHorasExtras : BigDecimal.ZERO)
                .add(asignacionFamiliar != null ? asignacionFamiliar : BigDecimal.ZERO);
            
            BigDecimal totalDescuentos = (descuentos != null ? descuentos : BigDecimal.ZERO)
                .add(montoAfpOnp != null ? montoAfpOnp : BigDecimal.ZERO)
                .add(rentaQuinta != null ? rentaQuinta : BigDecimal.ZERO);
            
            neto = totalIngresos.subtract(totalDescuentos);
        }
    }

    public enum PayrollStatus {
        GENERADO,
        APROBADO,
        PAGADO,
        CANCELADO
    }
}
