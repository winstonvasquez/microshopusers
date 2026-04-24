-- Tabla de tokens para recuperación de contraseña
CREATE TABLE IF NOT EXISTS password_reset_token (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      DEFAULT false,
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_prt_token ON password_reset_token (token);
CREATE INDEX IF NOT EXISTS idx_prt_user  ON password_reset_token (user_id);
