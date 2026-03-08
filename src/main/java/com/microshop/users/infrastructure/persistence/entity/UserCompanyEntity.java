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
    private Set<UserCompanyRoleEntity> roles = new HashSet<>();
}
