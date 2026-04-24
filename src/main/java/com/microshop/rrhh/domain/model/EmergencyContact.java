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

import java.time.LocalDateTime;

@Entity
@Table(schema = "dbshoprrhh", name = "emergency_contact", indexes = {
    @Index(name = "idx_emergency_contact_tenant", columnList = "tenant_id"),
    @Index(name = "idx_emergency_contact_employee", columnList = "employee_id")
})
@Comment("Tabla de contactos de emergencia")
@Getter
@Setter
@ToString(exclude = {"employee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class EmergencyContact {

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

    @Column(name = "nombre_completo", nullable = false, length = 200)
    @Comment("Nombre completo del contacto")
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "relacion", nullable = false, length = 50)
    @Comment("Relación con el empleado")
    private Relationship relacion;

    @Column(name = "telefono", nullable = false, length = 20)
    @Comment("Teléfono principal")
    private String telefono;

    @Column(name = "telefono_alternativo", length = 20)
    @Comment("Teléfono alternativo")
    private String telefonoAlternativo;

    @Column(name = "direccion", length = 500)
    @Comment("Dirección del contacto")
    private String direccion;

    @Column(name = "es_principal", nullable = false)
    @Comment("Indica si es el contacto principal")
    @Builder.Default
    private Boolean esPrincipal = false;

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

    public enum Relationship {
        PADRE,
        MADRE,
        CONYUGE,
        HIJO,
        HIJA,
        HERMANO,
        HERMANA,
        OTRO
    }
}
