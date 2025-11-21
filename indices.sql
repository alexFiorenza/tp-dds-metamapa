CREATE INDEX IF NOT EXISTS idx_hecho_duplicados ON hechos(LOWER(TRIM(titulo)), latitud, longitud, fecha_acontecimiento);
CREATE INDEX IF NOT EXISTS idx_contribuyente_user_id ON contribuyentes(user_id);
CREATE INDEX IF NOT EXISTS idx_hecho_estado ON hechos(estado);
CREATE INDEX IF NOT EXISTS idx_agregacion_job_estado_fecha ON agregacion_job(estado, fecha_inicio DESC);

ANALYZE;
