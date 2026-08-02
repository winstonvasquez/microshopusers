-- El editor de texto enriquecido del frontend ahora guarda HTML (negritas, listas, etc.)
-- en el campo de comentarios de aprobación/rechazo de vacaciones, así que
-- vacation_request.comentarios_aprobacion (VARCHAR(500)) quedaba demasiado corta y el
-- HTML la desbordaba, provocando DataException (500 al guardar). Se amplía a TEXT.

ALTER TABLE vacation_request ALTER COLUMN comentarios_aprobacion TYPE TEXT;
