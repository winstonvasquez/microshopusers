-- Catálogo de tipo de ubicación DENTRO DE UNA ZONA (WMS logística), distinto del catálogo
-- TIPO_UBICACION (V24) que es del dominio invalmacen (Location plana, sin jerarquía de zona).
-- Códigos derivados de com.microshop.logistica.domain.enums.LocationType.
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_UBICACION_ZONA.STORAGE', 'Almacenamiento', 'Tipo de ubicación dentro de una zona WMS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_UBICACION_ZONA.PICKING', 'Picking', 'Tipo de ubicación dentro de una zona WMS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_UBICACION_ZONA.RECEIVING', 'Recepción', 'Tipo de ubicación dentro de una zona WMS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_UBICACION_ZONA.SHIPPING', 'Despacho', 'Tipo de ubicación dentro de una zona WMS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_UBICACION_ZONA.STAGING', 'Consolidación', 'Tipo de ubicación dentro de una zona WMS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog');
