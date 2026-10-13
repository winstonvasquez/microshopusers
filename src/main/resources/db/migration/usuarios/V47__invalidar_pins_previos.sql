-- Hasta la ola R2 (2026-10-13) el PIN se fijaba con POST /users/api/auth/users/{id}/set-pin, que era ANÓNIMO:
-- cualquiera podía poner el PIN de cualquier usuario (y con él autorizar descuentos y anulaciones del POS).
-- Ningún PIN fijado por esa vía es de fiar: se invalidan todos. Cada usuario genera el suyo en Mi perfil
-- (PUT /users/api/users/me/pin, con su contraseña).
UPDATE usuario SET pin_hash = NULL WHERE pin_hash IS NOT NULL;
