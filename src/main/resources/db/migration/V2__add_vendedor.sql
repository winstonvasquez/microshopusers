-- V2__add_vendedor.sql

-- Tabla Vendedor
CREATE TABLE vendedor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    dni_ruc VARCHAR(20),
    telefono_contacto VARCHAR(20),
    estado_aprobacion VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Audit Fields
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    usuario_creacion VARCHAR(50) NOT NULL DEFAULT 'system',
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_vendedor_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_vendedor_usuario ON vendedor(usuario_id);
