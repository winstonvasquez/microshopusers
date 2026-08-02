-- El editor de texto enriquecido del frontend ahora guarda HTML (negritas, listas, etc.)
-- en lugar de texto plano, por lo que estas columnas VARCHAR quedaban demasiado cortas
-- y el HTML las desbordaba, provocando DataException (500 al guardar). Se amplían a TEXT.

ALTER TABLE attendance ALTER COLUMN observaciones TYPE TEXT;

ALTER TABLE vacation_request ALTER COLUMN motivo TYPE TEXT;

ALTER TABLE employee ALTER COLUMN motivo_salida TYPE TEXT;
