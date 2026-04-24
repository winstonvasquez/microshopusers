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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbshoprrhh", name = "dependent", indexes = {
    @Index(name = "idx_dependent_tenant", columnList = "tenant_id"),
    @Index(name = "idx_dependent_employee", columnList = "employee_id")
})
@Comment("Tabla de dependientes/familiares")
@Getter
@Setter
@ToString(exclude = {"employee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Dependent {

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
    @Comment("Nombre completo del dependiente")
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "relacion", nullable = false, length = 50)
    @Comment("Relación con el empleado")
    private Relationship relacion;

    @Column(name = "fecha_nacimiento", nullable = false)
    @Comment("Fecha de nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "documento_identidad", length = 20)
    @Comment("Documento de identidad")
    private String documentoIdentidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    @Comment("Género")
    private Gender genero;

    @Column(name = "es_beneficiario_seguro", nullable = false)
    @Comment("Es beneficiario del seguro")
    @Builder.Default
    private Boolean esBeneficiarioSeguro = false;

    @Column(name = "es_carga_familiar", nullable = false)
    @Comment("Es carga familiar")
    @Builder.Default
    private Boolean esCargaFamiliar = true;

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
        CONYUGE,
        HIJO,
        HIJA,
        PADRE,
        MADRE,
        HERMANO,
        HERMANA
    }

    public enum Gender {
        MASCULINO,
        FEMENINO,
        OTRO
    }
}
