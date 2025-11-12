-- =========================================================
-- Sistema de Estadísticas con Actualización Programada
-- Migración del modelo OLAP a tablas materializadas
-- Actualización cada 10 minutos vía pg_cron
-- =========================================================
BEGIN;

-- =======================
-- 1) Esquema
-- =======================
CREATE SCHEMA IF NOT EXISTS analiticas;

-- =======================
-- 2) Tablas de Estadísticas Materializadas
-- =======================

-- Tabla de control de actualizaciones
CREATE TABLE IF NOT EXISTS analiticas.stats_control (
  id                   SERIAL PRIMARY KEY,
  ultima_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
  duracion_ms          INTEGER,
  registros_procesados INTEGER,
  estado               TEXT CHECK (estado IN ('SUCCESS', 'ERROR', 'RUNNING')) DEFAULT 'SUCCESS',
  mensaje_error        TEXT
);

-- Estadística: Hechos por provincia y colección
CREATE TABLE IF NOT EXISTS analiticas.stats_hechos_por_provincia (
  coleccion_handle TEXT NOT NULL,
  provincia        TEXT NOT NULL,
  total            INTEGER NOT NULL DEFAULT 0,
  actualizado_at   TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (coleccion_handle, provincia)
);
CREATE INDEX IF NOT EXISTS idx_stats_provincia_coleccion ON analiticas.stats_hechos_por_provincia(coleccion_handle);
CREATE INDEX IF NOT EXISTS idx_stats_provincia_provincia ON analiticas.stats_hechos_por_provincia(provincia);

-- Estadística: Hechos por categoría
CREATE TABLE IF NOT EXISTS analiticas.stats_hechos_por_categoria (
  categoria      TEXT NOT NULL PRIMARY KEY,
  total          INTEGER NOT NULL DEFAULT 0,
  actualizado_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Estadística: Provincia por categoría
CREATE TABLE IF NOT EXISTS analiticas.stats_provincia_por_categoria (
  provincia      TEXT NOT NULL,
  categoria      TEXT NOT NULL,
  total          INTEGER NOT NULL DEFAULT 0,
  actualizado_at TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (provincia, categoria)
);
CREATE INDEX IF NOT EXISTS idx_stats_prov_cat_provincia ON analiticas.stats_provincia_por_categoria(provincia);
CREATE INDEX IF NOT EXISTS idx_stats_prov_cat_categoria ON analiticas.stats_provincia_por_categoria(categoria);

-- Estadística: Hora por categoría
CREATE TABLE IF NOT EXISTS analiticas.stats_hora_por_categoria (
  categoria      TEXT NOT NULL,
  hora           INTEGER NOT NULL CHECK (hora BETWEEN 0 AND 23),
  total          INTEGER NOT NULL DEFAULT 0,
  actualizado_at TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (categoria, hora)
);
CREATE INDEX IF NOT EXISTS idx_stats_hora_cat_categoria ON analiticas.stats_hora_por_categoria(categoria);
CREATE INDEX IF NOT EXISTS idx_stats_hora_cat_hora ON analiticas.stats_hora_por_categoria(hora);

-- Estadística: Solicitudes spam
CREATE TABLE IF NOT EXISTS analiticas.stats_solicitudes_spam (
  id             SERIAL PRIMARY KEY,
  spam_total     INTEGER NOT NULL DEFAULT 0,
  total          INTEGER NOT NULL DEFAULT 0,
  actualizado_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =======================
-- 3) Función auxiliar: Determinar provincia por coordenadas
-- =======================
CREATE OR REPLACE FUNCTION analiticas._get_provincia(
  p_lat DOUBLE PRECISION,
  p_lon DOUBLE PRECISION
) RETURNS TEXT LANGUAGE plpgsql IMMUTABLE AS $$
BEGIN
  -- Ciudad Autónoma de Buenos Aires (CABA)
  IF p_lat BETWEEN -34.7 AND -34.5 AND p_lon BETWEEN -58.5 AND -58.3 THEN
    RETURN 'Ciudad Autónoma de Buenos Aires';

  -- Buenos Aires
  ELSIF p_lat BETWEEN -39.0 AND -33.3 AND p_lon BETWEEN -63.4 AND -56.7 THEN
    RETURN 'Buenos Aires';

  -- Catamarca
  ELSIF p_lat BETWEEN -29.5 AND -25.2 AND p_lon BETWEEN -68.6 AND -65.0 THEN
    RETURN 'Catamarca';

  -- Chaco
  ELSIF p_lat BETWEEN -27.6 AND -24.1 AND p_lon BETWEEN -63.2 AND -58.0 THEN
    RETURN 'Chaco';

  -- Chubut
  ELSIF p_lat BETWEEN -46.0 AND -42.0 AND p_lon BETWEEN -71.6 AND -63.8 THEN
    RETURN 'Chubut';

  -- Córdoba
  ELSIF p_lat BETWEEN -35.0 AND -29.4 AND p_lon BETWEEN -65.5 AND -61.7 THEN
    RETURN 'Córdoba';

  -- Corrientes
  ELSIF p_lat BETWEEN -30.8 AND -27.2 AND p_lon BETWEEN -59.2 AND -55.6 THEN
    RETURN 'Corrientes';

  -- Entre Ríos
  ELSIF p_lat BETWEEN -34.0 AND -30.1 AND p_lon BETWEEN -60.7 AND -57.8 THEN
    RETURN 'Entre Ríos';

  -- Formosa
  ELSIF p_lat BETWEEN -26.2 AND -22.9 AND p_lon BETWEEN -62.5 AND -57.6 THEN
    RETURN 'Formosa';

  -- Jujuy
  ELSIF p_lat BETWEEN -24.6 AND -21.8 AND p_lon BETWEEN -67.2 AND -64.1 THEN
    RETURN 'Jujuy';

  -- La Pampa
  ELSIF p_lat BETWEEN -39.0 AND -35.0 AND p_lon BETWEEN -68.4 AND -63.3 THEN
    RETURN 'La Pampa';

  -- La Rioja
  ELSIF p_lat BETWEEN -31.6 AND -28.0 AND p_lon BETWEEN -70.0 AND -65.9 THEN
    RETURN 'La Rioja';

  -- Mendoza
  ELSIF p_lat BETWEEN -37.6 AND -32.0 AND p_lon BETWEEN -70.6 AND -66.2 THEN
    RETURN 'Mendoza';

  -- Misiones
  ELSIF p_lat BETWEEN -28.2 AND -25.3 AND p_lon BETWEEN -56.2 AND -53.6 THEN
    RETURN 'Misiones';

  -- Neuquén
  ELSIF p_lat BETWEEN -41.0 AND -36.0 AND p_lon BETWEEN -71.9 AND -68.1 THEN
    RETURN 'Neuquén';

  -- Río Negro
  ELSIF p_lat BETWEEN -42.0 AND -37.5 AND p_lon BETWEEN -71.9 AND -62.8 THEN
    RETURN 'Río Negro';

  -- Salta
  ELSIF p_lat BETWEEN -25.8 AND -22.0 AND p_lon BETWEEN -67.8 AND -62.4 THEN
    RETURN 'Salta';

  -- San Juan
  ELSIF p_lat BETWEEN -32.6 AND -28.8 AND p_lon BETWEEN -70.2 AND -66.9 THEN
    RETURN 'San Juan';

  -- San Luis
  ELSIF p_lat BETWEEN -36.0 AND -32.1 AND p_lon BETWEEN -67.0 AND -64.9 THEN
    RETURN 'San Luis';

  -- Santa Cruz
  ELSIF p_lat BETWEEN -52.4 AND -46.0 AND p_lon BETWEEN -73.6 AND -65.9 THEN
    RETURN 'Santa Cruz';

  -- Santa Fe
  ELSIF p_lat BETWEEN -34.0 AND -28.0 AND p_lon BETWEEN -62.9 AND -59.2 THEN
    RETURN 'Santa Fe';

  -- Santiago del Estero
  ELSIF p_lat BETWEEN -30.3 AND -25.4 AND p_lon BETWEEN -65.5 AND -61.0 THEN
    RETURN 'Santiago del Estero';

  -- Tierra del Fuego
  ELSIF p_lat BETWEEN -55.1 AND -52.6 AND p_lon BETWEEN -68.4 AND -63.8 THEN
    RETURN 'Tierra del Fuego';

  -- Tucumán
  ELSIF p_lat BETWEEN -28.0 AND -26.1 AND p_lon BETWEEN -66.3 AND -64.3 THEN
    RETURN 'Tucumán';

  -- Fuera de Argentina o no identificado
  ELSE
    RETURN 'Otra';
  END IF;
END; $$;

-- =======================
-- 4) Procedimiento Principal de Actualización
-- =======================
CREATE OR REPLACE FUNCTION analiticas.actualizar_estadisticas()
RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
  v_start_time   TIMESTAMP;
  v_end_time     TIMESTAMP;
  v_duracion_ms  INTEGER;
  v_registros    INTEGER := 0;
  v_error_msg    TEXT;
  v_control_id   INTEGER;
BEGIN
  v_start_time := clock_timestamp();

  -- Registrar inicio de ejecución
  INSERT INTO analiticas.stats_control(ultima_actualizacion, estado)
  VALUES (v_start_time, 'RUNNING')
  RETURNING id INTO v_control_id;

  BEGIN
    -- ==========================================
    -- 4.1) Actualizar stats_hechos_por_provincia
    -- ==========================================
    DELETE FROM analiticas.stats_hechos_por_provincia;

    INSERT INTO analiticas.stats_hechos_por_provincia (coleccion_handle, provincia, total, actualizado_at)
    SELECT
      ch.coleccion_handle,
      analiticas._get_provincia(h.latitud, h.longitud) AS provincia,
      COUNT(*)::INTEGER AS total,
      v_start_time
    FROM public.coleccion_hechos ch
    JOIN public.hechos h ON h.uuid = ch.hecho_uuid
    WHERE h.latitud IS NOT NULL
      AND h.longitud IS NOT NULL
      AND h.estado = 'ACTIVO'
    GROUP BY ch.coleccion_handle, analiticas._get_provincia(h.latitud, h.longitud);

    GET DIAGNOSTICS v_registros = ROW_COUNT;

    -- ==========================================
    -- 4.2) Actualizar stats_hechos_por_categoria
    -- ==========================================
    DELETE FROM analiticas.stats_hechos_por_categoria;

    INSERT INTO analiticas.stats_hechos_por_categoria (categoria, total, actualizado_at)
    SELECT
      h.categoria,
      COUNT(*)::INTEGER AS total,
      v_start_time
    FROM public.hechos h
    WHERE h.estado = 'ACTIVO'
      AND h.categoria IS NOT NULL
    GROUP BY h.categoria;

    -- ==========================================
    -- 4.3) Actualizar stats_provincia_por_categoria
    -- ==========================================
    DELETE FROM analiticas.stats_provincia_por_categoria;

    INSERT INTO analiticas.stats_provincia_por_categoria (provincia, categoria, total, actualizado_at)
    SELECT
      analiticas._get_provincia(h.latitud, h.longitud) AS provincia,
      h.categoria,
      COUNT(*)::INTEGER AS total,
      v_start_time
    FROM public.hechos h
    WHERE h.latitud IS NOT NULL
      AND h.longitud IS NOT NULL
      AND h.categoria IS NOT NULL
      AND h.estado = 'ACTIVO'
    GROUP BY analiticas._get_provincia(h.latitud, h.longitud), h.categoria;

    -- ==========================================
    -- 4.4) Actualizar stats_hora_por_categoria
    -- ==========================================
    DELETE FROM analiticas.stats_hora_por_categoria;

    INSERT INTO analiticas.stats_hora_por_categoria (categoria, hora, total, actualizado_at)
    SELECT
      h.categoria,
      EXTRACT(HOUR FROM h.fecha_carga)::INTEGER AS hora,
      COUNT(*)::INTEGER AS total,
      v_start_time
    FROM public.hechos h
    WHERE h.categoria IS NOT NULL
      AND h.fecha_carga IS NOT NULL
      AND h.estado = 'ACTIVO'
    GROUP BY h.categoria, EXTRACT(HOUR FROM h.fecha_carga)::INTEGER;

    -- ==========================================
    -- 4.5) Actualizar stats_solicitudes_spam
    -- ==========================================
    DELETE FROM analiticas.stats_solicitudes_spam;

    INSERT INTO analiticas.stats_solicitudes_spam (spam_total, total, actualizado_at)
    SELECT
      COUNT(*) FILTER (WHERE estado = 'SPAM')::INTEGER AS spam_total,
      COUNT(*)::INTEGER AS total,
      v_start_time
    FROM public.solicitudes_eliminacion;

    -- ==========================================
    -- Registrar éxito
    -- ==========================================
    v_end_time := clock_timestamp();
    v_duracion_ms := EXTRACT(MILLISECONDS FROM (v_end_time - v_start_time))::INTEGER;

    UPDATE analiticas.stats_control
    SET estado = 'SUCCESS',
        duracion_ms = v_duracion_ms,
        registros_procesados = v_registros,
        ultima_actualizacion = v_end_time
    WHERE id = v_control_id;

    RAISE NOTICE 'Estadísticas actualizadas exitosamente en % ms (% registros)', v_duracion_ms, v_registros;

  EXCEPTION WHEN OTHERS THEN
    -- Registrar error
    v_error_msg := SQLERRM;
    v_end_time := clock_timestamp();
    v_duracion_ms := EXTRACT(MILLISECONDS FROM (v_end_time - v_start_time))::INTEGER;

    UPDATE analiticas.stats_control
    SET estado = 'ERROR',
        duracion_ms = v_duracion_ms,
        mensaje_error = v_error_msg,
        ultima_actualizacion = v_end_time
    WHERE id = v_control_id;

    RAISE WARNING 'Error al actualizar estadísticas: %', v_error_msg;
  END;
END; $$;

-- =======================
-- 5) Vistas
-- =======================
CREATE OR REPLACE VIEW analiticas.agg_hechos_por_provincia AS
SELECT coleccion_handle, provincia, total
FROM analiticas.stats_hechos_por_provincia;

CREATE OR REPLACE VIEW analiticas.agg_hechos_por_categoria AS
SELECT categoria, total
FROM analiticas.stats_hechos_por_categoria;

CREATE OR REPLACE VIEW analiticas.agg_provincia_por_categoria AS
SELECT provincia, categoria, total
FROM analiticas.stats_provincia_por_categoria;

CREATE OR REPLACE VIEW analiticas.agg_hora_por_categoria AS
SELECT categoria, hora, total
FROM analiticas.stats_hora_por_categoria;

CREATE OR REPLACE VIEW analiticas.agg_solicitudes_spam AS
SELECT spam_total, total
FROM analiticas.stats_solicitudes_spam
ORDER BY id DESC
LIMIT 1;

-- =======================
-- 6) Ejecutar primera actualización
-- =======================
SELECT analiticas.actualizar_estadisticas();

COMMIT;