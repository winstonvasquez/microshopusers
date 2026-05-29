-- Hardening 2026-05-28: siembra de roles ERP granulares para segregación de funciones.
-- Contexto: la auditoría halló que solo existían ADMIN/USER/CUSTOMER/SELLER, lo que impedía
-- aplicar @PreAuthorize por rol sin bloquear a todos menos ADMIN.
--
-- Esta migración es ADITIVA e IDEMPOTENTE: crea los roles pero NO asigna usuarios todavía.
-- Por lo tanto NO cambia ningún acceso existente (nadie tiene estos roles aún).
-- El enforcement granular (@PreAuthorize('hasRole(CONTADOR)') etc.) se activará por fase,
-- DESPUÉS de reasignar usuarios a estos roles. Hasta entonces el gating interino usa ADMIN.
--
-- Sigue el patrón idempotente ya usado en db/migration/usuarios/data.sql.

INSERT INTO rol (nombre, descripcion) SELECT 'CONTADOR',   'Contabilidad: asientos, libros, cierres, declaraciones tributarias' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'CONTADOR');
INSERT INTO rol (nombre, descripcion) SELECT 'TESORERO',   'Tesorería: pagos, cobranzas, cuentas bancarias, flujo de caja'      WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'TESORERO');
INSERT INTO rol (nombre, descripcion) SELECT 'RRHH',       'Recursos humanos: planilla, empleados, vacaciones, asistencia'      WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'RRHH');
INSERT INTO rol (nombre, descripcion) SELECT 'COMPRADOR',  'Compras: órdenes de compra, proveedores, recepciones, facturas'     WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'COMPRADOR');
INSERT INTO rol (nombre, descripcion) SELECT 'ALMACENERO', 'Logística/inventario: kardex, movimientos, transferencias, picking' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ALMACENERO');
INSERT INTO rol (nombre, descripcion) SELECT 'GERENTE',    'Gerencia: dashboards, reportes, aprobaciones de alto monto'         WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'GERENTE');
INSERT INTO rol (nombre, descripcion) SELECT 'SUPERADMIN', 'Superadministrador multi-tenant (soporte/operaciones cross-tenant)' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'SUPERADMIN');
