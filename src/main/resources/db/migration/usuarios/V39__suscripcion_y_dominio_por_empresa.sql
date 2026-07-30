-- V39__suscripcion_y_dominio_por_empresa.sql
--
-- Dos cosas que el modelo SaaS daba por hechas y no lo estaban.
--
-- =============================================================================
-- 1. TODA EMPRESA TIENE QUE TENER SUSCRIPCION  (M06)
-- =============================================================================
-- `SaasQueryService.getEnabledModuleCodes` interpretaba «sin suscripcion» como «TODOS los modulos
-- activos». O sea que un tenant sin plan contratado recibia en el claim del token el catalogo
-- completo: POS, ventas, compras, inventario, contabilidad, logistica, tesoreria y RRHH. Y como
-- ningun backend validaba ese claim, el plan no restringia nada por ninguno de los dos lados.
--
-- Medido antes de este cambio: de 7 empresas, solo 3 tenian suscripcion. Las otras 4 operaban con
-- acceso total. La causa es que el alta por el panel de administracion
-- (`CompanyCommandService.createCompany`) no creaba suscripcion, mientras el autoservicio
-- (`SaasOnboardingCommandService`) si — dos caminos de alta con reglas distintas.
--
-- Ya cerrado el fallback en el codigo, no crear la suscripcion dejaria a esas 4 empresas sin poder
-- operar. Se les crea aqui la misma que da el autoservicio: TRIAL sobre STARTER. Un SUPERADMIN puede
-- subirlas de plan despues. Se elige STARTER y no ENTERPRISE a proposito: conceder de mas es
-- exactamente el defecto que se esta corrigiendo, y ampliar un plan es una operacion de negocio
-- trivial, mientras que descubrir que alguien tuvo acceso de mas no lo es.
--
-- =============================================================================
-- 2. DOMINIO POR EMPRESA  (M39)
-- =============================================================================
-- `company.domain` estaba vacio en las 7. Ese campo es lo unico que permite a un visitante ANONIMO
-- del storefront resolver de que tienda esta viendo el catalogo: `TenantFilter` busca la empresa por
-- la cabecera `X-Tenant-Domain` que el interceptor de app-shop envia con el hostname en toda
-- peticion. Con el campo vacio, `findByDomain` no encuentra nada, el contexto de tenant queda vacio
-- y — desde que se cerro la fuga cross-tenant del mega-menu (N04) — el storefront anonimo devuelve
-- vacio.
--
-- Se deriva del RUC y no del nombre, por tres razones: el RUC ya es UNIQUE (imposible colisionar),
-- es estable (el nombre comercial cambia) y es el MISMO valor en todos los servicios, asi que dos
-- servicios que deriven el dominio del RUC llegan al mismo resultado sin necesidad de sincronizarse.
-- Queda un dominio tecnico y feo (`20131366966.microshop.pe`); es un punto de partida valido y
-- editable desde el panel, no una eleccion comercial.
--
-- Y se anade UNIQUE: un dominio que resolviera a dos empresas haria la resolucion de tenant
-- ambigua, que es peor que no resolver. No lo era hasta ahora.

-- --- 1. Suscripcion TRIAL para las empresas que no tengan ninguna ------------------------------
DO $$
DECLARE
    plan_starter BIGINT;
    creadas      INTEGER;
BEGIN
    SELECT id INTO plan_starter FROM dbshopusuarios.saas_plan WHERE code = 'STARTER';

    IF plan_starter IS NULL THEN
        RAISE EXCEPTION 'No existe el plan STARTER en saas_plan. Sin el no se puede dar de alta la '
                        'suscripcion inicial, y sin suscripcion las empresas se quedan sin modulos '
                        'habilitados (el fallback que las concedia todos ya se cerro).';
    END IF;

    INSERT INTO dbshopusuarios.saas_subscription
        (company_id, plan_id, status, starts_at, trial_ends_at,
         activo, fecha_creacion, usuario_creacion)
    SELECT c.id, plan_starter, 'TRIAL', now(), now() + INTERVAL '30 days',
           TRUE, now(), 'V39'
      FROM dbshopusuarios.company c
     WHERE NOT EXISTS (SELECT 1 FROM dbshopusuarios.saas_subscription s WHERE s.company_id = c.id);

    GET DIAGNOSTICS creadas = ROW_COUNT;
    RAISE NOTICE 'Suscripciones TRIAL creadas: %', creadas;
END $$;

-- --- 2. Dominio derivado del RUC para quien no lo tenga ----------------------------------------
UPDATE dbshopusuarios.company
   SET domain = ruc || '.microshop.pe'
 WHERE (domain IS NULL OR domain = '')
   AND ruc IS NOT NULL AND ruc <> '';

-- --- 3. UNIQUE en domain -----------------------------------------------------------------------
DO $$
DECLARE
    duplicados INTEGER;
BEGIN
    SELECT count(*) INTO duplicados FROM (
        SELECT domain FROM dbshopusuarios.company
         WHERE domain IS NOT NULL AND domain <> ''
         GROUP BY domain HAVING count(*) > 1) x;

    IF duplicados > 0 THEN
        RAISE EXCEPTION 'Hay % dominio(s) repetidos en company. Un dominio que resuelva a dos '
                        'empresas hace ambigua la resolucion de tenant del storefront: hay que '
                        'decidir a cual pertenece antes de imponer la unicidad.', duplicados;
    END IF;

    -- Indice parcial y no constraint: `domain` es NULLABLE y en PostgreSQL cada NULL es distinto,
    -- asi que un UNIQUE normal ya toleraria varios NULL — pero el filtro deja fuera tambien la
    -- cadena vacia, que si colisionaria consigo misma y es un valor que el formulario puede enviar.
    IF NOT EXISTS (SELECT 1 FROM pg_indexes
                    WHERE schemaname = 'dbshopusuarios' AND indexname = 'uq_company_domain') THEN
        CREATE UNIQUE INDEX uq_company_domain
            ON dbshopusuarios.company (domain)
         WHERE domain IS NOT NULL AND domain <> '';
    END IF;
END $$;
