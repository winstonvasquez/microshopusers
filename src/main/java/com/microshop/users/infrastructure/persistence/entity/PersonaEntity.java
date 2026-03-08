package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persona")
@Comment("Tabla maestra de personas naturales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PersonaEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    @Comment("Nombres completos")
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    @Comment("Apellidos completos (Paterno y Materno)")
    private String apellidos;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    @Comment("Tipo de documento (DNI, CE, PASAPORTE)")
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 15)
    @Comment("Número de documento de identidad")
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    @Comment("Fecha de nacimiento")
    private LocalDate fechaNacimiento;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    @Builder.Default
    private List<UsuarioEntity> usuarios = new ArrayList<>();
}
