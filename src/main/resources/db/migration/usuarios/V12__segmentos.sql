-- Módulo Clientes: Segmentos de clientes
-- Permite clasificar clientes por tipo y comportamiento de compra.

CREATE TABLE IF NOT EXISTS segmento (
    id                   BIGSERIAL     PRIMARY KEY,
    nombre               VARCHAR(100)  NOT NULL,
    descripcion          TEXT,
    color                VARCHAR(20)   NOT NULL DEFAULT '#d7132a',
    tipo_cliente         VARCHAR(20)   NOT NULL DEFAULT 'REGULAR',
    total_clientes       INT           NOT NULL DEFAULT 0,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP     NOT NULL,
    usuario_creacion     VARCHAR(50)   NOT NULL,
    fecha_modificacion   TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    company_id           BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_segmento_nombre
    ON segmento (nombre)
    WHERE activo = TRUE;

CREATE INDEX IF NOT EXISTS idx_segmento_tipo_cliente
    ON segmento (tipo_cliente);

CREATE INDEX IF NOT EXISTS idx_segmento_company
    ON segmento (company_id);
