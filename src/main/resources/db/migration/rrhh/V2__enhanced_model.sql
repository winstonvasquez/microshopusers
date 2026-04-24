-- Migración V2: Modelo Robusto con Nuevas Entidades y Relaciones
-- Fecha: 2024-03-05
-- Descripción: Agrega nuevas entidades (Department, Position, Contract, etc.) y actualiza entidades existentes con relaciones FK

-- ============================================
-- PARTE 1: CREAR NUEVAS TABLAS
-- ============================================

-- Tabla de departamentos
CREATE TABLE IF NOT EXISTS department (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    manager_id BIGINT,
    parent_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_department_tenant_codigo UNIQUE (tenant_id, codigo),
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES department(id)
);

CREATE INDEX IF NOT EXISTS idx_department_tenant ON department(tenant_id);
CREATE INDEX IF NOT EXISTS idx_department_manager ON department(manager_id);
CREATE INDEX IF NOT EXISTS idx_department_parent ON department(parent_id);

COMMENT ON TABLE department IS 'Tabla de departamentos/áreas';
COMMENT ON COLUMN department.manager_id IS 'Gerente/Jefe del departamento (FK a employee)';
COMMENT ON COLUMN department.parent_id IS 'Departamento padre para jerarquía';

-- Tabla de puestos/cargos
CREATE TABLE IF NOT EXISTS position (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000),
    department_id BIGINT NOT NULL,
    nivel VARCHAR(50),
    salario_minimo DECIMAL(10,2),
    salario_maximo DECIMAL(10,2),
    requisitos TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_position_tenant_codigo UNIQUE (tenant_id, codigo),
    CONSTRAINT fk_position_department FOREIGN KEY (department_id) REFERENCES department(id)
);

CREATE INDEX IF NOT EXISTS idx_position_tenant ON position(tenant_id);
CREATE INDEX IF NOT EXISTS idx_position_codigo ON position(tenant_id, codigo);
CREATE INDEX IF NOT EXISTS idx_position_department ON position(department_id);

COMMENT ON TABLE position IS 'Tabla de puestos/cargos';

-- Tabla de contratos laborales
CREATE TABLE IF NOT EXISTS contract (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    tipo_contrato VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    salario_base DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'PEN',
    jornada_laboral VARCHAR(50),
    horas_semanales INTEGER,
    periodo_prueba_meses INTEGER,
    documento_contrato_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    motivo_fin VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_contract_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_contract_tenant ON contract(tenant_id);
CREATE INDEX IF NOT EXISTS idx_contract_employee ON contract(employee_id);
CREATE INDEX IF NOT EXISTS idx_contract_estado ON contract(tenant_id, estado);

COMMENT ON TABLE contract IS 'Tabla de contratos laborales';

-- Tabla de historial salarial
CREATE TABLE IF NOT EXISTS salary (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    salario_base DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'PEN',
    motivo VARCHAR(100),
    porcentaje_incremento DECIMAL(5,2),
    aprobado_por BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT fk_salary_aprobado_por FOREIGN KEY (aprobado_por) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_salary_tenant ON salary(tenant_id);
CREATE INDEX IF NOT EXISTS idx_salary_employee ON salary(employee_id);
CREATE INDEX IF NOT EXISTS idx_salary_fecha ON salary(tenant_id, employee_id, fecha_inicio);

COMMENT ON TABLE salary IS 'Tabla de historial salarial';

-- Tabla de balance de vacaciones
CREATE TABLE IF NOT EXISTS leave_balance (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    anio INTEGER NOT NULL,
    dias_ganados DECIMAL(5,2) NOT NULL,
    dias_usados DECIMAL(5,2) DEFAULT 0,
    dias_disponibles DECIMAL(5,2) NOT NULL,
    dias_vencidos DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_leave_balance_tenant_employee_anio UNIQUE (tenant_id, employee_id, anio),
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_leave_balance_tenant ON leave_balance(tenant_id);
CREATE INDEX IF NOT EXISTS idx_leave_balance_employee ON leave_balance(employee_id);

COMMENT ON TABLE leave_balance IS 'Tabla de balance de vacaciones';

-- Tabla de contactos de emergencia
CREATE TABLE IF NOT EXISTS emergency_contact (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    relacion VARCHAR(50) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    telefono_alternativo VARCHAR(20),
    direccion VARCHAR(500),
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_emergency_contact_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_emergency_contact_tenant ON emergency_contact(tenant_id);
CREATE INDEX IF NOT EXISTS idx_emergency_contact_employee ON emergency_contact(employee_id);

COMMENT ON TABLE emergency_contact IS 'Tabla de contactos de emergencia';

-- Tabla de dependientes/familiares
CREATE TABLE IF NOT EXISTS dependent (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    relacion VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    documento_identidad VARCHAR(20),
    genero VARCHAR(20),
    es_beneficiario_seguro BOOLEAN NOT NULL DEFAULT FALSE,
    es_carga_familiar BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_dependent_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_dependent_tenant ON dependent(tenant_id);
CREATE INDEX IF NOT EXISTS idx_dependent_employee ON dependent(employee_id);

COMMENT ON TABLE dependent IS 'Tabla de dependientes/familiares';

-- Tabla de documentos del empleado
CREATE TABLE IF NOT EXISTS document (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    nombre_archivo VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    url_archivo VARCHAR(500) NOT NULL,
    fecha_emision DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(20) DEFAULT 'VIGENTE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_document_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_document_tenant ON document(tenant_id);
CREATE INDEX IF NOT EXISTS idx_document_employee ON document(employee_id);
CREATE INDEX IF NOT EXISTS idx_document_tipo ON document(tenant_id, tipo_documento);

COMMENT ON TABLE document IS 'Tabla de documentos del empleado';

-- Tabla de detalle de conceptos de planilla
CREATE TABLE IF NOT EXISTS payroll_detail (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    payroll_id BIGINT NOT NULL,
    concepto VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    cantidad DECIMAL(10,2),
    tasa DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payroll_detail_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_detail_tenant ON payroll_detail(tenant_id);
CREATE INDEX IF NOT EXISTS idx_payroll_detail_payroll ON payroll_detail(payroll_id);

COMMENT ON TABLE payroll_detail IS 'Tabla de detalle de conceptos de planilla';

-- Tabla de criterios de evaluación
CREATE TABLE IF NOT EXISTS evaluation_criteria (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    peso_porcentaje DECIMAL(5,2) NOT NULL,
    puntaje_minimo DECIMAL(5,2) DEFAULT 0,
    puntaje_maximo DECIMAL(5,2) DEFAULT 100,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evaluation_criteria_tenant ON evaluation_criteria(tenant_id);

COMMENT ON TABLE evaluation_criteria IS 'Tabla de criterios de evaluación';

-- Tabla de detalle de evaluación por criterio
CREATE TABLE IF NOT EXISTS evaluation_detail (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    evaluation_id BIGINT NOT NULL,
    criteria_id BIGINT NOT NULL,
    puntaje DECIMAL(5,2) NOT NULL,
    comentarios VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_evaluation_detail_evaluation FOREIGN KEY (evaluation_id) REFERENCES performance_evaluation(id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_detail_criteria FOREIGN KEY (criteria_id) REFERENCES evaluation_criteria(id)
);

CREATE INDEX IF NOT EXISTS idx_evaluation_detail_tenant ON evaluation_detail(tenant_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_detail_evaluation ON evaluation_detail(evaluation_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_detail_criteria ON evaluation_detail(criteria_id);

COMMENT ON TABLE evaluation_detail IS 'Tabla de detalle de evaluación por criterio';

-- Tabla de metas/objetivos
CREATE TABLE IF NOT EXISTS goal (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO',
    porcentaje_avance DECIMAL(5,2) DEFAULT 0,
    prioridad VARCHAR(20),
    asignado_por BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_goal_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT fk_goal_asignado_por FOREIGN KEY (asignado_por) REFERENCES employee(id)
);

CREATE INDEX IF NOT EXISTS idx_goal_tenant ON goal(tenant_id);
CREATE INDEX IF NOT EXISTS idx_goal_employee ON goal(employee_id);
CREATE INDEX IF NOT EXISTS idx_goal_estado ON goal(tenant_id, estado);

COMMENT ON TABLE goal IS 'Tabla de metas/objetivos';

-- ============================================
-- PARTE 2: ACTUALIZAR TABLA EMPLOYEE
-- ============================================

-- Agregar nuevos campos a employee
ALTER TABLE employee ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(20) DEFAULT 'DNI';
ALTER TABLE employee ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS genero VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS estado_civil VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS nacionalidad VARCHAR(50);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS tipo_sangre VARCHAR(5);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS fecha_salida DATE;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS motivo_salida VARCHAR(500);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS position_id BIGINT;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS supervisor_id BIGINT;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS direccion VARCHAR(500);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS distrito VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS provincia VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS departamento_geo VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(500);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS nivel_educacion VARCHAR(50);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS profesion VARCHAR(100);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS universidad VARCHAR(200);

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE employee ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE employee ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- Agregar foreign keys a employee
ALTER TABLE employee ADD CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department(id);
ALTER TABLE employee ADD CONSTRAINT fk_employee_position FOREIGN KEY (position_id) REFERENCES position(id);
ALTER TABLE employee ADD CONSTRAINT fk_employee_supervisor FOREIGN KEY (supervisor_id) REFERENCES employee(id);

-- Agregar índices
CREATE INDEX IF NOT EXISTS idx_employee_department ON employee(department_id);
CREATE INDEX IF NOT EXISTS idx_employee_position ON employee(position_id);
CREATE INDEX IF NOT EXISTS idx_employee_supervisor ON employee(supervisor_id);

-- Agregar FK de department.manager_id a employee
ALTER TABLE department ADD CONSTRAINT fk_department_manager FOREIGN KEY (manager_id) REFERENCES employee(id);

-- ============================================
-- PARTE 3: ACTUALIZAR TABLA ATTENDANCE
-- ============================================

ALTER TABLE attendance ADD COLUMN IF NOT EXISTS horas_trabajadas DECIMAL(5,2);
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS horas_extras DECIMAL(5,2) DEFAULT 0;
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS justificacion VARCHAR(500);
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS aprobado_por BIGINT;
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS fecha_aprobacion TIMESTAMP;
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS ubicacion_entrada VARCHAR(200);
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS ubicacion_salida VARCHAR(200);

-- Cambiar tipo de created_at a TIMESTAMP
ALTER TABLE attendance ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;

-- Agregar FK
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employee(id);
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_aprobado_por FOREIGN KEY (aprobado_por) REFERENCES employee(id);

-- ============================================
-- PARTE 4: ACTUALIZAR TABLA VACATION_REQUEST
-- ============================================

ALTER TABLE vacation_request ADD COLUMN IF NOT EXISTS tipo_vacacion VARCHAR(50) DEFAULT 'ANUAL';
ALTER TABLE vacation_request ADD COLUMN IF NOT EXISTS balance_usado DECIMAL(5,2);
ALTER TABLE vacation_request ADD COLUMN IF NOT EXISTS reemplazo_id BIGINT;
ALTER TABLE vacation_request ADD COLUMN IF NOT EXISTS documento_url VARCHAR(500);

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE vacation_request ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE vacation_request ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- Agregar FK
ALTER TABLE vacation_request ADD CONSTRAINT fk_vacation_employee FOREIGN KEY (employee_id) REFERENCES employee(id);
ALTER TABLE vacation_request ADD CONSTRAINT fk_vacation_aprobado_por FOREIGN KEY (aprobado_por) REFERENCES employee(id);
ALTER TABLE vacation_request ADD CONSTRAINT fk_vacation_reemplazo FOREIGN KEY (reemplazo_id) REFERENCES employee(id);

-- ============================================
-- PARTE 5: ACTUALIZAR TABLA PAYROLL
-- ============================================

ALTER TABLE payroll ADD COLUMN IF NOT EXISTS afp_onp VARCHAR(50);
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS monto_afp_onp DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS essalud DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS renta_quinta DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS cts DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS gratificacion DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS asignacion_familiar DECIMAL(10,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS dias_trabajados INTEGER;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS horas_extras DECIMAL(5,2) DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS monto_horas_extras DECIMAL(10,2) DEFAULT 0;

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE payroll ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE payroll ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- Agregar FK
ALTER TABLE payroll ADD CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employee(id);

-- ============================================
-- PARTE 6: ACTUALIZAR TABLA PERFORMANCE_EVALUATION
-- ============================================

ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS tipo_evaluacion VARCHAR(50) DEFAULT 'ANUAL';
ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'BORRADOR';
ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS plan_mejora TEXT;
ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS proxima_revision DATE;

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE performance_evaluation ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE performance_evaluation ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- Agregar FK
ALTER TABLE performance_evaluation ADD CONSTRAINT fk_evaluation_employee FOREIGN KEY (employee_id) REFERENCES employee(id);
ALTER TABLE performance_evaluation ADD CONSTRAINT fk_evaluation_evaluador FOREIGN KEY (evaluador_id) REFERENCES employee(id);

-- ============================================
-- PARTE 7: ACTUALIZAR TABLA TRAINING
-- ============================================

ALTER TABLE training ADD COLUMN IF NOT EXISTS categoria VARCHAR(50);
ALTER TABLE training ADD COLUMN IF NOT EXISTS proveedor VARCHAR(200);
ALTER TABLE training ADD COLUMN IF NOT EXISTS costo DECIMAL(10,2);
ALTER TABLE training ADD COLUMN IF NOT EXISTS moneda VARCHAR(3) DEFAULT 'PEN';
ALTER TABLE training ADD COLUMN IF NOT EXISTS modalidad VARCHAR(50);
ALTER TABLE training ADD COLUMN IF NOT EXISTS ubicacion VARCHAR(200);
ALTER TABLE training ADD COLUMN IF NOT EXISTS cupo_maximo INTEGER;
ALTER TABLE training ADD COLUMN IF NOT EXISTS requiere_certificacion BOOLEAN DEFAULT FALSE;
ALTER TABLE training ADD COLUMN IF NOT EXISTS url_material VARCHAR(500);

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE training ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE training ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- ============================================
-- PARTE 8: ACTUALIZAR TABLA TRAINING_PARTICIPATION
-- ============================================

ALTER TABLE training_participation ADD COLUMN IF NOT EXISTS feedback TEXT;
ALTER TABLE training_participation ADD COLUMN IF NOT EXISTS calificacion_curso DECIMAL(3,1);
ALTER TABLE training_participation ADD COLUMN IF NOT EXISTS fecha_certificacion DATE;
ALTER TABLE training_participation ADD COLUMN IF NOT EXISTS url_certificado VARCHAR(500);

-- Cambiar tipo de created_at y updated_at a TIMESTAMP
ALTER TABLE training_participation ALTER COLUMN created_at TYPE TIMESTAMP USING created_at::TIMESTAMP;
ALTER TABLE training_participation ALTER COLUMN updated_at TYPE TIMESTAMP USING updated_at::TIMESTAMP;

-- Agregar FK
ALTER TABLE training_participation ADD CONSTRAINT fk_training_part_training FOREIGN KEY (training_id) REFERENCES training(id);
ALTER TABLE training_participation ADD CONSTRAINT fk_training_part_employee FOREIGN KEY (employee_id) REFERENCES employee(id);

-- ============================================
-- COMENTARIOS FINALES
-- ============================================

COMMENT ON COLUMN employee.cargo IS 'Cargo o puesto del empleado (DEPRECATED - usar position_id)';
COMMENT ON COLUMN employee.area IS 'Área o departamento (DEPRECATED - usar department_id)';
