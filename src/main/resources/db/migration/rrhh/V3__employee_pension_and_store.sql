-- V3: Campos adicionales para sistema previsional y tienda asignada
ALTER TABLE employee ADD COLUMN IF NOT EXISTS sistema_previsional VARCHAR(10) DEFAULT 'ONP';
ALTER TABLE employee ADD COLUMN IF NOT EXISTS afp_nombre VARCHAR(20);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS store_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_employee_store ON employee(tenant_id, store_id);
