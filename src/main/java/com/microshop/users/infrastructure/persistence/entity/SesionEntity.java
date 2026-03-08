package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.Instant;

@Entity
@Table(name = "sesion")
@Comment("Tabla de sesiones de usuario activas e históricas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SesionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 2048)
    @Comment("Token de sesión (JWT u opaco)")
    private String token;

    @Column(name = "fecha_inicio", nullable = false)
    @Comment("Fecha y hora de inicio de sesión")
    private Instant fechaInicio;

    @Column(name = "fecha_expiracion", nullable = false)
    @Comment("Fecha y hora de expiración del token")
    private Instant fechaExpiracion;

    @Column(name = "ip_address", length = 45)
    @Comment("Dirección IP desde donde se conectó")
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    @Comment("Agente de usuario (Navegador/SO)")
    private String userAgent;

    @Column(name = "valido", nullable = false)
    @Comment("Indica si la sesión es válida o ha sido revocada")
    @Builder.Default
    private boolean valido = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "sesion_usuario_fk"))
    @Comment("Usuario propietario de la sesión")
    private UsuarioEntity usuario;

    @Column(name = "company_id")
    @Comment("ID de la empresa activa en esta sesión (contexto)")
    private Long companyId;
}
