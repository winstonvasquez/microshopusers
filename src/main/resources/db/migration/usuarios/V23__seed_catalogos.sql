-- Catálogos para poblar dropdowns desde una única fuente (erp_parameters), evitando
-- <option> hardcodeados en el frontend. Convención:
--   param_group = 'CATALOGO'
--   param_key   = 'CATALOGO.<TABLA>.<CODIGO>'   (calificado → único por la constraint (tenant_id, param_key))
--   param_value = etiqueta visible
-- El endpoint GET /users/api/system/parameters/catalog/{tabla} deriva el código quitando el prefijo.
-- Orden de despliegue = orden de inserción (id asc). Globales (tenant_id NULL).

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  -- Sistema previsional del trabajador
  (NULL, 'CATALOGO', 'CATALOGO.SISTEMA_PREVISIONAL.ONP', 'ONP', 'Sistema previsional', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.SISTEMA_PREVISIONAL.AFP', 'AFP', 'Sistema previsional', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),

  -- AFP (deben coincidir con el enum com.microshop.rrhh.domain.enums.Afp)
  (NULL, 'CATALOGO', 'CATALOGO.AFP.INTEGRA',   'Integra',   'AFP', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.AFP.PRIMA',     'Prima',     'AFP', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.AFP.PROFUTURO', 'Profuturo', 'AFP', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.AFP.HABITAT',   'Habitat',   'AFP', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),

  -- Moneda
  (NULL, 'CATALOGO', 'CATALOGO.MONEDA.PEN', 'S/ (PEN)', 'Moneda', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MONEDA.USD', '$ (USD)',  'Moneda', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),

  -- Motivo de cambio de sueldo (enum SalaryChangeReason)
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.INCREMENTO',      'Incremento',           'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.PROMOCION',       'Promoción',            'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.AJUSTE_MERCADO',  'Ajuste de mercado',    'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.CAMBIO_PUESTO',   'Cambio de puesto',     'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.NEGOCIACION',     'Negociación',          'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_SALARIO.AJUSTE_INFLACION','Ajuste por inflación', 'Motivo de cambio de sueldo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),

  -- Estado del empleado (enum EmployeeStatus)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_EMPLEADO.ACTIVO',     'Activo',     'Estado del empleado', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_EMPLEADO.INACTIVO',   'Inactivo',   'Estado del empleado', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_EMPLEADO.SUSPENDIDO', 'Suspendido', 'Estado del empleado', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_EMPLEADO.CESADO',     'Cesado',     'Estado del empleado', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog')
ON CONFLICT (tenant_id, param_key) DO NOTHING;
