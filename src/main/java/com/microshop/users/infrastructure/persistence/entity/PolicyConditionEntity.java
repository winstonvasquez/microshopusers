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
@Table(name = "policy_condition")
@Comment("Condiciones evaluables para ABAC (Attribute-Based Access Control)")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PolicyConditionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pc_policy"))
    @Comment("Política a la que pertenece esta condición")
    private PolicyEntity policy;

    @Column(name = "left_operand", nullable = false, length = 150)
    @Comment("Operando izquierdo (ej. user.company_id)")
    private String leftOperand;

    @Column(name = "operador", nullable = false, length = 10)
    @Comment("Operador de comparación (=, <=, >=, IN, BETWEEN)")
    private String operador;

    @Column(name = "right_operand", nullable = false, length = 150)
    @Comment("Operando derecho (ej. resource.company_id)")
    private String rightOperand;

    @Column(name = "tipo", nullable = false, length = 20)
    @Comment("Tipo de condición: USER, RESOURCE, CONTEXT, LITERAL")
    private String tipo;
}
