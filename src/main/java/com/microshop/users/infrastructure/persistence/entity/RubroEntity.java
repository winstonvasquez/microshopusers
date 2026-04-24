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
@Table(name = "rubro")
@Comment("Tabla maestra de rubros o sectores de negocio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class RubroEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    @Comment("Código único del rubro (ej. RETAIL, FOOD, TECH)")
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre descriptivo del rubro")
    private String nombre;
}
