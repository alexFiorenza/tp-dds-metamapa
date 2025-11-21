-- ÍNDICES DE RENDIMIENTO - metamapa_db

-- Tabla: Hecho
CREATE INDEX IF NOT EXISTS idx_hecho_estado ON Hecho(estado);
CREATE INDEX IF NOT EXISTS idx_hecho_origen ON Hecho(origen);
CREATE INDEX IF NOT EXISTS idx_hecho_duplicados ON Hecho(LOWER(TRIM(titulo)), latitud, longitud, fechaAcontecimiento);
CREATE INDEX IF NOT EXISTS idx_hecho_titulo_lower ON Hecho(LOWER(TRIM(titulo)));
CREATE INDEX IF NOT EXISTS idx_hecho_categoria ON Hecho(categoria);
CREATE INDEX IF NOT EXISTS idx_hecho_fecha_acontecimiento ON Hecho(fechaAcontecimiento);
CREATE INDEX IF NOT EXISTS idx_hecho_fecha_carga ON Hecho(fechaCarga DESC);
CREATE INDEX IF NOT EXISTS idx_hecho_ubicacion ON Hecho(latitud, longitud);

-- Tabla: agregacion_job
CREATE INDEX IF NOT EXISTS idx_agregacion_job_estado ON agregacion_job(estado);
CREATE INDEX IF NOT EXISTS idx_agregacion_job_fecha_inicio ON agregacion_job(fecha_inicio DESC);
CREATE INDEX IF NOT EXISTS idx_agregacion_job_estado_fecha ON agregacion_job(estado, fecha_inicio DESC);

-- Tabla: Contribuyente
CREATE INDEX IF NOT EXISTS idx_contribuyente_user_id ON Contribuyente(userId);

-- Tabla: Fuente
CREATE INDEX IF NOT EXISTS idx_fuente_host ON Fuente(host);
CREATE INDEX IF NOT EXISTS idx_fuente_tipo ON Fuente(tipo);

-- RelacionesMany-to-Many
CREATE INDEX IF NOT EXISTS idx_coleccion_hechos_coleccion ON coleccion_hechos(coleccion_handle);
CREATE INDEX IF NOT EXISTS idx_coleccion_hechos_hecho ON coleccion_hechos(hechos_uuid);
CREATE INDEX IF NOT EXISTS idx_coleccion_fuentes_coleccion ON coleccion_fuentes(coleccion_handle);
CREATE INDEX IF NOT EXISTS idx_coleccion_fuentes_fuente ON coleccion_fuentes(fuentes_uuid);

-- Etiquetas
CREATE INDEX IF NOT EXISTS idx_hecho_etiquetas_hecho ON hecho_etiquetas(hecho_uuid);

-- Actualizar estadísticas
ANALYZE;
