-- Foto del empleado almacenada en base de datos (binario) en lugar de URL externa.
-- Mismo patrón que banner.imagen_data en microshopventas (V16).
-- foto_url se conserva como fallback para fotos externas ya cargadas.

ALTER TABLE employee ADD COLUMN IF NOT EXISTS foto_data BYTEA;
ALTER TABLE employee ADD COLUMN IF NOT EXISTS foto_mime VARCHAR(50);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS foto_etag VARCHAR(64);
ALTER TABLE employee ADD COLUMN IF NOT EXISTS foto_size INTEGER;

COMMENT ON COLUMN employee.foto_data IS 'Bytes de la foto del empleado (alternativa a foto_url)';
COMMENT ON COLUMN employee.foto_mime IS 'MIME de la foto: image/jpeg, image/png, image/webp';
COMMENT ON COLUMN employee.foto_etag IS 'MD5 hex del binario, usado como ETag para caché HTTP';
COMMENT ON COLUMN employee.foto_size IS 'Tamaño en bytes de la foto binaria';

-- foto_url ya es nullable, pero se fuerza por si algún entorno la creó NOT NULL.
DO $$ BEGIN
  ALTER TABLE employee ALTER COLUMN foto_url DROP NOT NULL;
EXCEPTION WHEN others THEN NULL;
END $$;
