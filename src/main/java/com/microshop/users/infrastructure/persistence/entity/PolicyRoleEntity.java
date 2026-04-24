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
@Table(name = "policy_role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_role", columnNames = { "policy_id", "rol_id" })
})
@Comment("Relación N:M entre políticas y roles - Punto de integración PBAC con RBAC")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "policy", "rol" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRoleEntity extends AuditEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        PolicyRoleEntity that = (PolicyRoleEntity) o;
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
    @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_policy"))
    @Comment("Política asociada")
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_rol"))
    @Comment("Rol al que aplica la política")
    private RolEntity rol;
}
