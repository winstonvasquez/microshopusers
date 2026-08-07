-- =====================================================================================
-- V43 — Retirar el catálogo plano de departamentos, que la V42 dejó duplicado
-- =====================================================================================
-- La V42 creó la tabla `ubigeo` con los tres niveles del INEI. Con eso, las 25 filas
-- CATALOGO.UBIGEO_DEPARTAMENTO.* de erp_parameters pasaron a ser una SEGUNDA fuente del
-- mismo dato, y con códigos INCOMPATIBLES:
--
--     erp_parameters : CATALOGO.UBIGEO_DEPARTAMENTO.AMAZONAS  → código 'AMAZONAS'
--     ubigeo         : departamento_codigo                    → código '01'
--
-- Dos fuentes del mismo maestro con claves distintas es precisamente cómo se llega a que
-- unas pantallas guarden 'AMAZONAS' y otras '01' en la misma columna. Este repo ya tiene
-- la cicatriz de un dato replicado que divergió (la tabla `company` de users y la de
-- ventas), así que no se deja abierta la segunda.
--
-- Antes de retirarlo se comprobó que está HUÉRFANO: 0 referencias a
-- 'UBIGEO_DEPARTAMENTO' en los seis backends y 0 en app-shop (grep sobre .java y sobre
-- todo src/ del frontend). Ninguna pantalla lo consume hoy.
--
-- Se DESACTIVA en lugar de borrarse: `getCatalog` ya filtra por `is_active`, así que el
-- endpoint deja de ofrecerlo de inmediato, y las filas siguen ahí por si alguna dirección
-- guardada resultara tener el nombre del departamento en vez del código y hubiera que
-- mapearla. Borrarlas no es reversible; esto sí.
-- =====================================================================================

UPDATE dbshopusuarios.erp_parameters
   SET is_active = false,
       activo = false,
       param_description = 'UBIGEO_DEPARTAMENTO (retirado en V43: usar la tabla ubigeo, códigos INEI)',
       fecha_modificacion = now(),
       usuario_modificacion = 'SYSTEM'
 WHERE param_key LIKE 'CATALOGO.UBIGEO_DEPARTAMENTO.%'
   AND tenant_id IS NULL;
