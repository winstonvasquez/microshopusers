-- Contenido editable de /portal/landing (Problema, Como funciona, Confianza, FAQ) — hoy vive
-- hardcodeado en landing-page.component.ts. Esta tabla permite configurarlo desde el admin del
-- ERP (SUPERADMIN o ADMIN, ver SecurityConfig) sin tocar codigo. Seed = copy actual, para que no
-- haya ninguna regresion visual el dia que el frontend empiece a consumir este endpoint.

CREATE TABLE IF NOT EXISTS landing_content_section (
    id BIGSERIAL PRIMARY KEY,
    section_key VARCHAR(50) NOT NULL UNIQUE,
    content_json TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    usuario_creacion VARCHAR(50) NOT NULL DEFAULT 'system',
    fecha_modificacion TIMESTAMP,
    usuario_modificacion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT true
);

INSERT INTO landing_content_section (section_key, content_json, usuario_creacion) VALUES
('PROBLEM_POINTS', '[
  "Vendes con cuaderno o Excel, y al cierre de mes nadie sabe si el stock real coincide con lo que dice el papel.",
  "Facturas manualmente o pagas a un tercero aparte solo para cumplir con SUNAT, sin que se conecte con tus ventas.",
  "Ventas, compras y contabilidad viven en archivos distintos, y armar un solo reporte implica copiar y pegar durante horas.",
  "Cuando creces y necesitas mas de un almacen o una planilla de RRHH, tu sistema de hojas sueltas simplemente no aguanta."
]', 'system'),
('HOW_IT_WORKS', '[
  {"title": "Registra tu RUC", "description": "Crea tu empresa con tu RUC y elige el plan segun lo que necesitas hoy. 30 dias de prueba, sin tarjeta."},
  {"title": "Tus modulos se activan solos", "description": "POS, ventas, inventario, compras... se habilitan automaticamente segun tu plan, sin instalaciones ni configuraciones complejas."},
  {"title": "Vende y factura desde el dia uno", "description": "Emite comprobantes electronicos SUNAT con todo ya conectado a tu inventario y tu contabilidad."}
]', 'system'),
('TRUST_POINTS', '[
  "Aislamiento de datos por empresa: nadie fuera de tu RUC ve tu informacion",
  "Contrasenas cifradas con BCrypt, nunca en texto plano",
  "Cada registro guarda que usuario lo creo o modifico y cuando",
  "La misma seguridad para los 3 planes: no se cobra distinto por estar protegido"
]', 'system'),
('FAQ', '[
  {"question": "¿Necesito tarjeta de credito para probarlo?", "answer": "No. Registrate con tu RUC y tienes 30 dias de prueba, sin tarjeta ni compromiso de permanencia."},
  {"question": "¿Los comprobantes que emito son validos ante SUNAT?", "answer": "El sistema genera comprobantes electronicos en formato UBL 2.1, el estandar que exige SUNAT. Emitir en produccion requiere el certificado digital de tu empresa; nuestro equipo te guia en esa configuracion."},
  {"question": "¿Que pasa si mi empresa ya usa Excel o un sistema antiguo?", "answer": "Puedes empezar a operar de inmediato. Si necesitas migrar tu historial de datos, escribenos y te ayudamos segun el volumen."},
  {"question": "¿Puedo cambiar de plan mas adelante?", "answer": "Si, tu plan puede crecer junto con tu negocio; escribenos cuando necesites mas modulos o usuarios."},
  {"question": "¿Que pasa si necesito mas usuarios de los que incluye mi plan?", "answer": "Tu equipo actual sigue operando sin problema; para sumar usuarios por encima del limite de tu plan, actualizalo cuando lo necesites."},
  {"question": "¿Mis datos estan seguros?", "answer": "Si: aislamos los datos de cada empresa, ciframos las contrasenas y cada accion queda registrada. Puedes ver el detalle completo en Planes."}
]', 'system')
ON CONFLICT (section_key) DO NOTHING;
