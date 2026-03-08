package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditEntity {

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private Instant fechaCreacion;

    @Column(name = "usuario_creacion", length = 50, nullable = false, updatable = false)
    @Comment("Usuario que creó el registro")
    private String usuarioCreacion;

    @Column(name = "fecha_modificacion")
    @Comment("Fecha de última modificación")
    private Instant fechaModificacion;

    @Column(name = "usuario_modificacion", length = 50)
    @Comment("Usuario que modificó por última vez")
    private String usuarioModificacion;

    @Column(name = "activo", nullable = false)
    @Comment("Indica si el registro está activo (Borrado lógico)")
    private boolean activo = true;

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        fechaCreacion = now;
        fechaModificacion = now;
        String user = getCurrentUser();
        usuarioCreacion = user;
        usuarioModificacion = user;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaModificacion = Instant.now();
        usuarioModificacion = getCurrentUser();
    }

    private String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }
}
