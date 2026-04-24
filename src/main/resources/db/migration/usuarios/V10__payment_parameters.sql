-- Parámetros de pago por tenant (valor inicial sandbox)
-- La tabla erp_parameters ya existe desde V5
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo)
VALUES
  (NULL, 'PAYMENT', 'MP_PUBLIC_KEY',    'TEST-00000000-0000-0000-0000-000000000000',                          'MercadoPago Public Key (sandbox)',   true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYMENT', 'MP_ACCESS_TOKEN',  'TEST-0000000000000000-000000-00000000000000000000000000-000000000', 'MercadoPago Access Token (sandbox)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYMENT', 'MP_SANDBOX_MODE',  'true',                                                               'Modo sandbox MercadoPago',           true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true)
ON CONFLICT (tenant_id, param_key) DO NOTHING;
