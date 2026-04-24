-- V9: Chat soporte cliente ↔ equipo MicroShop
-- Tablas de conversación y mensajes para chat MVP con HTTP polling

CREATE TABLE IF NOT EXISTS chat_conversacion (
    id              BIGSERIAL PRIMARY KEY,
    cliente_id      BIGINT       NOT NULL,
    asunto          VARCHAR(200),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'ABIERTA',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_message_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_conv_cliente
    ON chat_conversacion (cliente_id);

CREATE TABLE IF NOT EXISTS chat_mensaje (
    id               BIGSERIAL PRIMARY KEY,
    conversacion_id  BIGINT      NOT NULL REFERENCES chat_conversacion (id),
    emisor_id        BIGINT      NOT NULL,
    emisor_tipo      VARCHAR(10) NOT NULL,   -- 'CLIENTE' o 'SOPORTE'
    contenido        TEXT        NOT NULL,
    timestamp        TIMESTAMP   NOT NULL DEFAULT NOW(),
    leido            BOOLEAN     NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_chat_mensaje_conv
    ON chat_mensaje (conversacion_id, timestamp);
