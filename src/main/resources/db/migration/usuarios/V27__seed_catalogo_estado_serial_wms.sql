-- Catálogo de estado de número de serie (WMS logística), F2 de la iniciativa de consistencia de Inventario.
-- Códigos derivados de com.microshop.logistica.domain.enums.SerialStatus.
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SERIAL_WMS.AVAILABLE', 'Disponible', 'Estado de número de serie (WMS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SERIAL_WMS.RESERVED', 'Reservado', 'Estado de número de serie (WMS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SERIAL_WMS.SOLD', 'Vendido', 'Estado de número de serie (WMS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SERIAL_WMS.RETURNED', 'Devuelto', 'Estado de número de serie (WMS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SERIAL_WMS.DEFECTIVE', 'Defectuoso', 'Estado de número de serie (WMS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog');
