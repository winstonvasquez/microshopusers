-- Tabla de empleados
CREATE TABLE IF NOT EXISTS employee (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    codigo_empleado VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    cargo VARCHAR(100),
    area VARCHAR(100),
    email VARCHAR(100),
    telefono VARCHAR(20),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at DATE NOT NULL,
    updated_at DATE,
    CONSTRAINT uk_employee_tenant_codigo UNIQUE (tenant_id, codigo_empleado),
    CONSTRAINT uk_employee_tenant_documento UNIQUE (tenant_id, documento_identidad)
);

CREATE INDEX IF NOT EXISTS idx_employee_tenant ON employee(tenant_id);
CREATE INDEX IF NOT EXISTS idx_employee_codigo ON employee(tenant_id, codigo_empleado);
CREATE INDEX IF NOT EXISTS idx_employee_estado ON employee(tenant_id, estado);

COMMENT ON TABLE employee IS 'Tabla de empleados';
COMMENT ON COLUMN employee.tenant_id IS 'ID del tenant (companyId)';
COMMENT ON COLUMN employee.codigo_empleado IS 'Código único del empleado';

-- Tabla de asistencia
CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_entrada TIME,
    hora_salida TIME,
    tipo_registro VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    observaciones VARCHAR(500),
    created_at DATE NOT NULL,
    CONSTRAINT uk_attendance_tenant_employee_fecha UNIQUE (tenant_id, employee_id, fecha)
);

CREATE INDEX IF NOT EXISTS idx_attendance_tenant ON attendance(tenant_id);
CREATE INDEX IF NOT EXISTS idx_attendance_employee_fecha ON attendance(tenant_id, employee_id, fecha);

COMMENT ON TABLE attendance IS 'Tabla de asistencia de empleados';

-- Tabla de solicitudes de vacaciones
CREATE TABLE IF NOT EXISTS vacation_request (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    dias INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO',
    motivo VARCHAR(500),
    aprobado_por BIGINT,
    fecha_aprobacion DATE,
    comentarios_aprobacion VARCHAR(500),
    created_at DATE NOT NULL,
    updated_at DATE
);

CREATE INDEX IF NOT EXISTS idx_vacation_tenant ON vacation_request(tenant_id);
CREATE INDEX IF NOT EXISTS idx_vacation_employee ON vacation_request(tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_vacation_estado ON vacation_request(tenant_id, estado);

COMMENT ON TABLE vacation_request IS 'Tabla de solicitudes de vacaciones';

-- Tabla de planillas
CREATE TABLE IF NOT EXISTS payroll (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    periodo VARCHAR(7) NOT NULL,
    sueldo_base DECIMAL(10,2) NOT NULL,
    bonos DECIMAL(10,2) DEFAULT 0,
    descuentos DECIMAL(10,2) DEFAULT 0,
    neto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'GENERADO',
    fecha_pago DATE,
    pago_id BIGINT,
    created_at DATE NOT NULL,
    updated_at DATE,
    CONSTRAINT uk_payroll_tenant_employee_periodo UNIQUE (tenant_id, employee_id, periodo)
);

CREATE INDEX IF NOT EXISTS idx_payroll_tenant ON payroll(tenant_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employee_periodo ON payroll(tenant_id, employee_id, periodo);

COMMENT ON TABLE payroll IS 'Tabla de planillas de pago';

-- Tabla de evaluaciones de desempeño
CREATE TABLE IF NOT EXISTS performance_evaluation (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    periodo VARCHAR(7) NOT NULL,
    evaluador_id BIGINT NOT NULL,
    puntaje DECIMAL(5,2) NOT NULL,
    comentarios VARCHAR(2000),
    fortalezas VARCHAR(1000),
    areas_mejora VARCHAR(1000),
    fecha_evaluacion DATE NOT NULL,
    created_at DATE NOT NULL,
    updated_at DATE
);

CREATE INDEX IF NOT EXISTS idx_evaluation_tenant ON performance_evaluation(tenant_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_employee ON performance_evaluation(tenant_id, employee_id);

COMMENT ON TABLE performance_evaluation IS 'Tabla de evaluaciones de desempeño';

-- Tabla de cursos de capacitación
CREATE TABLE IF NOT EXISTS training (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    instructor VARCHAR(200),
    duracion_horas INTEGER,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    created_at DATE NOT NULL,
    updated_at DATE
);

CREATE INDEX IF NOT EXISTS idx_training_tenant ON training(tenant_id);

COMMENT ON TABLE training IS 'Tabla de cursos de capacitación';

-- Tabla de participación en capacitaciones
CREATE TABLE IF NOT EXISTS training_participation (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    training_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'INSCRITO',
    asistencia_porcentaje DECIMAL(5,2),
    nota_final DECIMAL(5,2),
    aprobado BOOLEAN,
    certificado_emitido BOOLEAN DEFAULT FALSE,
    comentarios VARCHAR(500),
    created_at DATE NOT NULL,
    updated_at DATE,
    CONSTRAINT uk_training_part_tenant_training_employee UNIQUE (tenant_id, training_id, employee_id)
);

CREATE INDEX IF NOT EXISTS idx_training_part_tenant ON training_participation(tenant_id);
CREATE INDEX IF NOT EXISTS idx_training_part_training ON training_participation(tenant_id, training_id);
CREATE INDEX IF NOT EXISTS idx_training_part_employee ON training_participation(tenant_id, employee_id);

COMMENT ON TABLE training_participation IS 'Tabla de participación en capacitaciones';
