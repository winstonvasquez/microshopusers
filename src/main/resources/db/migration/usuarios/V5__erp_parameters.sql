-- Eliminar unique constraint vieja si existe (solo en param_key)
-- Hibernate puede generar nombres como ukXXXXX o erp_parameters_param_key_key
ALTER TABLE erp_parameters DROP CONSTRAINT IF EXISTS erp_parameters_param_key_key;
ALTER TABLE erp_parameters DROP CONSTRAINT IF EXISTS uk_erp_param_key;
-- Drop any Hibernate-generated unique constraint on param_key alone
DO $$ DECLARE _cn TEXT; BEGIN
  SELECT conname INTO _cn FROM pg_constraint
  WHERE conrelid = 'dbshopusuarios.erp_parameters'::regclass
    AND contype = 'u'
    AND array_length(conkey, 1) = 1
    AND conkey[1] = (SELECT attnum FROM pg_attribute WHERE attrelid = 'dbshopusuarios.erp_parameters'::regclass AND attname = 'param_key');
  IF _cn IS NOT NULL THEN
    EXECUTE format('ALTER TABLE erp_parameters DROP CONSTRAINT %I', _cn);
  END IF;
END $$;

-- Agregar constraint compuesta única por tenant (si no existe)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_erp_param_tenant_key') THEN
    ALTER TABLE erp_parameters ADD CONSTRAINT uq_erp_param_tenant_key UNIQUE (tenant_id, param_key);
  END IF;
END $$;

-- Seed de parámetros globales (tenant_id = NULL)
-- Nota: AuditEntity usa columnas fecha_creacion, usuario_creacion y activo (boolean)
-- erp_parameters tiene también is_active propio de la entidad
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo)
VALUES
  (NULL, 'FINANCE', 'IGV_RATE',             '0.18',   'Tasa del IGV (Impuesto General a las Ventas)',        true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'FINANCE', 'UIT_ANIO',             '5150.00','Valor de la UIT para el año vigente',                 true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYROLL', 'RMV',                  '1025.00','Remuneración Mínima Vital',                           true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYROLL', 'ASIGNACION_FAMILIAR',  '102.50', 'Asignación Familiar (10% RMV)',                       true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYROLL', 'TASA_ONP',             '0.13',   'Tasa de aporte ONP',                                  true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'PAYROLL', 'TASA_ESSALUD',         '0.09',   'Tasa de aporte ESSALUD del empleador',                true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'CPE',     'SERIE_BOLETA',         'B001',   'Serie por defecto para Boletas Electrónicas',         true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'CPE',     'SERIE_FACTURA',        'F001',   'Serie por defecto para Facturas Electrónicas',        true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'CPE',     'PREFIX_TICKET',        'TICK-',  'Prefijo para tickets internos POS',                   true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'CPE',     'PREFIX_PEDIDO',        'PED-',   'Prefijo para números de pedido',                      true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true)
ON CONFLICT (tenant_id, param_key) DO NOTHING;
