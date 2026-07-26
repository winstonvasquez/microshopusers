-- Ronda 2 de consistenciación de inventario/logística (2026-07-26): PENDIENTE se agregó
-- como valor real de EstadoGuiaRemision (sealed interface, microshoplogistica) -- estado
-- inicial de las GRE auto-generadas (GuiaRemisionAutoService#generarDesdeEnvio). El
-- catálogo ESTADO_GUIA_REMISION (V25) tenía EMITIDA/EN_TRASLADO/RECIBIDA/ANULADA pero no
-- PENDIENTE. Mismo formato/convención de V25/V29 (param_key = 'CATALOGO.<TABLA>.<CODIGO>',
-- tenant_id NULL, tipo 'catalog').

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_GUIA_REMISION.PENDIENTE', 'Pendiente', 'Estado de guía de remisión', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog')
ON CONFLICT (tenant_id, param_key) DO NOTHING;
