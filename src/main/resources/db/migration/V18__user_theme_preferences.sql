-- V18__user_theme_preferences.sql
-- Override de tema por usuario. Sobreescribe el tema de empresa para ese usuario.
CREATE TABLE user_theme_preferences (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    company_id  BIGINT    NOT NULL,
    module      VARCHAR(20) NOT NULL,
    theme_key   VARCHAR(50) NOT NULL,
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_theme UNIQUE (user_id, company_id, module)
);
