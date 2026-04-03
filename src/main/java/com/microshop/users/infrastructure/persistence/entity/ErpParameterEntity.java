package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Table(name = "erp_parameters")
@Comment("Tabla de parametros de configuracion del sistema ERP")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErpParameterEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", length = 36)
    @Comment("ID del inquilino (vacío para global)")
    private String tenantId;

    @Column(name = "param_group", nullable = false, length = 50)
    @Comment("Grupo de parametro (STORE, SYSTEM, FINANCE, etc)")
    private String paramGroup;

    @Column(name = "param_key", nullable = false, length = 100)
    @Comment("Clave unica del parametro (unicidad compuesta con tenant_id via constraint uq_erp_param_tenant_key)")
    private String paramKey;

    @Column(name = "param_value", length = 500)
    @Comment("Valor del parametro")
    private String paramValue;

    @Column(name = "param_description", length = 255)
    @Comment("Descripcion del parametro")
    private String paramDescription;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @Comment("Si el parametro esta activo")
    private Boolean isActive = true;

    @Column(name = "vigente_desde")
    @Comment("Fecha desde la que el parámetro entra en vigencia (null = siempre vigente)")
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    @Comment("Fecha hasta la que el parámetro está vigente (null = sin expiración)")
    private LocalDate vigenteHasta;

    @Column(name = "editable", nullable = false)
    @Builder.Default
    private Boolean editable = false;

    @Column(name = "tipo", nullable = false, length = 20)
    @Builder.Default
    private String tipo = "text";

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        ErpParameterEntity that = (ErpParameterEntity) o;
        return getId() != null && java.util.Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
