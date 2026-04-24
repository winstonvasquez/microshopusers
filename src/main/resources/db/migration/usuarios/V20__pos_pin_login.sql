-- V10: Agregar PIN hash para login rápido POS
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS pin_hash VARCHAR(100);

COMMENT ON COLUMN usuario.pin_hash IS 'Hash del PIN numérico para login rápido en POS';
