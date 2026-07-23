-- V7: Bloqueo optimista para leave_balance (evita race check-then-act en actualización de días de vacaciones)
ALTER TABLE leave_balance ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
