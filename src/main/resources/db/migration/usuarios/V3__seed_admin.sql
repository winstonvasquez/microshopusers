-- V3__seed_admin.sql
-- Datos iniciales: empresa demo, roles y usuario admin

-- Empresa demo
INSERT INTO company (name, ruc, is_active, fecha_creacion, usuario_creacion, activo)
VALUES ('MicroShop S.A.C.', '20123456789', TRUE, NOW(), 'system', TRUE)
ON CONFLICT DO NOTHING;

-- Roles básicos
INSERT INTO rol (nombre, descripcion, fecha_creacion, usuario_creacion, activo)
VALUES
    ('ADMIN',    'Administrador del sistema',  NOW(), 'system', TRUE),
    ('VENDEDOR', 'Vendedor / Cajero',           NOW(), 'system', TRUE),
    ('COMPRADOR','Responsable de compras',      NOW(), 'system', TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- Persona para el admin
INSERT INTO persona (nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento,
                     fecha_creacion, usuario_creacion, activo)
VALUES ('Admin', 'Sistema', 'DNI', '00000001', '1990-01-01', NOW(), 'system', TRUE)
ON CONFLICT (numero_documento) DO NOTHING;

-- Usuario admin  (password: 12345678, BCrypt cost=10)
INSERT INTO usuario (username, password, email, rol_id, persona_id,
                     fecha_creacion, usuario_creacion, activo)
SELECT 'admin',
       '$2a$10$myeFE2FkjedMcC5xyBhYPOt9Kiq6ILoOlgpj.h24ueMNtglJKQewy',
       'admin@microshop.com',
       r.id,
       p.id,
       NOW(), 'system', TRUE
FROM rol r, persona p
WHERE r.nombre = 'ADMIN'
  AND p.numero_documento = '00000001'
ON CONFLICT (email) DO NOTHING;

-- Asociar admin con la empresa
INSERT INTO user_company (usuario_id, company_id, is_active, fecha_creacion, usuario_creacion, activo)
SELECT u.id, c.id, TRUE, NOW(), 'system', TRUE
FROM usuario u, company c
WHERE u.email = 'admin@microshop.com'
  AND c.ruc = '20123456789'
ON CONFLICT (usuario_id, company_id) DO NOTHING;

-- Rol ADMIN para la empresa
INSERT INTO user_company_role (user_company_id, rol_id, fecha_creacion, usuario_creacion, activo)
SELECT uc.id, r.id, NOW(), 'system', TRUE
FROM user_company uc
JOIN usuario u ON u.id = uc.usuario_id
JOIN company c ON c.id = uc.company_id
JOIN rol r ON r.nombre = 'ADMIN'
WHERE u.email = 'admin@microshop.com'
  AND c.ruc = '20123456789'
ON CONFLICT (user_company_id, rol_id) DO NOTHING;
