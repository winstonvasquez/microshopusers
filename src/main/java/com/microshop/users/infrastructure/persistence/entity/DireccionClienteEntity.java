package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * Entidad JPA para las direcciones de envío del cliente en la tienda online.
 * Cada usuario puede tener múltiples direcciones; una marcada como principal.
 */
@Entity
@Table(name = "cliente_direccion")
@Comment("Direcciones de envío registradas por clientes de la tienda")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionClienteEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("ID del usuario propietario de la dirección")
    private Long userId;

    @Column(name = "nombre_completo", length = 100)
    @Comment("Nombre completo del destinatario")
    private String nombreCompleto;

    @Column(name = "telefono", length = 15)
    @Comment("Teléfono de contacto del destinatario")
    private String telefono;

    @Column(name = "departamento", length = 50)
    @Comment("Departamento (región) del Perú")
    private String departamento;

    @Column(name = "provincia", length = 50)
    @Comment("Provincia")
    private String provincia;

    @Column(name = "distrito", length = 50)
    @Comment("Distrito")
    private String distrito;

    @Column(name = "direccion_linea1", length = 200)
    @Comment("Dirección: calle, número, urbanización")
    private String direccionLinea1;

    @Column(name = "referencia", length = 200)
    @Comment("Referencia adicional para facilitar la entrega")
    private String referencia;

    @Column(name = "es_principal", nullable = false)
    @Comment("Indica si es la dirección predeterminada del usuario")
    @Builder.Default
    private boolean esPrincipal = false;
}
