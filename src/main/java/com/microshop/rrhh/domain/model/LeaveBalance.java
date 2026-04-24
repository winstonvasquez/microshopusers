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
@Table(schema = "dbshoprrhh", name = "leave_balance", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_balance_tenant_employee_anio", 
                         columnNames = {"tenant_id", "employee_id", "anio"})
    },
    indexes = {
        @Index(name = "idx_leave_balance_tenant", columnList = "tenant_id"),
        @Index(name = "idx_leave_balance_employee", columnList = "employee_id")
    }
)
@Comment("Tabla de balance de vacaciones")
@Getter
@Setter
@ToString(exclude = {"employee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class LeaveBalance {

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

    @Column(name = "anio", nullable = false)
    @Comment("Año del balance")
    private Integer anio;

    @Column(name = "dias_ganados", nullable = false, precision = 5, scale = 2)
    @Comment("Días de vacaciones ganados en el año")
    private BigDecimal diasGanados;

    @Column(name = "dias_usados", precision = 5, scale = 2)
    @Comment("Días de vacaciones usados")
    @Builder.Default
    private BigDecimal diasUsados = BigDecimal.ZERO;

    @Column(name = "dias_disponibles", nullable = false, precision = 5, scale = 2)
    @Comment("Días de vacaciones disponibles")
    private BigDecimal diasDisponibles;

    @Column(name = "dias_vencidos", precision = 5, scale = 2)
    @Comment("Días de vacaciones vencidos")
    @Builder.Default
    private BigDecimal diasVencidos = BigDecimal.ZERO;

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
        if (diasDisponibles == null) {
            diasDisponibles = diasGanados.subtract(diasUsados);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        diasDisponibles = diasGanados.subtract(diasUsados).subtract(diasVencidos);
    }
}
