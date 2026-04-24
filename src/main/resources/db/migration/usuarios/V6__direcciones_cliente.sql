-- Migración V6: Tabla de direcciones de clientes para tienda online
-- Almacena múltiples direcciones de envío por usuario

CREATE TABLE IF NOT EXISTS cliente_direccion (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    nombre_completo VARCHAR(100),
    telefono        VARCHAR(15),
    departamento    VARCHAR(50),
    provincia       VARCHAR(50),
    distrito        VARCHAR(50),
    direccion_linea1 VARCHAR(200),
    referencia      VARCHAR(200),
    es_principal    BOOLEAN       NOT NULL DEFAULT FALSE,
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP              DEFAULT NOW(),
    fecha_creacion  TIMESTAMP,
    usuario_creacion VARCHAR(50),
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_cliente_direccion_user_id ON cliente_direccion (user_id);
CREATE INDEX IF NOT EXISTS idx_cliente_direccion_activo ON cliente_direccion (user_id, activo);
