package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Notificación in-app para un usuario.
 * No extiende AuditEntity — es una entidad de inbox con esquema propio.
 */
@Entity
@Table(name = "notifications")
@Comment("Notificaciones in-app por usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_user"))
    @Comment("Usuario destinatario")
    private UsuarioEntity usuario;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "type", nullable = false, length = 50)
    @Comment("Tipo: ORDER_STATUS_CHANGE | REVIEW_REQUEST | PROMO_OFFER | SYSTEM")
    private String type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;
}
