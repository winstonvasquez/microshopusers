-- V17__company_theme_config.sql
-- Tema base por empresa. Cada empresa puede definir su tema default por módulo.
CREATE TABLE IF NOT EXISTS company_theme_config (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT    NOT NULL,
    module      VARCHAR(20) NOT NULL,
    theme_key   VARCHAR(50) NOT NULL,
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_company_theme UNIQUE (company_id, module)
);
