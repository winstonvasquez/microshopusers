package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "vendedor")
@Comment("Tabla de perfil de vendedor asociado a un usuario")
@Getter
@Setter
@ToString(callSuper = true, exclude = { "usuario" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendedorEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "vendedor_usuario_fk"))
    @Comment("Usuario asociado a este perfil de vendedor")
    private UsuarioEntity usuario;

    @Column(name = "dni_ruc", length = 20)
    @Comment("Documento de identidad o RUC del vendedor")
    private String dniRuc;

    @Column(name = "telefono_contacto", length = 20)
    @Comment("Teléfono directo de contacto del vendedor")
    private String telefonoContacto;

    @Column(name = "estado_aprobacion", nullable = false, length = 20)
    @Comment("Estado de aprobación del vendedor: PENDING, APPROVED, REJECTED")
    @Builder.Default
    private String estadoAprobacion = "PENDING";
}
