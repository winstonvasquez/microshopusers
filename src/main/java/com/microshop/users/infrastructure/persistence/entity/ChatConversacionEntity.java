package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.Instant;

/**
 * Entidad JPA que representa una conversación de soporte entre un cliente y el equipo MicroShop.
 */
@Entity
@Table(name = "chat_conversacion")
@Comment("Conversaciones de chat soporte cliente-MicroShop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    @Comment("ID del usuario cliente que inició la conversación")
    private Long clienteId;

    @Column(name = "asunto", length = 200)
    @Comment("Asunto o motivo de la conversación")
    private String asunto;

    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado de la conversación: ABIERTA o CERRADA")
    @Builder.Default
    private String estado = "ABIERTA";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación de la conversación")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "last_message_at", nullable = false)
    @Comment("Fecha del último mensaje en la conversación")
    @Builder.Default
    private Instant lastMessageAt = Instant.now();
}
