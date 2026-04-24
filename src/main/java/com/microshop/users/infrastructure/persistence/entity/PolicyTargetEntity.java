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
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "policy_target")
@Comment("Define sobre qué recurso y acción actúa la política")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = { "policy" })
public class PolicyTargetEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pt_policy"))
    @Comment("Política a la que pertenece este target")
    private PolicyEntity policy;

    @Column(name = "recurso", nullable = false, length = 100)
    @Comment("Recurso sobre el que actúa (ej. VENTA, PEDIDO, USUARIO)")
    private String recurso;

    @Column(name = "accion", nullable = false, length = 50)
    @Comment("Acción permitida/denegada (ej. APROBAR, CREAR, VER)")
    private String accion;
}
