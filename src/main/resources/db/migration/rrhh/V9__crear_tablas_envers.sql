-- V9__crear_tablas_envers.sql
--
-- Las 20 tablas de auditoría de Envers de rrhh (`revinfo` + 19 `*_aud`) EXISTEN en la base de
-- desarrollo, pero NINGUNA migración las crea: las creó `ddl-auto` en su día, antes de que la
-- propiedad quedara fijada en `none`. Verificado con
-- `grep -ril "revinfo\|_aud" db/migration/rrhh/` → 0 resultados en V1..V8.
--
-- CONSECUENCIA EN UN ENTORNO NUEVO, que es peor que un fallo de arranque: Flyway termina con
-- ÉXITO y el servicio arranca sin queja, porque ninguna migración depende de estas tablas. El
-- fallo aparece más tarde, en el primer INSERT/UPDATE/DELETE sobre CUALQUIER entidad `@Audited`
-- —dar de alta un empleado, registrar una asistencia, aprobar unas vacaciones—, con
-- `relation "dbshoprrhh.employee_aud" does not exist`. Las 19 entidades auditadas están bajo
-- `rrhh/domain/model` (son las únicas 19 `@Audited` de todo el monorepo).
--
-- Por eso el auditor de migraciones no lo detectaba y el backlog daba rrhh por autoconsistente:
-- ningún heurístico basado en texto SQL puede verlo, porque no hay NINGUNA sentencia que
-- mencione estas tablas. Sólo se ve cruzando las `@Audited` contra el DDL.
--
-- PRECEDENTE QUE ESTE SERVICIO NO SIGUIÓ: microshoplogistica sí crea sus tablas Envers
-- explícitamente en `logistica/V1__create_logistics_schema.sql`.
--
-- POR QUÉ SE PUEDE AÑADIR AL FINAL (V9) y no hace falta una versión temprana: a diferencia de
-- los otros servicios con el mismo problema de despliegue-desde-cero (ventas, compras,
-- usuarios), aquí NINGUNA migración anterior referencia estas tablas, así que crearlas al final
-- de la secuencia es suficiente — el orden sólo importa frente a `ddl-auto`, que está en `none`,
-- y frente al primer uso en runtime, que ocurre después de que Flyway termine. rrhh es el único
-- de los cuatro donde la reparación real es viable sin tocar `outOfOrder`.
--
-- EL DDL NO ESTÁ ESCRITO A MANO: se volcó del schema vivo con `pg_dump --schema-only`, que es la
-- referencia de lo que Hibernate Envers espera encontrar, y se hizo idempotente. En este entorno
-- la migración es un no-op (las tablas ya están); su valor es que un entorno NUEVO quede igual.
--
-- `revinfo_seq` va incluida y con `INCREMENT BY 50` a propósito: tiene que coincidir con el
-- `allocationSize = 50` de `RrhhRevisionEntity:41`. Si la secuencia se creara con el incremento
-- por defecto (1), Hibernate reservaría bloques de 50 sobre una secuencia que avanza de 1 en 1 y
-- reutilizaría números de revisión ya usados, violando la PK compuesta (rev, id) de cada `_aud`.
--
-- VERIFICADA POR LOS DOS CAMINOS, y hacía falta: la primera versión de esta migración envolvía
-- cada constraint en `EXCEPTION WHEN duplicate_table OR duplicate_object`, funcionaba en un
-- schema vacío y REVENTÓ contra el schema real —añadir un PRIMARY KEY a una tabla que ya lo tiene
-- lanza `invalid_table_definition` («multiple primary keys for table are not allowed»), no un
-- error de duplicado—. Flyway abortó el arranque y revirtió limpio. Por eso los constraints
-- comprueban existencia en `pg_constraint` en vez de confiar en un SQLSTATE adivinado.


-- Secuencia de números de revisión (allocationSize=50 en RrhhRevisionEntity)
CREATE SEQUENCE IF NOT EXISTS dbshoprrhh.revinfo_seq INCREMENT BY 50 START WITH 1 MINVALUE 1;


-- Tablas ------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS dbshoprrhh.revinfo (
    rev integer NOT NULL,
    revtstmp bigint
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.attendance_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    fecha date,
    fecha_aprobacion timestamp(6) without time zone,
    hora_entrada time(6) without time zone,
    hora_salida time(6) without time zone,
    horas_extras numeric(5,2),
    horas_trabajadas numeric(5,2),
    justificacion character varying(500),
    observaciones character varying(500),
    tenant_id bigint,
    tipo_registro character varying(20),
    ubicacion_entrada character varying(200),
    ubicacion_salida character varying(200),
    aprobado_por bigint,
    employee_id bigint,
    CONSTRAINT attendance_aud_tipo_registro_check CHECK (((tipo_registro)::text = ANY ((ARRAY['NORMAL'::character varying, 'TARDANZA'::character varying, 'FALTA'::character varying, 'PERMISO'::character varying, 'LICENCIA'::character varying, 'VACACIONES'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.contract_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    documento_contrato_url character varying(500),
    estado character varying(20),
    fecha_fin date,
    fecha_inicio date,
    horas_semanales integer,
    jornada_laboral character varying(50),
    moneda character varying(3),
    motivo_fin character varying(500),
    periodo_prueba_meses integer,
    salario_base numeric(10,2),
    tenant_id bigint,
    tipo_contrato character varying(50),
    updated_at timestamp(6) without time zone,
    employee_id bigint,
    CONSTRAINT contract_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'FINALIZADO'::character varying, 'SUSPENDIDO'::character varying, 'RENOVADO'::character varying])::text[]))),
    CONSTRAINT contract_aud_jornada_laboral_check CHECK (((jornada_laboral)::text = ANY ((ARRAY['COMPLETA'::character varying, 'PARCIAL'::character varying, 'REDUCIDA'::character varying])::text[]))),
    CONSTRAINT contract_aud_tipo_contrato_check CHECK (((tipo_contrato)::text = ANY ((ARRAY['INDEFINIDO'::character varying, 'PLAZO_FIJO'::character varying, 'TEMPORAL'::character varying, 'PRACTICAS'::character varying, 'LOCACION_SERVICIOS'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.department_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    activo boolean,
    codigo character varying(20),
    created_at timestamp(6) without time zone,
    descripcion character varying(500),
    nombre character varying(100),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    manager_id bigint,
    parent_id bigint
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.dependent_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    documento_identidad character varying(20),
    es_beneficiario_seguro boolean,
    es_carga_familiar boolean,
    fecha_nacimiento date,
    genero character varying(20),
    nombre_completo character varying(200),
    relacion character varying(50),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    employee_id bigint,
    CONSTRAINT dependent_aud_genero_check CHECK (((genero)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMENINO'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT dependent_aud_relacion_check CHECK (((relacion)::text = ANY ((ARRAY['CONYUGE'::character varying, 'HIJO'::character varying, 'HIJA'::character varying, 'PADRE'::character varying, 'MADRE'::character varying, 'HERMANO'::character varying, 'HERMANA'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.document_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    descripcion character varying(500),
    estado character varying(20),
    fecha_emision date,
    fecha_vencimiento date,
    nombre_archivo character varying(200),
    tenant_id bigint,
    tipo_documento character varying(50),
    updated_at timestamp(6) without time zone,
    url_archivo character varying(500),
    employee_id bigint,
    CONSTRAINT document_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['VIGENTE'::character varying, 'VENCIDO'::character varying, 'RENOVADO'::character varying, 'ANULADO'::character varying])::text[]))),
    CONSTRAINT document_aud_tipo_documento_check CHECK (((tipo_documento)::text = ANY ((ARRAY['CV'::character varying, 'CONTRATO'::character varying, 'CERTIFICADO_TRABAJO'::character varying, 'CERTIFICADO_ESTUDIOS'::character varying, 'ANTECEDENTES_PENALES'::character varying, 'ANTECEDENTES_POLICIALES'::character varying, 'CERTIFICADO_SALUD'::character varying, 'LICENCIA_CONDUCIR'::character varying, 'CARTA_RECOMENDACION'::character varying, 'TITULO_PROFESIONAL'::character varying, 'GRADO_ACADEMICO'::character varying, 'CERTIFICACION_TECNICA'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.emergency_contact_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    direccion character varying(500),
    es_principal boolean,
    nombre_completo character varying(200),
    relacion character varying(50),
    telefono character varying(20),
    telefono_alternativo character varying(20),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    employee_id bigint,
    CONSTRAINT emergency_contact_aud_relacion_check CHECK (((relacion)::text = ANY ((ARRAY['PADRE'::character varying, 'MADRE'::character varying, 'CONYUGE'::character varying, 'HIJO'::character varying, 'HIJA'::character varying, 'HERMANO'::character varying, 'HERMANA'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.employee_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    apellidos character varying(100),
    area character varying(100),
    cargo character varying(100),
    codigo_empleado character varying(20),
    created_at timestamp(6) without time zone,
    departamento_geo character varying(100),
    direccion character varying(500),
    distrito character varying(100),
    documento_identidad character varying(20),
    email character varying(100),
    estado character varying(20),
    estado_civil character varying(20),
    fecha_ingreso date,
    fecha_nacimiento date,
    fecha_salida date,
    foto_url character varying(500),
    genero character varying(20),
    linkedin_url character varying(500),
    motivo_salida character varying(500),
    nacionalidad character varying(50),
    nivel_educacion character varying(50),
    nombres character varying(100),
    profesion character varying(100),
    provincia character varying(100),
    telefono character varying(20),
    tenant_id bigint,
    tipo_documento character varying(20),
    tipo_sangre character varying(5),
    universidad character varying(200),
    updated_at timestamp(6) without time zone,
    department_id bigint,
    position_id bigint,
    supervisor_id bigint,
    afp_nombre character varying(20),
    sistema_previsional character varying(10),
    store_id bigint,
    user_id bigint,
    CONSTRAINT employee_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'SUSPENDIDO'::character varying, 'CESADO'::character varying])::text[]))),
    CONSTRAINT employee_aud_estado_civil_check CHECK (((estado_civil)::text = ANY ((ARRAY['SOLTERO'::character varying, 'CASADO'::character varying, 'DIVORCIADO'::character varying, 'VIUDO'::character varying, 'CONVIVIENTE'::character varying])::text[]))),
    CONSTRAINT employee_aud_genero_check CHECK (((genero)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMENINO'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.evaluation_criteria_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    activo boolean,
    created_at timestamp(6) without time zone,
    descripcion character varying(500),
    nombre character varying(100),
    peso_porcentaje numeric(5,2),
    puntaje_maximo numeric(5,2),
    puntaje_minimo numeric(5,2),
    tenant_id bigint,
    updated_at timestamp(6) without time zone
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.evaluation_detail_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    comentarios character varying(1000),
    created_at timestamp(6) without time zone,
    puntaje numeric(5,2),
    tenant_id bigint,
    criteria_id bigint,
    evaluation_id bigint
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.goal_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    descripcion text,
    estado character varying(20),
    fecha_fin date,
    fecha_inicio date,
    porcentaje_avance numeric(5,2),
    prioridad character varying(20),
    tenant_id bigint,
    titulo character varying(200),
    updated_at timestamp(6) without time zone,
    asignado_por bigint,
    employee_id bigint,
    CONSTRAINT goal_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['EN_PROGRESO'::character varying, 'COMPLETADO'::character varying, 'CANCELADO'::character varying, 'RETRASADO'::character varying])::text[]))),
    CONSTRAINT goal_aud_prioridad_check CHECK (((prioridad)::text = ANY ((ARRAY['ALTA'::character varying, 'MEDIA'::character varying, 'BAJA'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.leave_balance_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    anio integer,
    created_at timestamp(6) without time zone,
    dias_disponibles numeric(5,2),
    dias_ganados numeric(5,2),
    dias_usados numeric(5,2),
    dias_vencidos numeric(5,2),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    employee_id bigint
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.payroll_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    afp_onp character varying(50),
    asignacion_familiar numeric(10,2),
    bonos numeric(10,2),
    created_at timestamp(6) without time zone,
    cts numeric(10,2),
    descuentos numeric(10,2),
    dias_trabajados integer,
    essalud numeric(10,2),
    estado character varying(20),
    fecha_pago date,
    gratificacion numeric(10,2),
    horas_extras numeric(5,2),
    monto_afp_onp numeric(10,2),
    monto_horas_extras numeric(10,2),
    neto numeric(10,2),
    pago_id bigint,
    periodo character varying(7),
    renta_quinta numeric(10,2),
    sueldo_base numeric(10,2),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    employee_id bigint,
    CONSTRAINT payroll_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['GENERADO'::character varying, 'APROBADO'::character varying, 'PAGADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.payroll_detail_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    cantidad numeric(10,2),
    concepto character varying(100),
    created_at timestamp(6) without time zone,
    monto numeric(10,2),
    tasa numeric(5,2),
    tenant_id bigint,
    tipo character varying(20),
    payroll_id bigint,
    CONSTRAINT payroll_detail_aud_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['INGRESO'::character varying, 'DESCUENTO'::character varying, 'APORTE_EMPLEADOR'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.performance_evaluation_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    areas_mejora character varying(1000),
    comentarios character varying(2000),
    created_at timestamp(6) without time zone,
    estado character varying(20),
    fecha_evaluacion date,
    fortalezas character varying(1000),
    periodo character varying(7),
    plan_mejora text,
    proxima_revision date,
    puntaje numeric(5,2),
    tenant_id bigint,
    tipo_evaluacion character varying(50),
    updated_at timestamp(6) without time zone,
    employee_id bigint,
    evaluador_id bigint,
    CONSTRAINT performance_evaluation_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['BORRADOR'::character varying, 'COMPLETADA'::character varying, 'APROBADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT performance_evaluation_aud_tipo_evaluacion_check CHECK (((tipo_evaluacion)::text = ANY ((ARRAY['ANUAL'::character varying, 'SEMESTRAL'::character varying, 'TRIMESTRAL'::character varying, 'PERIODO_PRUEBA'::character varying, 'PROMOCION'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.position_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    activo boolean,
    codigo character varying(20),
    created_at timestamp(6) without time zone,
    descripcion character varying(1000),
    nivel character varying(50),
    nombre character varying(100),
    requisitos text,
    salario_maximo numeric(10,2),
    salario_minimo numeric(10,2),
    tenant_id bigint,
    updated_at timestamp(6) without time zone,
    department_id bigint
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.salary_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    fecha_fin date,
    fecha_inicio date,
    moneda character varying(3),
    motivo character varying(100),
    porcentaje_incremento numeric(5,2),
    salario_base numeric(10,2),
    tenant_id bigint,
    aprobado_por bigint,
    employee_id bigint,
    CONSTRAINT salary_aud_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['INCREMENTO'::character varying, 'PROMOCION'::character varying, 'AJUSTE_MERCADO'::character varying, 'CAMBIO_PUESTO'::character varying, 'NEGOCIACION'::character varying, 'AJUSTE_INFLACION'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.training_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at date,
    descripcion character varying(1000),
    duracion_horas integer,
    estado character varying(20),
    fecha_fin date,
    fecha_inicio date,
    instructor character varying(200),
    nombre character varying(200),
    tenant_id bigint,
    updated_at date,
    CONSTRAINT training_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['PLANIFICADO'::character varying, 'EN_CURSO'::character varying, 'COMPLETADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.training_participation_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    aprobado boolean,
    asistencia_porcentaje numeric(5,2),
    certificado_emitido boolean,
    comentarios character varying(500),
    created_at date,
    employee_id bigint,
    estado character varying(20),
    fecha_inscripcion date,
    nota_final numeric(5,2),
    tenant_id bigint,
    training_id bigint,
    updated_at date,
    CONSTRAINT training_participation_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['INSCRITO'::character varying, 'EN_CURSO'::character varying, 'COMPLETADO'::character varying, 'ABANDONADO'::character varying, 'REPROBADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS dbshoprrhh.vacation_request_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    balance_usado numeric(5,2),
    comentarios_aprobacion character varying(500),
    created_at timestamp(6) without time zone,
    dias integer,
    documento_url character varying(500),
    estado character varying(20),
    fecha_aprobacion date,
    fecha_fin date,
    fecha_inicio date,
    motivo character varying(500),
    tenant_id bigint,
    tipo_vacacion character varying(50),
    updated_at timestamp(6) without time zone,
    aprobado_por bigint,
    employee_id bigint,
    reemplazo_id bigint,
    CONSTRAINT vacation_request_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'APROBADO'::character varying, 'RECHAZADO'::character varying, 'TOMADO'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT vacation_request_aud_tipo_vacacion_check CHECK (((tipo_vacacion)::text = ANY ((ARRAY['ANUAL'::character varying, 'TRUNCAS'::character varying, 'COMPENSATORIAS'::character varying, 'SIN_GOCE'::character varying])::text[])))
);


-- Claves primarias compuestas (rev, id) ---------------------------------------------------------------

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'attendance_aud'
                      AND c.conname = 'attendance_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.attendance_aud ADD CONSTRAINT attendance_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'contract_aud'
                      AND c.conname = 'contract_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.contract_aud ADD CONSTRAINT contract_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'department_aud'
                      AND c.conname = 'department_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.department_aud ADD CONSTRAINT department_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'dependent_aud'
                      AND c.conname = 'dependent_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.dependent_aud ADD CONSTRAINT dependent_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'document_aud'
                      AND c.conname = 'document_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.document_aud ADD CONSTRAINT document_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'emergency_contact_aud'
                      AND c.conname = 'emergency_contact_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.emergency_contact_aud ADD CONSTRAINT emergency_contact_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'employee_aud'
                      AND c.conname = 'employee_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.employee_aud ADD CONSTRAINT employee_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'evaluation_criteria_aud'
                      AND c.conname = 'evaluation_criteria_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.evaluation_criteria_aud ADD CONSTRAINT evaluation_criteria_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'evaluation_detail_aud'
                      AND c.conname = 'evaluation_detail_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.evaluation_detail_aud ADD CONSTRAINT evaluation_detail_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'goal_aud'
                      AND c.conname = 'goal_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.goal_aud ADD CONSTRAINT goal_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'leave_balance_aud'
                      AND c.conname = 'leave_balance_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.leave_balance_aud ADD CONSTRAINT leave_balance_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'payroll_aud'
                      AND c.conname = 'payroll_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.payroll_aud ADD CONSTRAINT payroll_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'payroll_detail_aud'
                      AND c.conname = 'payroll_detail_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.payroll_detail_aud ADD CONSTRAINT payroll_detail_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'performance_evaluation_aud'
                      AND c.conname = 'performance_evaluation_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.performance_evaluation_aud ADD CONSTRAINT performance_evaluation_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'position_aud'
                      AND c.conname = 'position_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.position_aud ADD CONSTRAINT position_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'revinfo'
                      AND c.conname = 'revinfo_pkey') THEN
        ALTER TABLE dbshoprrhh.revinfo ADD CONSTRAINT revinfo_pkey PRIMARY KEY (rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'salary_aud'
                      AND c.conname = 'salary_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.salary_aud ADD CONSTRAINT salary_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'training_aud'
                      AND c.conname = 'training_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.training_aud ADD CONSTRAINT training_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'training_participation_aud'
                      AND c.conname = 'training_participation_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.training_participation_aud ADD CONSTRAINT training_participation_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'vacation_request_aud'
                      AND c.conname = 'vacation_request_aud_pkey') THEN
        ALTER TABLE dbshoprrhh.vacation_request_aud ADD CONSTRAINT vacation_request_aud_pkey PRIMARY KEY (rev, id);
    END IF;
END $$;


-- Claves foráneas hacia revinfo ---------------------------------------------------------------

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'employee_aud'
                      AND c.conname = 'fk118cwnbfk1ny0ttu4bfqmeh8q') THEN
        ALTER TABLE dbshoprrhh.employee_aud ADD CONSTRAINT fk118cwnbfk1ny0ttu4bfqmeh8q FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'training_participation_aud'
                      AND c.conname = 'fk4wr7ufko1g967451ugflp6yo7') THEN
        ALTER TABLE dbshoprrhh.training_participation_aud ADD CONSTRAINT fk4wr7ufko1g967451ugflp6yo7 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'evaluation_detail_aud'
                      AND c.conname = 'fk5u95r30g21g6o2gi63a4275l4') THEN
        ALTER TABLE dbshoprrhh.evaluation_detail_aud ADD CONSTRAINT fk5u95r30g21g6o2gi63a4275l4 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'salary_aud'
                      AND c.conname = 'fkchi7yvayd79mnqnjifwejwjps') THEN
        ALTER TABLE dbshoprrhh.salary_aud ADD CONSTRAINT fkchi7yvayd79mnqnjifwejwjps FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'attendance_aud'
                      AND c.conname = 'fkcrnr2oxw00a5a5psb728o4pal') THEN
        ALTER TABLE dbshoprrhh.attendance_aud ADD CONSTRAINT fkcrnr2oxw00a5a5psb728o4pal FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'department_aud'
                      AND c.conname = 'fkdrxjxvx2qlyxtsq8teb2fgqy8') THEN
        ALTER TABLE dbshoprrhh.department_aud ADD CONSTRAINT fkdrxjxvx2qlyxtsq8teb2fgqy8 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'contract_aud'
                      AND c.conname = 'fkdwmknd8t7wjko72bg4ka0gtnb') THEN
        ALTER TABLE dbshoprrhh.contract_aud ADD CONSTRAINT fkdwmknd8t7wjko72bg4ka0gtnb FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'vacation_request_aud'
                      AND c.conname = 'fkg1ne9d0tph6mobww7smwtrfov') THEN
        ALTER TABLE dbshoprrhh.vacation_request_aud ADD CONSTRAINT fkg1ne9d0tph6mobww7smwtrfov FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'evaluation_criteria_aud'
                      AND c.conname = 'fkibyhj8tmwf1rqd7jenfwca64n') THEN
        ALTER TABLE dbshoprrhh.evaluation_criteria_aud ADD CONSTRAINT fkibyhj8tmwf1rqd7jenfwca64n FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'emergency_contact_aud'
                      AND c.conname = 'fkiyer4roa5y0asnylkq9w2htfr') THEN
        ALTER TABLE dbshoprrhh.emergency_contact_aud ADD CONSTRAINT fkiyer4roa5y0asnylkq9w2htfr FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'goal_aud'
                      AND c.conname = 'fkj2agfna90skoaiycrug5htfg3') THEN
        ALTER TABLE dbshoprrhh.goal_aud ADD CONSTRAINT fkj2agfna90skoaiycrug5htfg3 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'payroll_detail_aud'
                      AND c.conname = 'fkjl41iga81a8rj4ig6u9uox8xb') THEN
        ALTER TABLE dbshoprrhh.payroll_detail_aud ADD CONSTRAINT fkjl41iga81a8rj4ig6u9uox8xb FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'training_aud'
                      AND c.conname = 'fkko80nxyjjn8jnyqa8v6kaqqej') THEN
        ALTER TABLE dbshoprrhh.training_aud ADD CONSTRAINT fkko80nxyjjn8jnyqa8v6kaqqej FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'performance_evaluation_aud'
                      AND c.conname = 'fklypu8heeudk6yji40hvlnlqin') THEN
        ALTER TABLE dbshoprrhh.performance_evaluation_aud ADD CONSTRAINT fklypu8heeudk6yji40hvlnlqin FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'payroll_aud'
                      AND c.conname = 'fkm9gjks84xfuw5glrq5tg13cmw') THEN
        ALTER TABLE dbshoprrhh.payroll_aud ADD CONSTRAINT fkm9gjks84xfuw5glrq5tg13cmw FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'document_aud'
                      AND c.conname = 'fknb1mvvdy2r5eufnxnwv45tu20') THEN
        ALTER TABLE dbshoprrhh.document_aud ADD CONSTRAINT fknb1mvvdy2r5eufnxnwv45tu20 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'position_aud'
                      AND c.conname = 'fknmxis9h76fdd7tu0als3es0xv') THEN
        ALTER TABLE dbshoprrhh.position_aud ADD CONSTRAINT fknmxis9h76fdd7tu0als3es0xv FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'dependent_aud'
                      AND c.conname = 'fkoru5c6bhl4tbtwo8q2msfstvf') THEN
        ALTER TABLE dbshoprrhh.dependent_aud ADD CONSTRAINT fkoru5c6bhl4tbtwo8q2msfstvf FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshoprrhh' AND r.relname = 'leave_balance_aud'
                      AND c.conname = 'fkt4t9w2oc1qipy9ii4588khi91') THEN
        ALTER TABLE dbshoprrhh.leave_balance_aud ADD CONSTRAINT fkt4t9w2oc1qipy9ii4588khi91 FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;
