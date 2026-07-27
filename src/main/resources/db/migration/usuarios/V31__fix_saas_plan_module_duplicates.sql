-- Corrige drift de datos detectado 2026-07-26: saas_plan_module tenia cada par (plan_id, module_id)
-- duplicado (probablemente el constraint unico nunca se aplico porque la tabla ya existia via
-- ddl-auto antes de que corriera V4, y "CREATE TABLE IF NOT EXISTS" no la recreo). El backend
-- devolvia cada modulo 2 veces en /users/api/saas/plans (visible en pricing-page como items repetidos).
-- Ademas price_annual de STARTER habia derivado a 990.00 en vez del 999.00 que define la seed original.

-- 1) Deduplicar: conservar la fila con menor id por cada par (plan_id, module_id)
DELETE FROM saas_plan_module a
USING saas_plan_module b
WHERE a.plan_id = b.plan_id
  AND a.module_id = b.module_id
  AND a.id > b.id;

-- 2) Asegurar el constraint unico que deberia haber existido desde V4
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk_plan_module'
  ) THEN
    ALTER TABLE saas_plan_module ADD CONSTRAINT uk_plan_module UNIQUE (plan_id, module_id);
  END IF;
END $$;

-- 3) Restaurar el precio anual real de STARTER (999.00 segun V4__saas_subscriptions.sql)
UPDATE saas_plan SET price_annual = 999.00 WHERE code = 'STARTER' AND price_annual = 990.00;
