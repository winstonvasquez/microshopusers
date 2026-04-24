package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import java.time.Instant;

@Entity
@Table(name = "saas_subscription")
@Comment("Suscripción activa de cada empresa a un plan SaaS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaasSubscriptionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyEntity company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SaasPlanEntity plan;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "TRIAL";

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;
}
