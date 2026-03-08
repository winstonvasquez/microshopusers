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
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @Comment("Correo electrónico corporativo")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false, foreignKey = @ForeignKey(name = "usuario_rol_fk"))
    @Comment("Rol asignado al usuario")
    private RolEntity rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false, foreignKey = @ForeignKey(name = "usuario_persona_fk"))
    @Comment("Persona asociada al usuario")
    private PersonaEntity persona;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserCompanyEntity> userCompanies = new HashSet<>();
}
