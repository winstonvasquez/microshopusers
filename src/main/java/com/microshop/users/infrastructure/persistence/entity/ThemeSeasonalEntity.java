package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para temas estacionales de la tienda pública.
 * Permite activar temas según rangos de fecha (ej: Navidad, Black Friday, Verano).
 */
@Entity
@Table(name = "theme_seasonal")
@Comment("Temas estacionales con rango de fechas para la tienda pública")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeSeasonalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "theme_key", nullable = false, length = 50)
    @Comment("Clave del tema CSS (ej: christmas, black-friday, summer)")
    private String themeKey;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("Nombre descriptivo del tema estacional")
    private String name;

    @Column(name = "start_date", nullable = false)
    @Comment("Fecha de inicio de vigencia del tema")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @Comment("Fecha de fin de vigencia del tema")
    private LocalDate endDate;

    @Column(name = "tenant_id", length = 20)
    @Comment("ID del tenant (null = global para todos)")
    private String tenantId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    @Comment("Si el tema estacional está activo")
    private Boolean active = true;

    @Column(name = "created_at")
    @Comment("Fecha de creación del registro")
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
