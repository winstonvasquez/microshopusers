-- Nuevas transiciones de estado agregadas en microshopcompras que el catálogo
-- de erp_parameters aún no tenía: Devolucion.RECHAZADA y Cotizacion.CANCELADA.
-- Mismo formato/convención de V25 (param_key = 'CATALOGO.<TABLA>.<CODIGO>', tenant_id NULL, tipo 'catalog').

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_DEVOLUCION_COMPRA.RECHAZADA', 'Rechazada', 'Estado de devolución a proveedor', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_COTIZACION.CANCELADA', 'Cancelada', 'Estado de cotización', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog')
ON CONFLICT (tenant_id, param_key) DO NOTHING;
