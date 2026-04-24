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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbshoprrhh", name = "position", indexes = {
    @Index(name = "idx_position_tenant", columnList = "tenant_id"),
    @Index(name = "idx_position_codigo", columnList = "tenant_id,codigo"),
    @Index(name = "idx_position_department", columnList = "department_id")
})
@Comment("Tabla de puestos/cargos")
@Getter
@Setter
@ToString(exclude = {"department", "employees"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "codigo", nullable = false, length = 20)
    @Comment("Código único del puesto")
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre del puesto")
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    @Comment("Descripción del puesto")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @Comment("Departamento al que pertenece")
    private Department department;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @Column(name = "nivel", length = 50)
    @Comment("Nivel del puesto (Junior, Senior, Manager, etc.)")
    private String nivel;

    @Column(name = "salario_minimo", precision = 10, scale = 2)
    @Comment("Salario mínimo del puesto")
    private BigDecimal salarioMinimo;

    @Column(name = "salario_maximo", precision = 10, scale = 2)
    @Comment("Salario máximo del puesto")
    private BigDecimal salarioMaximo;

    @Column(name = "requisitos", columnDefinition = "TEXT")
    @Comment("Requisitos del puesto")
    private String requisitos;

    @Column(name = "activo", nullable = false)
    @Comment("Indica si el puesto está activo")
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
