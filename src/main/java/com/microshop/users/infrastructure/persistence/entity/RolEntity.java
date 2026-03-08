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
@Table(name = "rol")
@Comment("Tabla maestra de roles del sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RolEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    @Comment("Nombre único del rol (ej. ADMIN, USER)")
    private String nombre;

    @Column(name = "descripcion", length = 255)
    @Comment("Descripción opcional del rol")
    private String descripcion;
}
