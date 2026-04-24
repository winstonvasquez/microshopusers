package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "segmento")
@Comment("Segmentos de clasificación de clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentoEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @Comment("Nombre del segmento")
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    @Comment("Descripción opcional del segmento")
    private String descripcion;

    @Column(name = "color", nullable = false, length = 20)
    @Comment("Color hexadecimal para representación visual")
    private String color;

    @Column(name = "tipo_cliente", nullable = false, length = 20)
    @Comment("Tipo de cliente: VIP, REGULAR, OCASIONAL, MAYORISTA")
    private String tipoCliente;

    @Column(name = "total_clientes", nullable = false)
    @Builder.Default
    @Comment("Cantidad de clientes asignados a este segmento")
    private Integer totalClientes = 0;

    @Column(name = "company_id")
    @Comment("ID de empresa para multi-tenancy (null = visible para todas)")
    private Long companyId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        SegmentoEntity that = (SegmentoEntity) o;
        return getId() != null && java.util.Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
