-- =====================================================================================
-- V41 — Maestras reales: catálogos oficiales SUNAT y segmentos de cliente
-- =====================================================================================
-- Medido antes de escribir esto (SQL sobre la base de desarrollo):
--   · erp_parameters tiene 120 catálogos y 514 opciones, pero varios de los que un ERP
--     peruano usa a diario estaban INCOMPLETOS frente al catálogo oficial de SUNAT:
--       UNIDAD_MEDIDA             6 opciones, y las etiquetas repetían el código («UND»)
--       TIPO_DOCUMENTO_IDENTIDAD  4 de las 11 del catálogo 06 — faltaba el CPP, que es
--                                 hoy el documento de buena parte de la población migrante
--       TIPO_COMPROBANTE          3 de las que emite un comercio (faltaban nota de débito,
--                                 guía de remisión, ticket, liquidación de compra…)
--   · dbshopusuarios.segmento estaba VACÍA, así que el select de segmento de cliente no
--     tenía nada que ofrecer.
--
-- Criterio de compatibilidad: NO se renombra ningún código existente. Se comprobó que de
-- las unidades de medida sólo «UND» está en uso (515 variantes), pero aun así los códigos
-- viejos se conservan porque son los que están escritos en las filas ya guardadas; lo que
-- sí se mejora es su ETIQUETA (`param_value`), que es lo que ve el usuario en el select.
--
-- Contrato de la tabla (verificado): param_group='CATALOGO',
-- param_key='CATALOGO.<TABLA>.<CODIGO>', param_value=<etiqueta visible>,
-- param_description=<TABLA>, tenant_id=NULL para los catálogos globales,
-- UNIQUE (tenant_id, param_key) -> el ON CONFLICT se apoya en esa clave.
-- =====================================================================================

-- ── 1. Etiquetas legibles para las unidades que ya existían ───────────────────────────
-- Antes el select mostraba «UND», «KG», «HRS»: el código crudo. Ahora muestra el nombre.
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Unidad (UND)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.UND' AND param_value = 'UND';
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Kilogramo (KG)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.KG' AND param_value = 'KG';
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Litro (LT)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.LT' AND param_value = 'LT';
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Metro (MT)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.MT' AND param_value = 'MT';
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Caja (CAJA)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.CAJA' AND param_value = 'CAJA';
UPDATE dbshopusuarios.erp_parameters SET param_value = 'Hora (HRS)'
    WHERE param_key = 'CATALOGO.UNIDAD_MEDIDA.HRS' AND param_value = 'HRS';

-- ── 2. Unidades de medida que un comercio peruano necesita de verdad ──────────────────
-- Subconjunto del catálogo 03 de SUNAT (códigos UN/ECE) acotado a lo que se factura en
-- retail y distribución. NO se cargan las ~180 del catálogo completo a propósito: un
-- select con 180 opciones es peor que uno con las 25 que se usan, y las que falten se
-- agregan cuando aparezca el caso real.
INSERT INTO dbshopusuarios.erp_parameters
    (param_group, param_key, param_value, param_description, tenant_id, is_active, editable, tipo, usuario_creacion)
VALUES
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.NIU',  'Unidad (NIU · SUNAT)',        'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.KGM',  'Kilogramo (KGM · SUNAT)',     'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.GRM',  'Gramo (GRM)',                 'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.TNE',  'Tonelada métrica (TNE)',      'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.LTR',  'Litro (LTR · SUNAT)',         'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.MLT',  'Mililitro (MLT)',             'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.GLL',  'Galón (GLL)',                 'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.MTR',  'Metro (MTR · SUNAT)',         'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.CMT',  'Centímetro (CMT)',            'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.MTK',  'Metro cuadrado (MTK)',        'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.MTQ',  'Metro cúbico (MTQ)',          'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.BX',   'Caja (BX · SUNAT)',           'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.PK',   'Paquete (PK)',                'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.BG',   'Bolsa (BG)',                  'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.DZN',  'Docena (DZN)',                'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.CEN',  'Ciento (CEN)',                'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.MIL',  'Millar (MIL)',                'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.PR',   'Par (PR)',                    'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.SET',  'Juego / set (SET)',           'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.UNIDAD_MEDIDA.ZZ',   'Servicio (ZZ)',               'UNIDAD_MEDIDA', NULL, true, false, 'catalog', 'SYSTEM')
ON CONFLICT (tenant_id, param_key) DO NOTHING;

-- ── 3. Documentos de identidad — catálogo 06 de SUNAT ─────────────────────────────────
-- Se completan los que faltaban. El CPP (permiso temporal de permanencia) es hoy
-- imprescindible en Perú: sin él no se puede facturar a una parte de la población.
INSERT INTO dbshopusuarios.erp_parameters
    (param_group, param_key, param_value, param_description, tenant_id, is_active, editable, tipo, usuario_creacion)
VALUES
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.CPP',        'Carné de permiso temporal de permanencia (CPP)', 'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.CED_DIPL',   'Cédula diplomática de identidad',                'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.DOC_PAIS_RES','Documento de identidad del país de residencia', 'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.TIN',        'Tax Identification Number (TIN)',                'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.IN',         'Identification Number (IN)',                     'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_DOCUMENTO_IDENTIDAD.SIN_RUC',    'Doc. tributario no domiciliado sin RUC',         'TIPO_DOCUMENTO_IDENTIDAD', NULL, true, false, 'catalog', 'SYSTEM')
ON CONFLICT (tenant_id, param_key) DO NOTHING;

-- ── 4. Comprobantes — catálogo 01 de SUNAT ────────────────────────────────────────────
INSERT INTO dbshopusuarios.erp_parameters
    (param_group, param_key, param_value, param_description, tenant_id, is_active, editable, tipo, usuario_creacion)
VALUES
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.NOTA_DEBITO',      'Nota de débito',                'TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.GUIA_REMISION',    'Guía de remisión remitente',    'TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.GUIA_TRANSPORTISTA','Guía de remisión transportista','TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.TICKET',           'Ticket de máquina registradora','TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.RECIBO_HONORARIOS','Recibo por honorarios',         'TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.TIPO_COMPROBANTE.LIQUIDACION_COMPRA','Liquidación de compra',        'TIPO_COMPROBANTE', NULL, true, false, 'catalog', 'SYSTEM')
ON CONFLICT (tenant_id, param_key) DO NOTHING;

-- ── 5. Bancos y financieras — se completa con lo que falta del sistema financiero ──────
-- El catálogo existente usa el NOMBRE como código; se respeta ese estilo para no romper
-- las filas que ya lo referencian.
INSERT INTO dbshopusuarios.erp_parameters
    (param_group, param_key, param_value, param_description, tenant_id, is_active, editable, tipo, usuario_creacion)
VALUES
    ('CATALOGO', 'CATALOGO.BANCO.Mibanco',                    'Mibanco',                        'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Banco Santander Perú',       'Banco Santander Perú',           'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Citibank Perú',              'Citibank Perú',                  'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Banco de Comercio',          'Banco de Comercio',              'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Alfin Banco',                'Alfin Banco',                    'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Caja Cusco',                 'Caja Cusco',                     'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Caja Piura',                 'Caja Piura',                     'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Caja Trujillo',              'Caja Trujillo',                  'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Caja Sullana',               'Caja Sullana',                   'BANCO', NULL, true, false, 'catalog', 'SYSTEM'),
    ('CATALOGO', 'CATALOGO.BANCO.Compartamos Financiera',     'Compartamos Financiera',         'BANCO', NULL, true, false, 'catalog', 'SYSTEM')
ON CONFLICT (tenant_id, param_key) DO NOTHING;

-- ── 6. Segmentos de cliente ───────────────────────────────────────────────────────────
-- La tabla estaba vacía. Segmentación estándar de retail peruano (RFM + tipo de cliente).
-- `total_clientes` arranca en 0: es un contador que mantiene la aplicación, no un dato
-- que deba sembrarse con un número inventado.
INSERT INTO dbshopusuarios.segmento
    (nombre, descripcion, tipo_cliente, color, total_clientes, activo, company_id, fecha_creacion, usuario_creacion)
SELECT v.nombre, v.descripcion, v.tipo_cliente, v.color, 0, true, 1, now(), 'SYSTEM'
FROM (VALUES
    ('Clientes frecuentes',  'Compran al menos una vez al mes en los últimos 6 meses',              'B2C', '#0B3D91'),
    ('Clientes VIP',         'Alto ticket promedio y recurrencia; acceso a precios preferentes',     'B2C', '#F08C00'),
    ('Clientes nuevos',      'Primera compra en los últimos 30 días',                                'B2C', '#16A34A'),
    ('Clientes inactivos',   'Sin compras en los últimos 6 meses; objetivo de campañas de retorno',  'B2C', '#DC2626'),
    ('Mayoristas',           'Compran por volumen con lista de precios mayorista',                   'B2B', '#7C3AED'),
    ('Distribuidores',       'Revenden el producto en su propia red; condiciones de crédito',        'B2B', '#0891B2'),
    ('Corporativo',          'Empresas con contrato marco y facturación mensual',                    'B2B', '#334155'),
    ('Sector público',       'Entidades del Estado; compras por procedimiento de selección',         'B2B', '#B45309')
) AS v(nombre, descripcion, tipo_cliente, color)
WHERE NOT EXISTS (
    SELECT 1 FROM dbshopusuarios.segmento s WHERE s.nombre = v.nombre AND s.company_id = 1
);
