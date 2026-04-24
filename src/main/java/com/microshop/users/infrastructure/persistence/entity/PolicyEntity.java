package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "policy")
@Comment("Tabla de políticas PBAC del sistema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class PolicyEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    @Comment("Código único de la política (ej. VENTA_APROBACION_SUPERVISOR)")
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre descriptivo de la política")
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    @Comment("Descripción detallada de la política")
    private String descripcion;

    @Column(name = "efecto", nullable = false, length = 10)
    @Comment("Efecto de la política: ALLOW o DENY")
    private String efecto;

    @Column(name = "prioridad", nullable = false)
    @Comment("Prioridad para resolución de conflictos (mayor = más prioritario)")
    @Builder.Default
    private Integer prioridad = 1;
}
