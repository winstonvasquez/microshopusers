-- V37__vendedor_company_id.sql
--
-- B07: el módulo Vendedores no tenía NINGÚN discriminador de tenant en ninguna capa. La tabla
-- `vendedor` (creada en V2) solo llevaba `usuario_id`, así que `GET /users/api/v1/vendedores`
-- devolvía los vendedores de TODAS las empresas de la plataforma a cualquier usuario autenticado,
-- y `PATCH /{id}/status` permitía aprobar o rechazar el vendedor de otra empresa.
--
-- No se podía arreglar solo con una anotación: faltaba la columna contra la que comparar.
--
-- El backfill deriva la empresa desde la tabla de membresías `user_company` (misma fuente que usa
-- la defensa IDOR de los endpoints de usuario). Si quedara alguna fila sin empresa resoluble, el
-- SET NOT NULL de más abajo falla a propósito: es preferible que la migración se detenga a dejar
-- filas de negocio sin tenant asignado, que es justo el defecto que esta migración corrige.

ALTER TABLE vendedor ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- Empresa del vendedor = empresa de la membresía activa de su usuario. MIN() resuelve de forma
-- determinista el caso de un usuario con varias membresías activas.
UPDATE vendedor v
SET company_id = (
    SELECT MIN(uc.company_id)
    FROM user_company uc
    WHERE uc.usuario_id = v.usuario_id
      AND uc.is_active = TRUE
)
WHERE v.company_id IS NULL;

ALTER TABLE vendedor ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE vendedor
    ADD CONSTRAINT fk_vendedor_company FOREIGN KEY (company_id) REFERENCES company (id);

-- Índice con el discriminador de tenant al frente, como exige la convención del proyecto.
CREATE INDEX IF NOT EXISTS idx_vendedor_company ON vendedor (company_id);
CREATE INDEX IF NOT EXISTS idx_vendedor_company_estado ON vendedor (company_id, estado_aprobacion);
