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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbshoprrhh", name = "department", indexes = {
    @Index(name = "idx_department_tenant", columnList = "tenant_id"),
    @Index(name = "idx_department_codigo", columnList = "tenant_id,codigo")
})
@Comment("Tabla de departamentos/áreas")
@Getter
@Setter
@ToString(exclude = {"manager", "parent", "subDepartments", "employees", "positions"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "codigo", nullable = false, length = 20)
    @Comment("Código único del departamento")
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre del departamento")
    private String nombre;

    @Column(name = "descripcion", length = 500)
    @Comment("Descripción del departamento")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @Comment("Gerente/Jefe del departamento")
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Comment("Departamento padre (jerarquía)")
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Department> subDepartments = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Position> positions = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    @Comment("Indica si el departamento está activo")
    @Builder.Default
    private Boolean activo = true;

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
}
