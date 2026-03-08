package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.Instant;

@Entity
@Table(name = "policy_audit")
@Comment("Auditoría de evaluaciones de políticas PBAC (registro inmutable)")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "policy_id")
    @Comment("ID de la política evaluada (no FK para permitir eliminación de políticas)")
    private Long policyId;

    @Column(name = "usuario_id")
    @Comment("ID del usuario que solicitó la acción")
    private Long usuarioId;

    @Column(name = "company_id")
    @Comment("ID de la empresa en contexto")
    private Long companyId;

    @Column(name = "recurso", length = 100)
    @Comment("Recurso accedido (ej. VENTA, PEDIDO)")
    private String recurso;

    @Column(name = "accion", length = 50)
    @Comment("Acción solicitada (ej. APROBAR, CREAR)")
    private String accion;

    @Column(name = "resultado", nullable = false, length = 10)
    @Comment("Resultado de la evaluación: ALLOW o DENY")
    private String resultado;

    @Column(name = "fecha", nullable = false)
    @Comment("Fecha y hora de la evaluación")
    @Builder.Default
    private Instant fecha = Instant.now();
}
