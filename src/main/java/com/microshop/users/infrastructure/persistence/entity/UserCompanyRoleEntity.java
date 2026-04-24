package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user_company_role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_company_role", columnNames = { "user_company_id", "rol_id" })
})
@Comment("Roles asignados a un usuario dentro de una empresa")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "userCompany", "rol" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompanyRoleEntity extends AuditEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        UserCompanyRoleEntity that = (UserCompanyRoleEntity) o;
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
    @JoinColumn(name = "user_company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ucr_user_company"))
    @Comment("Relación usuario-empresa")
    private UserCompanyEntity userCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ucr_rol"))
    @Comment("Rol asignado")
    private RolEntity rol;
}
