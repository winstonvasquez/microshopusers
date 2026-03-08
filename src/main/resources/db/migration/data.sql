-- Init roles
INSERT INTO rol (nombre, descripcion) SELECT 'ADMIN', 'Administrador' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMIN');
INSERT INTO rol (nombre, descripcion) SELECT 'USER', 'Usuario' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'USER');
INSERT INTO rol (nombre, descripcion) SELECT 'CUSTOMER', 'Cliente' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'CUSTOMER');
INSERT INTO rol (nombre, descripcion) SELECT 'SELLER', 'Vendedor' WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'SELLER');

-- Init persona for admin
INSERT INTO persona (nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento) SELECT 'Admin', 'System', 'DNI', '00000000', '2000-01-01' WHERE NOT EXISTS (SELECT 1 FROM persona WHERE numero_documento = '00000000');

-- Init admin user (password: 12345678)
INSERT INTO usuario (username, password, email, rol_id, persona_id, created_at, created_by) SELECT 'admin', '$2b$10$vbSRv9VjQEO68ZGz0rxO4uPbInUiP8LdA3YI3SF7yVUbnJ0f7lnii', 'admin@microshop.com', (SELECT id FROM rol WHERE nombre = 'ADMIN'), (SELECT id FROM persona WHERE numero_documento = '00000000'), NOW(), 'SYSTEM' WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'admin');
