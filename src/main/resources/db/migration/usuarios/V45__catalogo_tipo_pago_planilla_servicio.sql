-- TES-6: el catálogo TIPO_PAGO ofrecía NOMINA, pero el enum PaymentType de tesorería es
-- PROVEEDOR, PLANILLA, SERVICIO, IMPUESTO, OTRO. Alinear catálogo con el enum.
UPDATE erp_parameters
   SET param_key = 'CATALOGO.TIPO_PAGO.PLANILLA', param_value = 'Planilla', fecha_modificacion = NOW()
 WHERE param_key = 'CATALOGO.TIPO_PAGO.NOMINA';

-- tenant_id NULL no dispara ON CONFLICT (NULLs distintos en el UNIQUE) → NOT EXISTS para idempotencia.
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
SELECT NULL, 'CATALOGO', 'CATALOGO.TIPO_PAGO.SERVICIO', 'Servicio', 'TIPO_PAGO', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'
 WHERE NOT EXISTS (SELECT 1 FROM erp_parameters WHERE tenant_id IS NULL AND param_key = 'CATALOGO.TIPO_PAGO.SERVICIO');
