-- Catálogos para las zonas de almacén WMS (F2 de la iniciativa de consistencia de Inventario).
-- Códigos derivados de com.microshop.logistica.domain.enums.ZoneType / TemperatureType.
-- Misma convención de V23/V24/V25: param_key = 'CATALOGO.<TABLA>.<CODIGO>', tenant_id NULL (global), tipo 'catalog'.

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.RECEPCION', 'Recepción', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.ALMACENAMIENTO', 'Almacenamiento', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.PICKING', 'Picking', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.PACKING', 'Packing', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.DESPACHO', 'Despacho', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.DEVOLUCIONES', 'Devoluciones', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ZONA_ALMACEN.CROSS_DOCK', 'Cross-Dock', 'Tipo de zona de almacén', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TEMPERATURA_ZONA.AMBIENTE', 'Ambiente', 'Temperatura de zona (cadena de frío)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TEMPERATURA_ZONA.FRIO', 'Frío', 'Temperatura de zona (cadena de frío)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TEMPERATURA_ZONA.CONGELADO', 'Congelado', 'Temperatura de zona (cadena de frío)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog');
