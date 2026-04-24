package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad que representa un token temporal para recuperación de contraseña.
 * Cada token es de un solo uso y expira en 1 hora.
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario al que pertenece el token. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** UUID aleatorio que actúa como token de recuperación. */
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    /** Fecha y hora de expiración (1 hora desde la creación). */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Indica si el token ya fue utilizado para cambiar la contraseña. */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
