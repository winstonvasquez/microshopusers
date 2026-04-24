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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbshoprrhh", name = "employee", indexes = {
    @Index(name = "idx_employee_tenant", columnList = "tenant_id"),
    @Index(name = "idx_employee_codigo", columnList = "tenant_id,codigo_empleado"),
    @Index(name = "idx_employee_department", columnList = "department_id"),
    @Index(name = "idx_employee_position", columnList = "position_id"),
    @Index(name = "idx_employee_supervisor", columnList = "supervisor_id")
})
@Comment("Tabla de empleados")
@Getter
@Setter
@ToString(exclude = {"department", "position", "supervisor", "subordinates", "contracts", "salaries", "leaveBalances", "emergencyContacts", "dependents", "documents", "goals"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK autogenerada")
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Comment("ID del tenant (companyId)")
    private Long tenantId;

    @Column(name = "codigo_empleado", nullable = false, length = 20)
    @Comment("Código único del empleado")
    private String codigoEmpleado;

    @Column(name = "nombres", nullable = false, length = 100)
    @Comment("Nombres del empleado")
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    @Comment("Apellidos del empleado")
    private String apellidos;

    @Column(name = "tipo_documento", length = 20)
    @Comment("Tipo de documento (DNI, CE, PASAPORTE)")
    @Builder.Default
    private String tipoDocumento = "DNI";

    @Column(name = "documento_identidad", nullable = false, length = 20)
    @Comment("DNI o documento de identidad")
    private String documentoIdentidad;

    @Column(name = "fecha_nacimiento")
    @Comment("Fecha de nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    @Comment("Género del empleado")
    private Gender genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    @Comment("Estado civil")
    private MaritalStatus estadoCivil;

    @Column(name = "nacionalidad", length = 50)
    @Comment("Nacionalidad")
    private String nacionalidad;

    @Column(name = "tipo_sangre", length = 5)
    @Comment("Tipo de sangre")
    private String tipoSangre;

    @Column(name = "fecha_ingreso", nullable = false)
    @Comment("Fecha de ingreso a la empresa")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    @Comment("Fecha de salida de la empresa")
    private LocalDate fechaSalida;

    @Column(name = "motivo_salida", length = 500)
    @Comment("Motivo de salida")
    private String motivoSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @Comment("Departamento al que pertenece")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @Comment("Puesto/Cargo del empleado")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    @Comment("Supervisor directo")
    private Employee supervisor;

    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Employee> subordinates = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Salary> salaries = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LeaveBalance> leaveBalances = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Dependent> dependents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();

    @Column(name = "cargo", length = 100)
    @Comment("Cargo o puesto del empleado (legacy)")
    @Deprecated
    private String cargo;

    @Column(name = "area", length = 100)
    @Comment("Área o departamento (legacy)")
    @Deprecated
    private String area;

    @Column(name = "email", length = 100)
    @Comment("Email corporativo")
    private String email;

    @Column(name = "telefono", length = 20)
    @Comment("Teléfono de contacto")
    private String telefono;

    @Column(name = "direccion", length = 500)
    @Comment("Dirección de residencia")
    private String direccion;

    @Column(name = "distrito", length = 100)
    @Comment("Distrito")
    private String distrito;

    @Column(name = "provincia", length = 100)
    @Comment("Provincia")
    private String provincia;

    @Column(name = "departamento_geo", length = 100)
    @Comment("Departamento geográfico")
    private String departamentoGeo;

    @Column(name = "foto_url", length = 500)
    @Comment("URL de la foto del empleado")
    private String fotoUrl;

    @Column(name = "linkedin_url", length = 500)
    @Comment("URL del perfil de LinkedIn")
    private String linkedinUrl;

    @Column(name = "nivel_educacion", length = 50)
    @Comment("Nivel de educación")
    private String nivelEducacion;

    @Column(name = "profesion", length = 100)
    @Comment("Profesión")
    private String profesion;

    @Column(name = "universidad", length = 200)
    @Comment("Universidad")
    private String universidad;

    @Column(name = "sistema_previsional", length = 10)
    @Comment("Sistema previsional (ONP, AFP)")
    @Builder.Default
    private String sistemaPrevisional = "ONP";

    @Column(name = "afp_nombre", length = 20)
    @Comment("Nombre de la AFP (si aplica)")
    private String afpNombre;

    @Column(name = "store_id")
    @Comment("ID de la tienda asignada")
    private Long storeId;

    @Column(name = "user_id")
    @Comment("ID del usuario vinculado (microshopusers)")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Comment("Estado del empleado")
    @Builder.Default
    private EmployeeStatus estado = EmployeeStatus.ACTIVO;

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
    }

    public enum EmployeeStatus {
        ACTIVO,
        INACTIVO,
        SUSPENDIDO,
        CESADO
    }

    public enum Gender {
        MASCULINO,
        FEMENINO,
        OTRO
    }

    public enum MaritalStatus {
        SOLTERO,
        CASADO,
        DIVORCIADO,
        VIUDO,
        CONVIVIENTE
    }
}
