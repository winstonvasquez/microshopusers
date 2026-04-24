-- V5: Vinculación empleado-usuario para portal de autoservicio
ALTER TABLE employee ADD COLUMN IF NOT EXISTS user_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_employee_user ON employee(tenant_id, user_id);
