package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "company")
@Comment("Tabla de empresas (tenants)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
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
    @Comment("URL del logotipo")
    private String logoUrl;

    @Column(name = "domain", length = 100, unique = true)
    @Comment("Dominio personalizado del tenant")
    private String domain;
}
