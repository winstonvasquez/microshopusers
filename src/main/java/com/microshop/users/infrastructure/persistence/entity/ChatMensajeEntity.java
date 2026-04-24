package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.Instant;

/**
 * Entidad JPA que representa un mensaje dentro de una conversación de soporte.
 */
@Entity
@Table(name = "chat_mensaje",
        indexes = @Index(name = "idx_chat_mensaje_conv", columnList = "conversacion_id, timestamp"))
@Comment("Mensajes de chat soporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMensajeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "conversacion_id", nullable = false)
    @Comment("ID de la conversación a la que pertenece el mensaje")
    private Long conversacionId;

    @Column(name = "emisor_id", nullable = false)
    @Comment("ID del usuario que envió el mensaje")
    private Long emisorId;

    @Column(name = "emisor_tipo", nullable = false, length = 10)
    @Comment("Tipo de emisor: CLIENTE o SOPORTE")
    private String emisorTipo;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    @Comment("Contenido del mensaje")
    private String contenido;

    @Column(name = "timestamp", nullable = false)
    @Comment("Marca de tiempo del mensaje")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Column(name = "leido", nullable = false)
    @Comment("Indica si el mensaje fue leído por el destinatario")
    @Builder.Default
    private boolean leido = false;
}
