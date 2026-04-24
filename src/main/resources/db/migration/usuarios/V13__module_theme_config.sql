-- Parámetros de tema por módulo (admin y POS)
-- SHOP_ACTIVE_THEME ya existe en V8 con valor 'dark'; actualizamos al nuevo default
UPDATE erp_parameters
SET param_value = 'fresh-mint'
WHERE param_key = 'SHOP_ACTIVE_THEME'
  AND tenant_id IS NULL;

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo)
VALUES
  (NULL, 'THEME', 'ADMIN_ACTIVE_THEME', 'dark',  'Tema activo del panel de administración', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'THEME', 'POS_ACTIVE_THEME',   'dark',  'Tema activo del punto de venta',          true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true)
ON CONFLICT (tenant_id, param_key) DO NOTHING;
