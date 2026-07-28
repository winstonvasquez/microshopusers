-- Logo de empresa almacenado en base de datos (binario) en lugar de URL externa.
-- Mismo patrón que banner.imagen_data en microshopventas (V16).
-- logo_url se conserva como fallback para logos externos ya cargados.

ALTER TABLE company ADD COLUMN IF NOT EXISTS logo_data BYTEA;
ALTER TABLE company ADD COLUMN IF NOT EXISTS logo_mime VARCHAR(50);
ALTER TABLE company ADD COLUMN IF NOT EXISTS logo_etag VARCHAR(64);
ALTER TABLE company ADD COLUMN IF NOT EXISTS logo_size INTEGER;

COMMENT ON COLUMN company.logo_data IS 'Bytes del logotipo (alternativa a logo_url)';
COMMENT ON COLUMN company.logo_mime IS 'MIME del logotipo: image/jpeg, image/png, image/webp';
COMMENT ON COLUMN company.logo_etag IS 'MD5 hex del binario, usado como ETag para caché HTTP';
COMMENT ON COLUMN company.logo_size IS 'Tamaño en bytes del logotipo binario';

-- logo_url ya es nullable, pero se fuerza por si algún entorno la creó NOT NULL.
DO $$ BEGIN
  ALTER TABLE company ALTER COLUMN logo_url DROP NOT NULL;
EXCEPTION WHEN others THEN NULL;
END $$;
