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
@Table(name = "company_rubro", uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_rubro", columnNames = { "company_id", "rubro_id" })
})
@Comment("Relación N:M entre empresas y rubros - Una empresa puede operar en múltiples sectores")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "company", "rubro" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRubroEntity extends AuditEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        CompanyRubroEntity that = (CompanyRubroEntity) o;
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
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cr_company"))
    @Comment("Empresa asociada")
    private CompanyEntity company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cr_rubro"))
    @Comment("Rubro o sector de negocio")
    private RubroEntity rubro;
}
