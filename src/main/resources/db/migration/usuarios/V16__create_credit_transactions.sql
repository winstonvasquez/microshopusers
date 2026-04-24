-- Movimientos de saldo de crédito
CREATE TABLE IF NOT EXISTS credit_transactions (
    id                BIGSERIAL PRIMARY KEY,
    credit_account_id BIGINT        NOT NULL REFERENCES credit_accounts(id),
    type              VARCHAR(30)   NOT NULL,   -- RECARGA | USO | DEVOLUCION | BONUS | AJUSTE
    amount            DECIMAL(10,2) NOT NULL,
    balance_before    DECIMAL(10,2) NOT NULL,
    balance_after     DECIMAL(10,2) NOT NULL,
    reference_id      BIGINT,
    reference_type    VARCHAR(50),
    description       TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_credit_tx_account ON credit_transactions(credit_account_id);
CREATE INDEX IF NOT EXISTS idx_credit_tx_date    ON credit_transactions(created_at DESC);
