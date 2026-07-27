-- Bug real: POST /users/api/user-companies/assign fallaba con 500
-- (org.hibernate.NonUniqueResultException: 2 results were returned) para CUALQUIER usuario
-- nuevo asignado a company_id=1, porque UserCompanyCommandService.enforceUserQuota() llama
-- subscriptionRepository.findByCompanyId(companyId) esperando 0-1 fila (Optional), pero
-- dbshopusuarios.saas_subscription tenia 2 filas ACTIVE para company_id=1.
--
-- Causa raiz (mismo patron que V31__fix_saas_plan_module_duplicates.sql): el constraint
-- "uk_company_subscription UNIQUE (company_id)" definido en V4__saas_subscriptions.sql nunca
-- se aplico realmente sobre dbshopusuarios.saas_subscription. Ademas, se confirmo que el propio
-- fix de V31 para saas_plan_module NUNCA tomo efecto: su guardia
-- "IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_plan_module')" no filtra por
-- schema/tabla, y encontro una constraint con el MISMO NOMBRE en una tabla homonima huerfana del
-- schema "public" (residuo de una epoca en que Hibernate ddl-auto creaba las tablas ahi antes de
-- que el datasource JPA se configurara con currentSchema=dbshopusuarios) — por lo que el guard
-- dio un falso positivo y jamas agrego el constraint real en dbshopusuarios.saas_plan_module.
-- Mismo problema confirmado en company_module (uk_company_module solo existe en "public").
--
-- Este migration: (1) deduplica saas_subscription conservando la fila de menor id por empresa
-- (mismo criterio que V31), (2) agrega los 3 constraints faltantes con un guard correctamente
-- scoped por schema+tabla via pg_namespace/pg_class, para que esto no pueda volver a suceder.

-- 1) Deduplicar saas_subscription: conservar la fila de menor id por company_id
DELETE FROM saas_subscription a
USING saas_subscription b
WHERE a.company_id = b.company_id
  AND a.id > b.id;

-- 2) Constraint faltante en saas_subscription (deberia existir desde V4)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'uk_company_subscription'
      AND n.nspname = 'dbshopusuarios'
      AND t.relname = 'saas_subscription'
  ) THEN
    ALTER TABLE saas_subscription ADD CONSTRAINT uk_company_subscription UNIQUE (company_id);
  END IF;
END $$;

-- 3) Re-fix real de V31: uk_plan_module nunca se aplico en dbshopusuarios (falso positivo por
-- constraint homonima en schema "public"). Guard esta vez correctamente scoped.
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'uk_plan_module'
      AND n.nspname = 'dbshopusuarios'
      AND t.relname = 'saas_plan_module'
  ) THEN
    ALTER TABLE saas_plan_module ADD CONSTRAINT uk_plan_module UNIQUE (plan_id, module_id);
  END IF;
END $$;

-- 4) Mismo gap en company_module (sin duplicados activos hoy, pero sin constraint nada lo impide)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'uk_company_module'
      AND n.nspname = 'dbshopusuarios'
      AND t.relname = 'company_module'
  ) THEN
    ALTER TABLE company_module ADD CONSTRAINT uk_company_module UNIQUE (company_id, module_id);
  END IF;
END $$;
