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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(schema = "dbshoprrhh", name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_tenant_employee_fecha",
                         columnNames = {"tenant_id", "employee_id", "fecha"})
    },
    indexes = {
        @Index(name = "idx_attendance_tenant", columnList = "tenant_id"),
        @Index(name = "idx_attendance_employee_fecha", columnList = "tenant_id,employee_id,fecha")
    }
)
@Comment("Tabla de asistencia de empleados")
@Getter
@Setter
@ToString(exclude = {"employee", "aprobadoPor"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Attendance {

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

    @Column(name = "fecha", nullable = false)
    @Comment("Fecha de asistencia")
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    @Comment("Hora de entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    @Comment("Hora de salida")
    private LocalTime horaSalida;

    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    @Comment("Horas trabajadas en el día")
    private BigDecimal horasTrabajadas;

    @Column(name = "horas_extras", precision = 5, scale = 2)
    @Comment("Horas extras trabajadas")
    @Builder.Default
    private BigDecimal horasExtras = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false, length = 20)
    @Comment("Tipo de registro de asistencia")
    @Builder.Default
    private AttendanceType tipoRegistro = AttendanceType.NORMAL;

    @Column(name = "observaciones", length = 500)
    @Comment("Observaciones adicionales")
    private String observaciones;

    @Column(name = "justificacion", length = 500)
    @Comment("Justificación de tardanza o falta")
    private String justificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    @Comment("Empleado que aprobó la asistencia")
    private Employee aprobadoPor;

    @Column(name = "fecha_aprobacion")
    @Comment("Fecha de aprobación")
    private LocalDateTime fechaAprobacion;

    @Column(name = "ubicacion_entrada", length = 200)
    @Comment("Ubicación GPS de entrada")
    private String ubicacionEntrada;

    @Column(name = "ubicacion_salida", length = 200)
    @Comment("Ubicación GPS de salida")
    private String ubicacionSalida;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateHours();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateHours();
    }

    private void calculateHours() {
        if (horaEntrada != null && horaSalida != null) {
            Duration duration = Duration.between(horaEntrada, horaSalida);
            double hours = duration.toMinutes() / 60.0;
            horasTrabajadas = BigDecimal.valueOf(hours);
            
            // Calcular horas extras (más de 8 horas)
            if (hours > 8) {
                horasExtras = BigDecimal.valueOf(hours - 8);
            }
        }
    }

    public enum AttendanceType {
        NORMAL,
        TARDANZA,
        FALTA,
        PERMISO,
        LICENCIA,
        VACACIONES
    }
}
