package com.microshop.rrhh.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbshoprrhh", name = "document", indexes = {
    @Index(name = "idx_document_tenant", columnList = "tenant_id"),
    @Index(name = "idx_document_employee", columnList = "employee_id"),
    @Index(name = "idx_document_tipo", columnList = "tenant_id,tipo_documento")
})
@Comment("Tabla de documentos del empleado")
@Getter
@Setter
@ToString(exclude = {"employee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @Comment("Empleado")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 50)
    @Comment("Tipo de documento")
    private DocumentType tipoDocumento;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    @Comment("Nombre del archivo")
    private String nombreArchivo;

    @Column(name = "descripcion", length = 500)
    @Comment("Descripción del documento")
    private String descripcion;

    @Column(name = "url_archivo", nullable = false, length = 500)
    @Comment("URL del archivo")
    private String urlArchivo;

    @Column(name = "fecha_emision")
    @Comment("Fecha de emisión del documento")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    @Comment("Fecha de vencimiento del documento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Comment("Estado del documento")
    @Builder.Default
    private DocumentStatus estado = DocumentStatus.VIGENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Comment("Fecha de última actualización")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Actualizar estado según fecha de vencimiento
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) {
            estado = DocumentStatus.VENCIDO;
        }
    }

    public enum DocumentType {
        CV,
        CONTRATO,
        CERTIFICADO_TRABAJO,
        CERTIFICADO_ESTUDIOS,
        ANTECEDENTES_PENALES,
        ANTECEDENTES_POLICIALES,
        CERTIFICADO_SALUD,
        LICENCIA_CONDUCIR,
        CARTA_RECOMENDACION,
        TITULO_PROFESIONAL,
        GRADO_ACADEMICO,
        CERTIFICACION_TECNICA,
        OTRO
    }

    public enum DocumentStatus {
        VIGENTE,
        VENCIDO,
        RENOVADO,
        ANULADO
    }
}
