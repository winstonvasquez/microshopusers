-- Additive columns to company (do NOT break existing)
ALTER TABLE company ADD COLUMN IF NOT EXISTS legal_name   VARCHAR(200);
ALTER TABLE company ADD COLUMN IF NOT EXISTS address      VARCHAR(300);
ALTER TABLE company ADD COLUMN IF NOT EXISTS phone        VARCHAR(20);
ALTER TABLE company ADD COLUMN IF NOT EXISTS email        VARCHAR(100);
ALTER TABLE company ADD COLUMN IF NOT EXISTS logo_url     VARCHAR(500);
ALTER TABLE company ADD COLUMN IF NOT EXISTS domain       VARCHAR(100);

-- Ensure defaults on pre-existing tables (Hibernate DDL-auto may have omitted them)
DO $$ BEGIN
  -- saas_module
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'dbshopusuarios' AND table_name = 'saas_module' AND column_name = 'activo') THEN
    ALTER TABLE saas_module ALTER COLUMN activo SET DEFAULT TRUE;
    ALTER TABLE saas_module ALTER COLUMN is_active SET DEFAULT TRUE;
    ALTER TABLE saas_module ALTER COLUMN fecha_creacion SET DEFAULT NOW();
    ALTER TABLE saas_module ALTER COLUMN usuario_creacion SET DEFAULT 'SYSTEM';
    ALTER TABLE saas_module ALTER COLUMN sort_order SET DEFAULT 0;
  END IF;
  -- saas_plan
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'dbshopusuarios' AND table_name = 'saas_plan' AND column_name = 'activo') THEN
    ALTER TABLE saas_plan ALTER COLUMN activo SET DEFAULT TRUE;
    ALTER TABLE saas_plan ALTER COLUMN is_active SET DEFAULT TRUE;
    ALTER TABLE saas_plan ALTER COLUMN fecha_creacion SET DEFAULT NOW();
    ALTER TABLE saas_plan ALTER COLUMN usuario_creacion SET DEFAULT 'SYSTEM';
  END IF;
  -- saas_subscription
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'dbshopusuarios' AND table_name = 'saas_subscription' AND column_name = 'activo') THEN
    ALTER TABLE saas_subscription ALTER COLUMN activo SET DEFAULT TRUE;
    ALTER TABLE saas_subscription ALTER COLUMN status SET DEFAULT 'TRIAL';
    ALTER TABLE saas_subscription ALTER COLUMN fecha_creacion SET DEFAULT NOW();
    ALTER TABLE saas_subscription ALTER COLUMN usuario_creacion SET DEFAULT 'SYSTEM';
  END IF;
  -- company_module
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'dbshopusuarios' AND table_name = 'company_module' AND column_name = 'activo') THEN
    ALTER TABLE company_module ALTER COLUMN activo SET DEFAULT TRUE;
    ALTER TABLE company_module ALTER COLUMN is_enabled SET DEFAULT TRUE;
    ALTER TABLE company_module ALTER COLUMN fecha_creacion SET DEFAULT NOW();
    ALTER TABLE company_module ALTER COLUMN usuario_creacion SET DEFAULT 'SYSTEM';
  END IF;
END $$;

-- ERP module catalog
CREATE TABLE IF NOT EXISTS saas_module (
    id                   BIGSERIAL PRIMARY KEY,
    code                 VARCHAR(50)  NOT NULL UNIQUE,
    name                 VARCHAR(100) NOT NULL,
    description          VARCHAR(500),
    icon                 VARCHAR(100),
    route_prefix         VARCHAR(100),
    sort_order           INTEGER NOT NULL DEFAULT 0,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    fecha_modificacion   TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo               BOOLEAN NOT NULL DEFAULT TRUE
);

-- Ensure defaults on pre-existing saas_plan
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'dbshopusuarios' AND table_name = 'saas_plan' AND column_name = 'activo') THEN
    ALTER TABLE saas_plan ALTER COLUMN activo SET DEFAULT TRUE;
    ALTER TABLE saas_plan ALTER COLUMN is_active SET DEFAULT TRUE;
    ALTER TABLE saas_plan ALTER COLUMN fecha_creacion SET DEFAULT NOW();
    ALTER TABLE saas_plan ALTER COLUMN usuario_creacion SET DEFAULT 'SYSTEM';
  END IF;
END $$;

-- Subscription plan catalog
CREATE TABLE IF NOT EXISTS saas_plan (
    id                   BIGSERIAL PRIMARY KEY,
    code                 VARCHAR(50)  NOT NULL UNIQUE,
    name                 VARCHAR(100) NOT NULL,
    description          VARCHAR(500),
    price_monthly        NUMERIC(10,2),
    price_annual         NUMERIC(10,2),
    max_users            INTEGER NOT NULL DEFAULT 5,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    fecha_modificacion   TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo               BOOLEAN NOT NULL DEFAULT TRUE
);

-- Modules included in each plan
CREATE TABLE IF NOT EXISTS saas_plan_module (
    id        BIGSERIAL PRIMARY KEY,
    plan_id   BIGINT NOT NULL REFERENCES saas_plan(id),
    module_id BIGINT NOT NULL REFERENCES saas_module(id),
    CONSTRAINT uk_plan_module UNIQUE (plan_id, module_id)
);

-- Active subscription per company (one per company)
CREATE TABLE IF NOT EXISTS saas_subscription (
    id                   BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL REFERENCES company(id),
    plan_id              BIGINT NOT NULL REFERENCES saas_plan(id),
    status               VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    starts_at            TIMESTAMP NOT NULL,
    ends_at              TIMESTAMP,
    trial_ends_at        TIMESTAMP,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    fecha_modificacion   TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_company_subscription UNIQUE (company_id)
);

-- Per-company module overrides (enable/disable individual modules)
CREATE TABLE IF NOT EXISTS company_module (
    id                   BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL REFERENCES company(id),
    module_id            BIGINT NOT NULL REFERENCES saas_module(id),
    is_enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_creacion     VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    fecha_modificacion   TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_company_module UNIQUE (company_id, module_id)
);

-- Seed: ERP module catalog
INSERT INTO saas_module (code, name, description, icon, route_prefix, sort_order, is_active, activo)
VALUES
  ('POS',          'Punto de Venta',    'POS táctil para ventas directas',        'cash-register', '/pos',          10, TRUE, TRUE),
  ('VENTAS',       'Ventas',            'Pedidos, cotizaciones, CPE SUNAT',       'shopping-cart', '/admin',        20, TRUE, TRUE),
  ('COMPRAS',      'Compras',           'Proveedores, órdenes de compra',         'package',       '/compras',      30, TRUE, TRUE),
  ('INVENTARIO',   'Inventario',        'Almacenes, kardex, control de stock',    'warehouse',     '/inventario',   40, TRUE, TRUE),
  ('CONTABILIDAD', 'Contabilidad',      'PCGE 2020, PLE, asientos contables',    'calculator',    '/contabilidad', 50, TRUE, TRUE),
  ('LOGISTICA',    'Logística',         'Guías de remisión electrónicas, despacho','truck',        '/logistica',    60, TRUE, TRUE),
  ('TESORERIA',    'Tesorería',         'Cajas, flujo de caja, pagos',           'bank',          '/tesoreria',    70, TRUE, TRUE),
  ('RRHH',         'RRHH',              'Planillas, asistencia, vacaciones',      'users',         '/rrhh',         80, TRUE, TRUE)
ON CONFLICT (code) DO NOTHING;

-- Seed: plan catalog
INSERT INTO saas_plan (code, name, description, price_monthly, price_annual, max_users, is_active, activo)
VALUES
  ('STARTER',      'Starter',       'Ideal para pequeñas empresas',      99.00,  999.00,   5, TRUE, TRUE),
  ('PROFESSIONAL', 'Professional',  'Para empresas en crecimiento',      299.00, 2990.00, 25, TRUE, TRUE),
  ('ENTERPRISE',   'Enterprise',    'Solución completa para corporativos',799.00, 7990.00,999, TRUE, TRUE)
ON CONFLICT (code) DO NOTHING;

-- Seed: Starter = POS + VENTAS
INSERT INTO saas_plan_module (plan_id, module_id)
SELECT p.id, m.id FROM saas_plan p, saas_module m
WHERE p.code = 'STARTER' AND m.code IN ('POS','VENTAS')
ON CONFLICT DO NOTHING;

-- Seed: Professional = everything except RRHH
INSERT INTO saas_plan_module (plan_id, module_id)
SELECT p.id, m.id FROM saas_plan p, saas_module m
WHERE p.code = 'PROFESSIONAL' AND m.code IN ('POS','VENTAS','COMPRAS','INVENTARIO','CONTABILIDAD','LOGISTICA','TESORERIA')
ON CONFLICT DO NOTHING;

-- Seed: Enterprise = all modules
INSERT INTO saas_plan_module (plan_id, module_id)
SELECT p.id, m.id FROM saas_plan p, saas_module m
WHERE p.code = 'ENTERPRISE'
ON CONFLICT DO NOTHING;

-- Assign existing company (id=1) a Professional subscription with all modules enabled (so existing tests keep working)
INSERT INTO saas_subscription (company_id, plan_id, status, starts_at, trial_ends_at, activo, fecha_creacion, usuario_creacion)
SELECT 1, p.id, 'ACTIVE', NOW(), NOW() + INTERVAL '30 days', TRUE, NOW(), 'SYSTEM'
FROM saas_plan p WHERE p.code = 'ENTERPRISE'
ON CONFLICT DO NOTHING;
