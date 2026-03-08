package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company")
@Comment("Tabla de empresas (tenants)")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CompanyEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("Nombre comercial de la empresa")
    private String name;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    @Comment("RUC o identificador fiscal")
    private String ruc;

    @Column(name = "is_active", nullable = false)
    @Comment("Estado de la empresa (Activa/Inactiva)")
    @Builder.Default
    private boolean isActive = true;
}
