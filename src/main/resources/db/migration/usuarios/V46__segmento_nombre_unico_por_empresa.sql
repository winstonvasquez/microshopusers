-- El nombre de segmento es único dentro de la empresa, no entre todas: con el índice global la
-- empresa 2 no podía crear «Clientes VIP» porque la 1 ya lo tenía (y el error lo revelaba).
DROP INDEX IF EXISTS uq_segmento_nombre;
CREATE UNIQUE INDEX IF NOT EXISTS uq_segmento_company_nombre
    ON segmento (company_id, nombre) WHERE activo = true;
