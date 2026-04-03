-- V19__erp_parameters_editable.sql
-- Agrega soporte de parámetros editables desde la UI de administración.
ALTER TABLE erp_parameters ADD COLUMN IF NOT EXISTS editable    BOOLEAN     NOT NULL DEFAULT FALSE;
ALTER TABLE erp_parameters ADD COLUMN IF NOT EXISTS tipo        VARCHAR(20) NOT NULL DEFAULT 'text';
-- tipo: text | number | boolean | email | url

-- Marcar como editables los parámetros configurables por el administrador
UPDATE erp_parameters SET editable = TRUE, tipo = 'number'
WHERE param_key IN ('IGV_RATE', 'igv.rate', 'TAX_RATE');

UPDATE erp_parameters SET editable = TRUE, tipo = 'text'
WHERE param_key IN ('STORE_NAME', 'store.name', 'APP_NAME', 'app.name');

UPDATE erp_parameters SET editable = TRUE, tipo = 'boolean'
WHERE param_key IN ('SHOP_THEME_SEASONAL_ENABLED', 'seasonal.enabled');

UPDATE erp_parameters SET editable = TRUE, tipo = 'number'
WHERE param_key IN ('MAX_PAGE_SIZE', 'pagination.max-size');
