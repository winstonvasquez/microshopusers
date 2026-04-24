-- Cuenta de saldo de crédito por cliente
CREATE TABLE IF NOT EXISTS credit_accounts (
    id          BIGSERIAL PRIMARY KEY,
    cliente_id  BIGINT        NOT NULL UNIQUE,  -- userId
    balance     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency    VARCHAR(3)    NOT NULL DEFAULT 'PEN',
    company_id  BIGINT,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_credit_accounts_cliente ON credit_accounts(cliente_id);
