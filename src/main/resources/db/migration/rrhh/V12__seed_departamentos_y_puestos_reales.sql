-- =====================================================================================
-- V12 — Maestras reales de RRHH: departamentos y puestos
-- =====================================================================================
-- Por qué existe esta migración (medido por SQL antes de escribirla):
--
--   · `dbshoprrhh.department` tenía 19 filas y las 19 eran RESIDUO DE PRUEBAS E2E:
--     18 llamadas «Departamento E2E-XXXXXX editado» y una «Prueba DBG11414». O sea que el
--     select de departamento del ERP —que el frontend ya consumía correctamente— estaba
--     sirviendo basura de test como si fuera la maestra de la empresa.
--   · `dbshoprrhh.position` estaba VACÍA. Con 6 empleados y ningún puesto, no se puede dar
--     de alta un contrato laboral: el select de puesto no tiene nada.
--   · Ningún empleado referenciaba los departamentos E2E (`department_id` NULL en los 6),
--     así que sembrar lo real aquí y borrar el residuo después no arrastra nada.
--
-- Se siembra ANTES de limpiar el residuo a propósito: al revés, el select quedaría vacío
-- entre una cosa y la otra.
--
-- Estructura: organigrama de una empresa comercial peruana mediana. Los rangos salariales
-- están en soles y por encima de la RMV vigente; son referencias de mercado para que los
-- formularios de contrato tengan un rango razonable, no compromisos de la empresa.
-- =====================================================================================

-- ── 1. Departamentos ──────────────────────────────────────────────────────────────────
-- Jerarquía en dos pasos porque `parent_id` es una FK a esta misma tabla: primero las
-- gerencias, después las áreas que cuelgan de ellas.
INSERT INTO dbshoprrhh.department (tenant_id, codigo, nombre, descripcion, activo, created_at)
SELECT 1, v.codigo, v.nombre, v.descripcion, true, now()
FROM (VALUES
    ('GER',  'Gerencia General',        'Dirección de la empresa y representación legal'),
    ('ADM',  'Administración y Finanzas','Contabilidad, tesorería, presupuesto y tributación'),
    ('COM',  'Comercial y Ventas',      'Fuerza de ventas, canal presencial y tienda en línea'),
    ('OPE',  'Operaciones',             'Almacén, logística de entrada y salida, distribución'),
    ('COMP', 'Compras y Abastecimiento','Proveedores, órdenes de compra y reposición'),
    ('RRHH', 'Recursos Humanos',        'Reclutamiento, planilla, bienestar y capacitación'),
    ('TI',   'Tecnología de la Información','Sistemas, soporte y desarrollo'),
    ('MKT',  'Marketing',               'Marca, campañas, contenidos y analítica comercial')
) AS v(codigo, nombre, descripcion)
WHERE NOT EXISTS (
    SELECT 1 FROM dbshoprrhh.department d WHERE d.tenant_id = 1 AND d.codigo = v.codigo
);

-- Áreas que dependen de una gerencia.
INSERT INTO dbshoprrhh.department (tenant_id, codigo, nombre, descripcion, parent_id, activo, created_at)
SELECT 1, v.codigo, v.nombre, v.descripcion,
       (SELECT id FROM dbshoprrhh.department p WHERE p.tenant_id = 1 AND p.codigo = v.padre),
       true, now()
FROM (VALUES
    ('CONT', 'Contabilidad',        'Registro contable, PCGE y cierres',                    'ADM'),
    ('TES',  'Tesorería',           'Caja, bancos, cobranzas y pagos',                       'ADM'),
    ('VTAS', 'Ventas Presenciales', 'Punto de venta y atención en tienda',                  'COM'),
    ('ECOM', 'Comercio Electrónico','Tienda en línea, marketplace y despacho a domicilio',   'COM'),
    ('ALM',  'Almacén',             'Recepción, ubicación, conteos y despacho',              'OPE'),
    ('DIST', 'Distribución',        'Rutas de entrega y transportistas',                     'OPE'),
    ('SOP',  'Soporte Técnico',     'Mesa de ayuda y mantenimiento de equipos',              'TI')
) AS v(codigo, nombre, descripcion, padre)
WHERE NOT EXISTS (
    SELECT 1 FROM dbshoprrhh.department d WHERE d.tenant_id = 1 AND d.codigo = v.codigo
);

-- ── 2. Puestos ────────────────────────────────────────────────────────────────────────
-- `department_id` es NOT NULL con FK, así que cada puesto se resuelve por el código del
-- departamento sembrado arriba. UNIQUE (tenant_id, codigo).
INSERT INTO dbshoprrhh."position"
    (tenant_id, codigo, nombre, descripcion, department_id, nivel, salario_minimo, salario_maximo, requisitos, activo, created_at)
SELECT 1, v.codigo, v.nombre, v.descripcion,
       (SELECT id FROM dbshoprrhh.department d WHERE d.tenant_id = 1 AND d.codigo = v.depto),
       v.nivel, v.sal_min, v.sal_max, v.requisitos, true, now()
FROM (VALUES
    ('P-GG',    'Gerente General',            'Conduce la empresa y responde ante la junta',         'GER',  'DIRECTIVO',    12000, 20000, 'Titulado en administración, economía o ingeniería. MBA deseable. 8 años en puestos de dirección.'),
    ('P-GAF',   'Gerente de Administración y Finanzas','Responsable de finanzas, contabilidad y tributación','ADM','GERENCIAL', 9000, 14000, 'Contador público o economista titulado. 6 años en el área, dominio de NIIF y tributación peruana.'),
    ('P-CONT',  'Contador General',           'Lleva la contabilidad y firma los estados financieros','CONT','JEFATURA',     4500,  7500, 'Contador público colegiado. 4 años de experiencia. Dominio de PCGE 2020 y PLE.'),
    ('P-ASIS-C','Asistente Contable',        'Registro de comprobantes y conciliaciones',            'CONT', 'OPERATIVO',    1600,  2600, 'Egresado de contabilidad. Manejo de Excel y sistemas contables.'),
    ('P-TES',   'Tesorero',                   'Gestiona caja, bancos y flujo de efectivo',            'TES',  'JEFATURA',     4000,  6500, 'Titulado en finanzas o contabilidad. 3 años en tesorería.'),
    ('P-GCOM',  'Gerente Comercial',          'Define la estrategia comercial y el plan de ventas',   'COM',  'GERENCIAL',    9000, 15000, 'Titulado en administración o marketing. 6 años liderando equipos comerciales.'),
    ('P-JVTAS', 'Jefe de Ventas',             'Dirige la fuerza de ventas y las metas del canal',     'VTAS', 'JEFATURA',     4000,  7000, 'Titulado en administración o afines. 4 años en jefatura de ventas.'),
    ('P-VEND',  'Vendedor de Tienda',         'Atiende al cliente en el punto de venta',              'VTAS', 'OPERATIVO',    1300,  2200, 'Secundaria completa. Experiencia en atención al cliente. Se valora manejo de POS.'),
    ('P-CAJ',   'Cajero',                     'Cobra, arquea la caja y emite comprobantes',           'VTAS', 'OPERATIVO',    1300,  2000, 'Secundaria completa. Manejo de efectivo y medios de pago digitales.'),
    ('P-ECOM',  'Coordinador de E-commerce',  'Administra el catálogo en línea y los pedidos web',    'ECOM', 'COORDINACION', 2800,  4500, 'Titulado o egresado en marketing o sistemas. Manejo de plataformas de comercio electrónico.'),
    ('P-GOPE',  'Gerente de Operaciones',     'Responsable de almacén, logística y distribución',     'OPE',  'GERENCIAL',    8000, 13000, 'Ingeniero industrial o afín. 6 años en operaciones logísticas.'),
    ('P-JALM',  'Jefe de Almacén',            'Controla existencias, ubicaciones y conteos',          'ALM',  'JEFATURA',     3500,  6000, 'Ingeniero industrial o técnico en logística. 4 años en almacenes. Manejo de WMS.'),
    ('P-ALMAC', 'Auxiliar de Almacén',        'Recibe, ubica y despacha mercadería',                  'ALM',  'OPERATIVO',    1300,  1900, 'Secundaria completa. Se valora certificado de operador de montacargas.'),
    ('P-JDIST', 'Jefe de Distribución',       'Planifica rutas y coordina transportistas',            'DIST', 'JEFATURA',     3500,  5800, 'Titulado en logística o administración. 3 años en distribución.'),
    ('P-CHOF',  'Conductor de Reparto',       'Entrega los pedidos y liquida las guías',              'DIST', 'OPERATIVO',    1500,  2400, 'Licencia A-IIb vigente. Récord de conductor sin sanciones graves.'),
    ('P-JCOMP', 'Jefe de Compras',            'Negocia con proveedores y aprueba órdenes de compra',  'COMP', 'JEFATURA',     4000,  7000, 'Titulado en administración o ingeniería. 4 años en compras. Capacidad de negociación.'),
    ('P-COMPR', 'Analista de Compras',        'Cotiza, compara y hace seguimiento a las órdenes',     'COMP', 'OPERATIVO',    2000,  3200, 'Egresado en administración o industrial. Manejo de Excel avanzado.'),
    ('P-JRRHH', 'Jefe de Recursos Humanos',   'Dirige planilla, reclutamiento y bienestar',           'RRHH', 'JEFATURA',     4500,  7500, 'Titulado en psicología, derecho o administración. 4 años en RRHH. Legislación laboral peruana.'),
    ('P-ASIS-R','Asistente de RRHH',          'Apoya en planilla, asistencia y expedientes',          'RRHH', 'OPERATIVO',    1600,  2500, 'Egresado en administración o psicología. Manejo de planillas electrónicas.'),
    ('P-JTI',   'Jefe de Sistemas',           'Responsable de la infraestructura y los sistemas',     'TI',   'JEFATURA',     5000,  8500, 'Ingeniero de sistemas o informático. 4 años liderando TI.'),
    ('P-SOP',   'Analista de Soporte',        'Atiende incidencias y mantiene los equipos',           'SOP',  'OPERATIVO',    1800,  3000, 'Técnico o egresado en computación. Conocimiento de redes y soporte de usuario.'),
    ('P-JMKT',  'Jefe de Marketing',          'Dirige marca, campañas y analítica comercial',         'MKT',  'JEFATURA',     4000,  7000, 'Titulado en marketing o comunicaciones. 4 años en marketing digital y tradicional.'),
    ('P-DISEN', 'Diseñador Gráfico',          'Produce las piezas de marca y campaña',                'MKT',  'OPERATIVO',    1800,  3000, 'Egresado en diseño gráfico. Portafolio y manejo de suite de diseño.')
) AS v(codigo, nombre, descripcion, depto, nivel, sal_min, sal_max, requisitos)
WHERE EXISTS (SELECT 1 FROM dbshoprrhh.department d WHERE d.tenant_id = 1 AND d.codigo = v.depto)
  AND NOT EXISTS (SELECT 1 FROM dbshoprrhh."position" p WHERE p.tenant_id = 1 AND p.codigo = v.codigo);

-- ── 3. Reasignar los empleados reales a su departamento ───────────────────────────────
-- Los 6 empleados tenían `department_id` NULL y el área sólo como texto libre. Se enlazan
-- por ese texto, que es el único dato que había. El empleado de prueba «Juan E2E-Test» no
-- se toca: lo borra la limpieza de residuo E2E.
UPDATE dbshoprrhh.employee e
SET department_id = d.id
FROM dbshoprrhh.department d
WHERE e.tenant_id = 1 AND e.department_id IS NULL AND d.tenant_id = 1
  AND d.codigo = CASE lower(trim(COALESCE(e.area, '')))
                     WHEN 'gerencia'     THEN 'GER'
                     WHEN 'ventas'       THEN 'VTAS'
                     WHEN 'contabilidad' THEN 'CONT'
                     WHEN 'logistica'    THEN 'ALM'
                     WHEN 'logística'    THEN 'ALM'
                     WHEN 'compras'      THEN 'COMP'
                     WHEN 'rrhh'         THEN 'RRHH'
                     WHEN 'marketing'    THEN 'MKT'
                     WHEN 'ti'           THEN 'TI'
                     ELSE NULL
                 END;
