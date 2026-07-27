-- V32 sembro landing_content_section con tildes/enies perdidas (ej. "mas" en vez de "mas"
-- con tilde, "contrasenas" en vez de "contraseñas"). Corrige el contenido para que coincida
-- byte a byte con el copy real de landing-page.component.ts (fuente de verdad visual).

UPDATE landing_content_section SET content_json = '[
  "Vendes con cuaderno o Excel, y al cierre de mes nadie sabe si el stock real coincide con lo que dice el papel.",
  "Facturas manualmente o pagas a un tercero aparte solo para cumplir con SUNAT, sin que se conecte con tus ventas.",
  "Ventas, compras y contabilidad viven en archivos distintos, y armar un solo reporte implica copiar y pegar durante horas.",
  "Cuando creces y necesitas más de un almacén o una planilla de RRHH, tu sistema de hojas sueltas simplemente no aguanta."
]', usuario_modificacion = 'system', fecha_modificacion = now()
WHERE section_key = 'PROBLEM_POINTS';

UPDATE landing_content_section SET content_json = '[
  {"title": "Registra tu RUC", "description": "Crea tu empresa con tu RUC y elige el plan según lo que necesitas hoy. 30 días de prueba, sin tarjeta."},
  {"title": "Tus módulos se activan solos", "description": "POS, ventas, inventario, compras... se habilitan automáticamente según tu plan, sin instalaciones ni configuraciones complejas."},
  {"title": "Vende y factura desde el día uno", "description": "Emite comprobantes electrónicos SUNAT con todo ya conectado a tu inventario y tu contabilidad."}
]', usuario_modificacion = 'system', fecha_modificacion = now()
WHERE section_key = 'HOW_IT_WORKS';

UPDATE landing_content_section SET content_json = '[
  "Aislamiento de datos por empresa: nadie fuera de tu RUC ve tu información",
  "Contraseñas cifradas con BCrypt, nunca en texto plano",
  "Cada registro guarda qué usuario lo creó o modificó y cuándo",
  "La misma seguridad para los 3 planes: no se cobra distinto por estar protegido"
]', usuario_modificacion = 'system', fecha_modificacion = now()
WHERE section_key = 'TRUST_POINTS';

UPDATE landing_content_section SET content_json = '[
  {"question": "¿Necesito tarjeta de crédito para probarlo?", "answer": "No. Regístrate con tu RUC y tienes 30 días de prueba, sin tarjeta ni compromiso de permanencia."},
  {"question": "¿Los comprobantes que emito son válidos ante SUNAT?", "answer": "El sistema genera comprobantes electrónicos en formato UBL 2.1, el estándar que exige SUNAT. Emitir en producción requiere el certificado digital de tu empresa; nuestro equipo te guía en esa configuración."},
  {"question": "¿Qué pasa si mi empresa ya usa Excel o un sistema antiguo?", "answer": "Puedes empezar a operar de inmediato. Si necesitas migrar tu historial de datos, escríbenos y te ayudamos según el volumen."},
  {"question": "¿Puedo cambiar de plan más adelante?", "answer": "Sí, tu plan puede crecer junto con tu negocio; escríbenos cuando necesites más módulos o usuarios."},
  {"question": "¿Qué pasa si necesito más usuarios de los que incluye mi plan?", "answer": "Tu equipo actual sigue operando sin problema; para sumar usuarios por encima del límite de tu plan, actualízalo cuando lo necesites."},
  {"question": "¿Mis datos están seguros?", "answer": "Sí: aislamos los datos de cada empresa, ciframos las contraseñas y cada acción queda registrada. Puedes ver el detalle completo en Planes."}
]', usuario_modificacion = 'system', fecha_modificacion = now()
WHERE section_key = 'FAQ';
