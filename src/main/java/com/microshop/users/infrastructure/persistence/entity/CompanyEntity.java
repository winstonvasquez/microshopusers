package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

// M05 — auditoria de escalada de privilegios. `AuditEntity` ya dejaba «quien toco por ultima vez y
// cuando» (@PreUpdate sobre usuario_modificacion/fecha_modificacion), pero NO el valor anterior, que es
// justo lo que importa para reconstruir un cambio de rol o una suspension de empresa. Envers es como
// este proyecto ya audita: 19 entidades en rrhh, con una unica @RevisionEntity compartida
// (RrhhRevisionEntity). Las tablas *_aud las crea usuarios/V38.
@Audited
@Entity
@Table(name = "company")
@Comment("Tabla de empresas (tenants)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = {"logoData"})
public class CompanyEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("Nombre comercial de la empresa")
    private String name;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    @Comment("RUC o identificador fiscal")
    private String ruc;

    @Column(name = "is_active", nullable = false)
    @Comment("Estado de la empresa (Activa/Inactiva)")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "legal_name", length = 200)
    @Comment("Razón social / nombre legal")
    private String legalName;

    @Column(name = "address", length = 300)
    @Comment("Dirección fiscal")
    private String address;

    @Column(name = "phone", length = 20)
    @Comment("Teléfono de contacto")
    private String phone;

    @Column(name = "email", length = 100)
    @Comment("Email corporativo")
    private String email;

    @Column(name = "logo_url", length = 500)
    @Comment("URL del logotipo (fallback externo cuando no hay binario en BD)")
    private String logoUrl;

    // ── Logotipo almacenado en BD (V36) ─────────────────────────
    // LAZY para no arrastrar el binario en cada SELECT del listado de empresas.

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "logo_data", columnDefinition = "bytea")
    @Comment("Bytes del logotipo (alternativa a logo_url)")
    // NO se audita: Envers copiaria el binario del logo en company_aud en CADA cambio de la
    // empresa. Es bloat de almacenamiento sin valor de auditoria — lo que M05 quiere rastrear es
    // quien cambio el estado o los datos fiscales, no el historial de imagenes.
    @NotAudited
    private byte[] logoData;

    @Column(name = "logo_mime", length = 50)
    @Comment("MIME del logotipo: image/jpeg, image/png, image/webp")
    private String logoMime;

    @Column(name = "logo_etag", length = 64)
    @Comment("MD5 hex del binario — usado como ETag para caché HTTP")
    private String logoEtag;

    @Column(name = "logo_size")
    @Comment("Tamaño en bytes del logotipo binario")
    private Integer logoSize;

    @Column(name = "domain", length = 100, unique = true)
    @Comment("Dominio personalizado del tenant")
    private String domain;
}
