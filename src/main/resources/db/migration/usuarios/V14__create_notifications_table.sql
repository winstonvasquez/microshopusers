-- Tabla de notificaciones in-app para usuarios
CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    company_id      BIGINT,
    type            VARCHAR(50)  NOT NULL,           -- ORDER_STATUS_CHANGE | REVIEW_REQUEST | PROMO_OFFER | SYSTEM
    title           VARCHAR(200) NOT NULL,
    body            TEXT,
    reference_id    BIGINT,
    reference_type  VARCHAR(50),
    read            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_notifications_user_date   ON notifications(user_id, created_at DESC);
