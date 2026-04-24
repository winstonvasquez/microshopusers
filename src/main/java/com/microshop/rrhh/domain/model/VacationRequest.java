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
@Table(schema = "dbshoprrhh", name = "vacation_request", indexes = {
    @Index(name = "idx_vacation_tenant", columnList = "tenant_id"),
    @Index(name = "idx_vacation_employee", columnList = "tenant_id,employee_id"),
    @Index(name = "idx_vacation_estado", columnList = "tenant_id,estado")
})
@Comment("Tabla de solicitudes de vacaciones")
@Getter
@Setter
@ToString(exclude = {"employee", "reemplazo", "aprobadoPor"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class VacationRequest {

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
    @Comment("Fecha de inicio de vacaciones")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    @Comment("Fecha de fin de vacaciones")
    private LocalDate fechaFin;

    @Column(name = "dias", nullable = false)
    @Comment("Cantidad de días de vacaciones")
    private Integer dias;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vacacion", length = 50)
    @Comment("Tipo de vacación")
    @Builder.Default
    private VacationType tipoVacacion = VacationType.ANUAL;

    @Column(name = "balance_usado", precision = 5, scale = 2)
    @Comment("Balance de días usado")
    private BigDecimal balanceUsado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reemplazo_id")
    @Comment("Empleado que reemplazará durante las vacaciones")
    private Employee reemplazo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado de la solicitud")
    @Builder.Default
    private VacationStatus estado = VacationStatus.SOLICITADO;

    @Column(name = "motivo", length = 500)
    @Comment("Motivo de las vacaciones")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    @Comment("Empleado que aprobó/rechazó")
    private Employee aprobadoPor;

    @Column(name = "fecha_aprobacion")
    @Comment("Fecha de aprobación/rechazo")
    private LocalDate fechaAprobacion;

    @Column(name = "comentarios_aprobacion", length = 500)
    @Comment("Comentarios de aprobación/rechazo")
    private String comentariosAprobacion;

    @Column(name = "documento_url", length = 500)
    @Comment("URL del documento de solicitud")
    private String documentoUrl;

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

    public enum VacationStatus {
        SOLICITADO,
        APROBADO,
        RECHAZADO,
        TOMADO,
        CANCELADO
    }

    public enum VacationType {
        ANUAL,
        TRUNCAS,
        COMPENSATORIAS,
        SIN_GOCE
    }
}
