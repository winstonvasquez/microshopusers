-- =====================================================================================
-- V44 — Tildes en los nombres y descripciones del catálogo de módulos SaaS
-- =====================================================================================
-- `saas_module.name` es lo que se pinta en las cards de /portal/pricing, en la tabla
-- comparativa de planes y en el selector de módulos del alta de empresa. Estaba escrito
-- sin acentos —«Logistica», «Tesoreria», «ordenes de compra», «Guias de remision
-- electronicas», «POS tactil»—, y en una página de precios que es la primera impresión
-- comercial del producto eso se lee como descuido.
--
-- Se cambian SOLO `name` y `description`. El `code` (LOGISTICA, TESORERIA…) se deja
-- intacto a propósito: es la clave con la que `saas_plan_module` enlaza los módulos de
-- cada plan y con la que el frontend resuelve la inclusión (`p.moduleCodes.includes`).
-- Verificado antes de escribir esto: no hay ninguna comparación por NOMBRE en app-shop
-- ni en los backends, así que renombrar la etiqueta no rompe ningún enlace.
-- =====================================================================================

UPDATE dbshopusuarios.saas_module AS m SET
    name        = v.name,
    description = v.description
FROM (VALUES
    ('POS',          'Punto de Venta', 'POS táctil para ventas directas en mostrador'),
    ('VENTAS',       'Ventas',         'Pedidos, cotizaciones y comprobantes electrónicos SUNAT'),
    ('COMPRAS',      'Compras',        'Proveedores y órdenes de compra'),
    ('INVENTARIO',   'Inventario',     'Almacenes, kardex y control de stock'),
    ('CONTABILIDAD', 'Contabilidad',   'PCGE 2020, PLE y asientos contables'),
    ('LOGISTICA',    'Logística',      'Guías de remisión electrónicas y despacho'),
    ('TESORERIA',    'Tesorería',      'Cajas, flujo de caja y pagos'),
    ('RRHH',         'RRHH',           'Planillas, asistencia y vacaciones')
) AS v(code, name, description)
WHERE m.code = v.code;
