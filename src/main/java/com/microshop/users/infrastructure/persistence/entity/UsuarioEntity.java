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
import jakarta.persistence.OneToMany;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

// M05 — auditoria de escalada de privilegios. `AuditEntity` ya dejaba «quien toco por ultima vez y
// cuando» (@PreUpdate sobre usuario_modificacion/fecha_modificacion), pero NO el valor anterior, que es
// justo lo que importa para reconstruir un cambio de rol o una suspension de empresa. Envers es como
// este proyecto ya audita: 19 entidades en rrhh, con una unica @RevisionEntity compartida
// (RrhhRevisionEntity). Las tablas *_aud las crea usuarios/V38.
@Audited
@Entity
@Table(name = "usuario")
@Comment("Tabla de usuarios del sistema con credenciales")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "userCompanies", "password" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity extends AuditEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        UsuarioEntity that = (UsuarioEntity) o;
        return getId() != null && java.util.Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @Comment("Identificador único de inicio de sesión")
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    @Comment("Contraseña encriptada")
    // NO se audita: el historial de una escalada de privilegios no necesita los hashes de
    // credenciales, y una tabla _aud acumulandolos seria un sitio mas donde viven, con retencion
    // indefinida y otros permisos de lectura. Envers los incluiria por defecto.
    @NotAudited
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @Comment("Correo electrónico corporativo")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false, foreignKey = @ForeignKey(name = "usuario_rol_fk"))
    @Comment("Rol asignado al usuario")
    // El rol es el dato que se quiere auditar, pero RolEntity en si no se audita: interesa QUE rol
    // tenia el usuario, no el historial del catalogo de roles. Sin NOT_AUDITED Envers falla al
    // arrancar con "An audited relation to a non-audited entity".
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private RolEntity rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false, foreignKey = @ForeignKey(name = "usuario_persona_fk"))
    @Comment("Persona asociada al usuario")
    // PersonaEntity no se audita (datos personales, fuera del alcance de M05).
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PersonaEntity persona;

    @Column(name = "pin_hash", length = 100)
    @Comment("Hash del PIN numérico para login rápido en POS")
    // NO se audita: el historial de una escalada de privilegios no necesita los hashes de
    // credenciales, y una tabla _aud acumulandolos seria un sitio mas donde viven, con retencion
    // indefinida y otros permisos de lectura. Envers los incluiria por defecto.
    @NotAudited
    private String pinHash;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserCompanyEntity> userCompanies = new HashSet<>();
}
