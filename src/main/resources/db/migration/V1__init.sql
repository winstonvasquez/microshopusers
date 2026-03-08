-- Tabla Company
CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla Persona
CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(15) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla Rol
CREATE TABLE rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla Usuario
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    rol_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT usuario_rol_fk FOREIGN KEY (rol_id) REFERENCES rol(id),
    CONSTRAINT usuario_persona_fk FOREIGN KEY (persona_id) REFERENCES persona(id)
);

-- Tabla UserCompany
CREATE TABLE user_company (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_user_company UNIQUE (usuario_id, company_id),
    CONSTRAINT fk_user_company_user FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_user_company_company FOREIGN KEY (company_id) REFERENCES company(id)
);

-- Tabla UserCompanyRole
CREATE TABLE user_company_role (
    id BIGSERIAL PRIMARY KEY,
    user_company_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_user_company_role UNIQUE (user_company_id, rol_id),
    CONSTRAINT fk_ucr_user_company FOREIGN KEY (user_company_id) REFERENCES user_company(id),
    CONSTRAINT fk_ucr_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

-- Tabla Sesion
CREATE TABLE sesion (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    valido BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT NOT NULL,
    company_id BIGINT,

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_creacion VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT sesion_usuario_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
