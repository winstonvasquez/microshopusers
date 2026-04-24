package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import java.math.BigDecimal;

@Entity
@Table(name = "saas_plan")
@Comment("Planes de suscripción SaaS disponibles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaasPlanEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price_monthly", precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "price_annual", precision = 10, scale = 2)
    private BigDecimal priceAnnual;

    @Column(name = "max_users", nullable = false)
    @Builder.Default
    private Integer maxUsers = 5;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
