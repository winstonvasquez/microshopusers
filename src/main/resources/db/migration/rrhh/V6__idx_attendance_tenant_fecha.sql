-- V6: Índice para acotar getMonthlySummary por tenant_id + fecha (evita full scan de todo el histórico)
CREATE INDEX IF NOT EXISTS idx_attendance_tenant_fecha ON attendance(tenant_id, fecha);
