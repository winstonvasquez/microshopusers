package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "saas_module")
@Comment("Catálogo de módulos ERP disponibles en la plataforma SaaS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaasModuleEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @Comment("Código único del módulo (POS, VENTAS, COMPRAS, ...)")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "route_prefix", length = 100)
    private String routePrefix;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
