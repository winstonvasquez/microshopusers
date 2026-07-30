-- V40__sesion_jti_y_revocacion.sql
--
-- REVOCACION DE SESION  (M27)
--
-- Estado antes de esto: la tabla `sesion` guardaba 1267 filas y las 1267 estaban marcadas
-- `valido = true`. Nada las invalidaba nunca — ni el logout, ni el reseteo de contrasena, ni la
-- suspension de la empresa. B08 cerro el bloqueo en el LOGIN, pero un token ya emitido seguia siendo
-- valido hasta expirar (24 h) porque ningun servicio consultaba nada por peticion. Suspender un
-- tenant no cortaba a quien ya estuviera dentro.
--
-- POR QUE `jti` Y NO EL TOKEN ENTERO
-- La tabla ya guarda el JWT completo en una columna `varchar`, y `findByToken` lo busca por igualdad
-- de cadena: un indice sobre una cadena de ~800 caracteres, comparada en cada peticion. El `jti` que
-- M42 introdujo en el token es un UUID de 36 caracteres, unico por token, y es el identificador
-- natural para revocar. Se anade la columna y su indice; la columna `token` se conserva porque hay
-- codigo que aun la usa y borrarla no aporta nada.
--
-- BACKFILL: las sesiones existentes se quedan con `jti` NULL a proposito. Son tokens emitidos ANTES
-- de que el jti existiera, asi que no hay valor que rellenar — no se puede inventar. La consecuencia
-- esta asumida y es acotada: esas sesiones no se pueden revocar individualmente, pero todas expiran
-- en 24 h y la comprobacion del filtro trata «sin jti» como no revocable en vez de como invalida,
-- para no expulsar a todo el mundo al desplegar.
--
-- Y se marcan como no validas las que ya estan expiradas, que es lo que `valido` deberia haber
-- reflejado siempre: 1267 filas validas incluyendo sesiones de hace meses no es un estado, es la
-- ausencia de mantenimiento.

ALTER TABLE dbshopusuarios.sesion
    ADD COLUMN IF NOT EXISTS jti VARCHAR(36);

COMMENT ON COLUMN dbshopusuarios.sesion.jti IS
    'Claim jti del JWT de esta sesion. Clave de revocacion. NULL en sesiones emitidas antes de V40: '
    'esas no se pueden revocar individualmente y expiran solas. Ver M27.';

CREATE INDEX IF NOT EXISTS idx_sesion_jti ON dbshopusuarios.sesion (jti);

-- Consulta caliente de la revocacion: «sesiones vivas de esta empresa».
CREATE INDEX IF NOT EXISTS idx_sesion_company_valido
    ON dbshopusuarios.sesion (company_id, valido);

-- Higiene: lo ya expirado no puede seguir contando como sesion valida.
DO $$
DECLARE
    cerradas INTEGER;
BEGIN
    UPDATE dbshopusuarios.sesion
       SET valido = FALSE,
           fecha_modificacion = now(),
           usuario_modificacion = 'V40'
     WHERE valido = TRUE
       AND fecha_expiracion < now();
    GET DIAGNOSTICS cerradas = ROW_COUNT;
    RAISE NOTICE 'Sesiones ya expiradas marcadas como no validas: %', cerradas;
END $$;
