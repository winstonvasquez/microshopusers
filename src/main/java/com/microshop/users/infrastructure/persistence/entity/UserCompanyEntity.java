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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

// M05 — auditoria de escalada de privilegios. `AuditEntity` ya dejaba «quien toco por ultima vez y
// cuando» (@PreUpdate sobre usuario_modificacion/fecha_modificacion), pero NO el valor anterior, que es
// justo lo que importa para reconstruir un cambio de rol o una suspension de empresa. Envers es como
// este proyecto ya audita: 19 entidades en rrhh, con una unica @RevisionEntity compartida
// (RrhhRevisionEntity). Las tablas *_aud las crea usuarios/V38.
@Audited
@Entity
@Table(name = "user_company", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_company", columnNames = { "usuario_id", "company_id" })
})
@Comment("Tabla intermedia usuarios por empresa")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "roles" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompanyEntity extends AuditEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        UserCompanyEntity that = (UserCompanyEntity) o;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_company_user"))
    @Comment("Usuario asociado")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_company_company"))
    @Comment("Empresa asociada")
    private CompanyEntity company;

    @Column(name = "is_active", nullable = false)
    @Comment("Si el usuario está activo en esta empresa")
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "userCompany", fetch = FetchType.LAZY)
    @Builder.Default
    // UserCompanyRoleEntity no se audita: auditarla exigiría su propia tabla _aud, y M05 pide el
    // rastro del rol del usuario y de la membresía, que ya quedan cubiertos.
    //
    // Se excluye con @NotAudited y NO con @Audited(targetAuditMode = NOT_AUDITED): ese modo sólo
    // vale para relaciones *to-one*. En una colección, Envers exige que el destino esté auditado y
    // aborta el arranque con "An audited relation from ... to a not audited entity" — que es
    // exactamente lo que pasó al intentarlo así.
    @NotAudited
    private Set<UserCompanyRoleEntity> roles = new HashSet<>();
}
