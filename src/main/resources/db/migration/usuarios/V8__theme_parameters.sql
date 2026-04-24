-- Parámetros globales de tema de tienda
INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo)
VALUES
  (NULL, 'THEME', 'SHOP_ACTIVE_THEME',           'dark',  'Tema activo de la tienda pública',                    true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true),
  (NULL, 'THEME', 'SHOP_THEME_SEASONAL_ENABLED',  'true',  'Activar temas estacionales automáticos por fecha',   true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true)
ON CONFLICT (tenant_id, param_key) DO NOTHING;

-- Tabla de temas estacionales
CREATE TABLE IF NOT EXISTS theme_seasonal (
  id          BIGSERIAL PRIMARY KEY,
  theme_key   VARCHAR(50)  NOT NULL,
  name        VARCHAR(100) NOT NULL,
  start_date  DATE         NOT NULL,
  end_date    DATE         NOT NULL,
  tenant_id   VARCHAR(20),
  active      BOOLEAN      DEFAULT true,
  created_at  TIMESTAMP    DEFAULT NOW()
);

INSERT INTO theme_seasonal (theme_key, name, start_date, end_date) VALUES
  ('christmas',    'Navidad',      '2026-12-01', '2026-12-31'),
  ('black-friday', 'Black Friday', '2026-11-24', '2026-11-30'),
  ('summer',       'Verano',       '2026-01-01', '2026-03-20');
