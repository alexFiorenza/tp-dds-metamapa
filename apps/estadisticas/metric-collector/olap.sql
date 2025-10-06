-- =========================================================
-- OLAP / Analíticas - Modelo Estrella (idempotente)
-- =========================================================
BEGIN;

CREATE SCHEMA IF NOT EXISTS analiticas;

-- =========================================================
-- DIMENSIONES
-- =========================================================

-- Dimensión Ubicación (con provincia)
CREATE TABLE IF NOT EXISTS analiticas.dim_ubicacion (
  ubicacion_id SERIAL PRIMARY KEY,
  provincia    text NOT NULL,
  latitud      double precision NOT NULL,
  longitud     double precision NOT NULL,
  UNIQUE (provincia, latitud, longitud)
);

-- Dimensión Categoría
CREATE TABLE IF NOT EXISTS analiticas.dim_categoria (
  categoria_id SERIAL PRIMARY KEY,
  categoria    text NOT NULL UNIQUE
);

-- Dimensión Tiempo
CREATE TABLE IF NOT EXISTS analiticas.dim_tiempo (
  tiempo_id SERIAL PRIMARY KEY,
  hora      integer NOT NULL CHECK (hora BETWEEN 0 AND 23),
  dia       integer,
  mes       integer,
  anio      integer,
  UNIQUE (hora, dia, mes, anio)
);

-- Dimensión Colección (opcional, para filtrado)
CREATE TABLE IF NOT EXISTS analiticas.dim_coleccion (
  coleccion_id     SERIAL PRIMARY KEY,
  coleccion_handle text NOT NULL UNIQUE
);

-- =========================================================
-- TABLAS DE HECHOS (FACT TABLES)
-- =========================================================

-- Tabla de Hechos Principal
CREATE TABLE IF NOT EXISTS analiticas.fact_hechos (
  hecho_uuid       text NOT NULL,
  ubicacion_id     integer REFERENCES analiticas.dim_ubicacion(ubicacion_id),
  categoria_id     integer REFERENCES analiticas.dim_categoria(categoria_id),
  tiempo_id        integer REFERENCES analiticas.dim_tiempo(tiempo_id),
  coleccion_id     integer REFERENCES analiticas.dim_coleccion(coleccion_id),
  PRIMARY KEY (hecho_uuid, coleccion_id)
);

-- =========================================================
-- TABLAS AGREGADAS (Pre-calculadas para Grafana)
-- =========================================================

-- Agregado: Hechos por provincia y colección
CREATE TABLE IF NOT EXISTS analiticas.agg_hechos_por_provincia (
  coleccion_handle text NOT NULL,
  provincia        text NOT NULL,
  total            integer NOT NULL DEFAULT 0,
  PRIMARY KEY (coleccion_handle, provincia)
);

-- Agregado: Hechos por categoría (global)
CREATE TABLE IF NOT EXISTS analiticas.agg_hechos_por_categoria (
  categoria text NOT NULL PRIMARY KEY,
  total     integer NOT NULL DEFAULT 0
);

-- Agregado: Provincia por categoría
CREATE TABLE IF NOT EXISTS analiticas.agg_provincia_por_categoria (
  provincia text NOT NULL,
  categoria text NOT NULL,
  total     integer NOT NULL DEFAULT 0,
  PRIMARY KEY (provincia, categoria)
);

-- Agregado: Hora del día por categoría
CREATE TABLE IF NOT EXISTS analiticas.agg_hora_por_categoria (
  categoria text NOT NULL,
  hora      integer NOT NULL CHECK (hora BETWEEN 0 AND 23),
  total     integer NOT NULL DEFAULT 0,
  PRIMARY KEY (categoria, hora)
);

-- Agregado: Solicitudes de eliminación (spam)
CREATE TABLE IF NOT EXISTS analiticas.agg_solicitudes_spam (
  id         boolean PRIMARY KEY DEFAULT true,
  spam_total integer NOT NULL DEFAULT 0,
  total      integer NOT NULL DEFAULT 0
);
INSERT INTO analiticas.agg_solicitudes_spam(id) VALUES(true)
ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- ÍNDICES
-- =========================================================
CREATE INDEX IF NOT EXISTS ix_fact_hechos_ubicacion ON analiticas.fact_hechos(ubicacion_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_categoria ON analiticas.fact_hechos(categoria_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_tiempo ON analiticas.fact_hechos(tiempo_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_coleccion ON analiticas.fact_hechos(coleccion_id);

CREATE INDEX IF NOT EXISTS ix_ch_hecho_uuid ON public.coleccion_hechos(hecho_uuid);
CREATE INDEX IF NOT EXISTS ix_ch_coleccion ON public.coleccion_hechos(coleccion_handle);
CREATE INDEX IF NOT EXISTS ix_h_uuid ON public.hechos(uuid);

-- =========================================================
-- FUNCIONES AUXILIARES
-- =========================================================

-- Función para obtener o crear provincia desde lat/lon
CREATE OR REPLACE FUNCTION analiticas._get_provincia(
  p_lat double precision,
  p_lon double precision
) RETURNS text LANGUAGE plpgsql AS $$
BEGIN
  -- Lógica simplificada: mapear coordenadas a provincias argentinas
  -- Ajustar según tus necesidades reales (puede usar PostGIS o tabla de mapeo)

  -- Buenos Aires: lat entre -35 y -33
  IF p_lat BETWEEN -35 AND -33 AND p_lon BETWEEN -59 AND -57 THEN
    RETURN 'Buenos Aires';
  -- Córdoba: lat entre -32 y -30
  ELSIF p_lat BETWEEN -32 AND -30 AND p_lon BETWEEN -65 AND -63 THEN
    RETURN 'Córdoba';
  -- Santa Fe: lat entre -34 and -28
  ELSIF p_lat BETWEEN -34 AND -28 AND p_lon BETWEEN -62 AND -60 THEN
    RETURN 'Santa Fe';
  -- Mendoza: lat entre -34 and -32
  ELSIF p_lat BETWEEN -34 AND -32 AND p_lon BETWEEN -69 AND -68 THEN
    RETURN 'Mendoza';
  ELSE
    RETURN 'Otra';
  END IF;
END;
$$;

-- Función para obtener o crear ubicacion_id
CREATE OR REPLACE FUNCTION analiticas._get_ubicacion_id(
  p_lat double precision,
  p_lon double precision
) RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
  v_provincia text;
  v_ubicacion_id integer;
BEGIN
  IF p_lat IS NULL OR p_lon IS NULL THEN
    RETURN NULL;
  END IF;

  v_provincia := analiticas._get_provincia(p_lat, p_lon);

  INSERT INTO analiticas.dim_ubicacion(provincia, latitud, longitud)
  VALUES (v_provincia, p_lat, p_lon)
  ON CONFLICT (provincia, latitud, longitud) DO NOTHING;

  SELECT ubicacion_id INTO v_ubicacion_id
  FROM analiticas.dim_ubicacion
  WHERE provincia = v_provincia AND latitud = p_lat AND longitud = p_lon;

  RETURN v_ubicacion_id;
END;
$$;

-- Función para obtener o crear categoria_id
CREATE OR REPLACE FUNCTION analiticas._get_categoria_id(p_cat text)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
  v_categoria_id integer;
BEGIN
  IF p_cat IS NULL THEN
    RETURN NULL;
  END IF;

  INSERT INTO analiticas.dim_categoria(categoria)
  VALUES (p_cat)
  ON CONFLICT (categoria) DO NOTHING;

  SELECT categoria_id INTO v_categoria_id
  FROM analiticas.dim_categoria
  WHERE categoria = p_cat;

  RETURN v_categoria_id;
END;
$$;

-- Función para obtener o crear tiempo_id
CREATE OR REPLACE FUNCTION analiticas._get_tiempo_id(p_fecha timestamp)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
  v_hora integer;
  v_dia integer;
  v_mes integer;
  v_anio integer;
  v_tiempo_id integer;
BEGIN
  IF p_fecha IS NULL THEN
    RETURN NULL;
  END IF;

  v_hora := EXTRACT(HOUR FROM p_fecha)::int;
  v_dia := EXTRACT(DAY FROM p_fecha)::int;
  v_mes := EXTRACT(MONTH FROM p_fecha)::int;
  v_anio := EXTRACT(YEAR FROM p_fecha)::int;

  INSERT INTO analiticas.dim_tiempo(hora, dia, mes, anio)
  VALUES (v_hora, v_dia, v_mes, v_anio)
  ON CONFLICT (hora, dia, mes, anio) DO NOTHING;

  SELECT tiempo_id INTO v_tiempo_id
  FROM analiticas.dim_tiempo
  WHERE hora = v_hora AND dia = v_dia AND mes = v_mes AND anio = v_anio;

  RETURN v_tiempo_id;
END;
$$;

-- Función para obtener o crear coleccion_id
CREATE OR REPLACE FUNCTION analiticas._get_coleccion_id(p_col_handle text)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
  v_coleccion_id integer;
BEGIN
  IF p_col_handle IS NULL THEN
    RETURN NULL;
  END IF;

  INSERT INTO analiticas.dim_coleccion(coleccion_handle)
  VALUES (p_col_handle)
  ON CONFLICT (coleccion_handle) DO NOTHING;

  SELECT coleccion_id INTO v_coleccion_id
  FROM analiticas.dim_coleccion
  WHERE coleccion_handle = p_col_handle;

  RETURN v_coleccion_id;
END;
$$;

-- =========================================================
-- FUNCIONES DE INCREMENTO/DECREMENTO
-- =========================================================

-- Incrementar hechos por provincia
CREATE OR REPLACE FUNCTION analiticas._inc_hechos_por_provincia(
  p_col text, p_provincia text, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_provincia IS NULL THEN
    RETURN;
  END IF;

  INSERT INTO analiticas.agg_hechos_por_provincia(coleccion_handle, provincia, total)
  VALUES (p_col, p_provincia, GREATEST(p_delta, 0))
  ON CONFLICT (coleccion_handle, provincia) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_hechos_por_provincia.total + p_delta);
END;
$$;

-- Incrementar hechos por categoría (global)
CREATE OR REPLACE FUNCTION analiticas._inc_hechos_por_categoria(
  p_cat text, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_cat IS NULL THEN
    RETURN;
  END IF;

  INSERT INTO analiticas.agg_hechos_por_categoria(categoria, total)
  VALUES (p_cat, GREATEST(p_delta, 0))
  ON CONFLICT (categoria) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_hechos_por_categoria.total + p_delta);
END;
$$;

-- Incrementar provincia por categoría
CREATE OR REPLACE FUNCTION analiticas._inc_provincia_por_categoria(
  p_provincia text, p_cat text, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_provincia IS NULL OR p_cat IS NULL THEN
    RETURN;
  END IF;

  INSERT INTO analiticas.agg_provincia_por_categoria(provincia, categoria, total)
  VALUES (p_provincia, p_cat, GREATEST(p_delta, 0))
  ON CONFLICT (provincia, categoria) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_provincia_por_categoria.total + p_delta);
END;
$$;

-- Incrementar hora por categoría
CREATE OR REPLACE FUNCTION analiticas._inc_hora_por_categoria(
  p_cat text, p_hora int, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_cat IS NULL OR p_hora IS NULL THEN
    RETURN;
  END IF;

  INSERT INTO analiticas.agg_hora_por_categoria(categoria, hora, total)
  VALUES (p_cat, p_hora, GREATEST(p_delta, 0))
  ON CONFLICT (categoria, hora) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_hora_por_categoria.total + p_delta);
END;
$$;

-- Incrementar solicitudes spam
CREATE OR REPLACE FUNCTION analiticas._inc_solicitudes_spam(
  p_is_spam boolean, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  UPDATE analiticas.agg_solicitudes_spam
  SET total = GREATEST(0, total + p_delta),
      spam_total = GREATEST(0, spam_total + (CASE WHEN p_is_spam THEN p_delta ELSE 0 END));
END;
$$;

-- =========================================================
-- TRIGGERS
-- =========================================================

-- Trigger: INSERT en coleccion_hechos
CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_lat double precision;
  v_lon double precision;
  v_cat text;
  v_fecha timestamp;
  v_hora int;
  v_provincia text;
  v_ubicacion_id int;
  v_categoria_id int;
  v_tiempo_id int;
  v_coleccion_id int;
BEGIN
  -- Obtener datos del hecho
  SELECT latitud, longitud, categoria, fecha_carga
    INTO v_lat, v_lon, v_cat, v_fecha
  FROM public.hechos
  WHERE uuid = NEW.hecho_uuid;

  IF v_lat IS NULL OR v_lon IS NULL THEN
    RETURN NEW;
  END IF;

  v_provincia := analiticas._get_provincia(v_lat, v_lon);
  v_hora := EXTRACT(HOUR FROM v_fecha)::int;

  -- Insertar en dimensiones y obtener IDs
  v_ubicacion_id := analiticas._get_ubicacion_id(v_lat, v_lon);
  v_categoria_id := analiticas._get_categoria_id(v_cat);
  v_tiempo_id := analiticas._get_tiempo_id(v_fecha);
  v_coleccion_id := analiticas._get_coleccion_id(NEW.coleccion_handle);

  -- Insertar en tabla de hechos
  INSERT INTO analiticas.fact_hechos(hecho_uuid, ubicacion_id, categoria_id, tiempo_id, coleccion_id)
  VALUES (NEW.hecho_uuid, v_ubicacion_id, v_categoria_id, v_tiempo_id, v_coleccion_id)
  ON CONFLICT (hecho_uuid, coleccion_id) DO NOTHING;

  -- Actualizar agregados
  PERFORM analiticas._inc_hechos_por_provincia(NEW.coleccion_handle, v_provincia, +1);
  PERFORM analiticas._inc_hechos_por_categoria(v_cat, +1);
  PERFORM analiticas._inc_provincia_por_categoria(v_provincia, v_cat, +1);
  PERFORM analiticas._inc_hora_por_categoria(v_cat, v_hora, +1);

  RETURN NEW;
END;
$$;

-- Trigger: DELETE en coleccion_hechos
CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_del()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_lat double precision;
  v_lon double precision;
  v_cat text;
  v_fecha timestamp;
  v_hora int;
  v_provincia text;
BEGIN
  -- Obtener datos del hecho
  SELECT latitud, longitud, categoria, fecha_carga
    INTO v_lat, v_lon, v_cat, v_fecha
  FROM public.hechos
  WHERE uuid = OLD.hecho_uuid;

  IF v_lat IS NULL OR v_lon IS NULL THEN
    RETURN OLD;
  END IF;

  v_provincia := analiticas._get_provincia(v_lat, v_lon);
  v_hora := EXTRACT(HOUR FROM v_fecha)::int;

  -- Eliminar de tabla de hechos
  DELETE FROM analiticas.fact_hechos
  WHERE hecho_uuid = OLD.hecho_uuid
    AND coleccion_id = (SELECT coleccion_id FROM analiticas.dim_coleccion WHERE coleccion_handle = OLD.coleccion_handle);

  -- Actualizar agregados
  PERFORM analiticas._inc_hechos_por_provincia(OLD.coleccion_handle, v_provincia, -1);
  PERFORM analiticas._inc_hechos_por_categoria(v_cat, -1);
  PERFORM analiticas._inc_provincia_por_categoria(v_provincia, v_cat, -1);
  PERFORM analiticas._inc_hora_por_categoria(v_cat, v_hora, -1);

  RETURN OLD;
END;
$$;

-- Trigger: UPDATE en hechos (cambios relevantes)
CREATE OR REPLACE FUNCTION analiticas.trg_hechos_upd()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_provincia_old text;
  v_provincia_new text;
  v_hora_old int;
  v_hora_new int;
  r record;
BEGIN
  IF (OLD.latitud IS DISTINCT FROM NEW.latitud)
     OR (OLD.longitud IS DISTINCT FROM NEW.longitud)
     OR (OLD.categoria IS DISTINCT FROM NEW.categoria)
     OR (OLD.fecha_carga IS DISTINCT FROM NEW.fecha_carga) THEN

    v_provincia_old := analiticas._get_provincia(OLD.latitud, OLD.longitud);
    v_provincia_new := analiticas._get_provincia(NEW.latitud, NEW.longitud);
    v_hora_old := EXTRACT(HOUR FROM OLD.fecha_carga)::int;
    v_hora_new := EXTRACT(HOUR FROM NEW.fecha_carga)::int;

    -- Actualizar para cada colección que contiene este hecho
    FOR r IN
      SELECT coleccion_handle
      FROM public.coleccion_hechos
      WHERE hecho_uuid = NEW.uuid
    LOOP
      -- Restar OLD
      IF OLD.latitud IS NOT NULL AND OLD.longitud IS NOT NULL THEN
        PERFORM analiticas._inc_hechos_por_provincia(r.coleccion_handle, v_provincia_old, -1);
        PERFORM analiticas._inc_hechos_por_categoria(OLD.categoria, -1);
        PERFORM analiticas._inc_provincia_por_categoria(v_provincia_old, OLD.categoria, -1);
        PERFORM analiticas._inc_hora_por_categoria(OLD.categoria, v_hora_old, -1);
      END IF;

      -- Sumar NEW
      IF NEW.latitud IS NOT NULL AND NEW.longitud IS NOT NULL THEN
        PERFORM analiticas._inc_hechos_por_provincia(r.coleccion_handle, v_provincia_new, +1);
        PERFORM analiticas._inc_hechos_por_categoria(NEW.categoria, +1);
        PERFORM analiticas._inc_provincia_por_categoria(v_provincia_new, NEW.categoria, +1);
        PERFORM analiticas._inc_hora_por_categoria(NEW.categoria, v_hora_new, +1);
      END IF;
    END LOOP;
  END IF;

  RETURN NEW;
END;
$$;

-- Trigger: INSERT en solicitudes_eliminacion
CREATE OR REPLACE FUNCTION analiticas.trg_sol_elim_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  PERFORM analiticas._inc_solicitudes_spam(NEW.estado = 'SPAM', +1);
  RETURN NEW;
END;
$$;

-- Trigger: DELETE en solicitudes_eliminacion
CREATE OR REPLACE FUNCTION analiticas.trg_sol_elim_del()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  PERFORM analiticas._inc_solicitudes_spam(OLD.estado = 'SPAM', -1);
  RETURN OLD;
END;
$$;

-- =========================================================
-- REGISTRAR TRIGGERS
-- =========================================================
DROP TRIGGER IF EXISTS trg_ch_ins ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_ins
AFTER INSERT ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_ins();

DROP TRIGGER IF EXISTS trg_ch_del ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_del
AFTER DELETE ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_del();

DROP TRIGGER IF EXISTS trg_hechos_upd ON public.hechos;
CREATE TRIGGER trg_hechos_upd
AFTER UPDATE OF latitud, longitud, categoria, fecha_carga ON public.hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_hechos_upd();

DROP TRIGGER IF EXISTS trg_selim_ins ON public.solicitudes_eliminacion;
CREATE TRIGGER trg_selim_ins
AFTER INSERT ON public.solicitudes_eliminacion
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_sol_elim_ins();

DROP TRIGGER IF EXISTS trg_selim_del ON public.solicitudes_eliminacion;
CREATE TRIGGER trg_selim_del
AFTER DELETE ON public.solicitudes_eliminacion
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_sol_elim_del();

-- =========================================================
-- FUNCIÓN REBUILD
-- =========================================================
CREATE OR REPLACE FUNCTION analiticas.rebuild_all() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  v_lat double precision;
  v_lon double precision;
  v_cat text;
  v_fecha timestamp;
  v_provincia text;
  v_hora int;
  v_ubicacion_id int;
  v_categoria_id int;
  v_tiempo_id int;
  v_coleccion_id int;
BEGIN
  -- Limpiar tablas
  TRUNCATE analiticas.fact_hechos;
  TRUNCATE analiticas.dim_ubicacion RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_categoria RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_tiempo RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_coleccion RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.agg_hechos_por_provincia;
  TRUNCATE analiticas.agg_hechos_por_categoria;
  TRUNCATE analiticas.agg_provincia_por_categoria;
  TRUNCATE analiticas.agg_hora_por_categoria;
  UPDATE analiticas.agg_solicitudes_spam SET spam_total = 0, total = 0;

  -- Reconstruir desde coleccion_hechos
  FOR v_lat, v_lon, v_cat, v_fecha, v_coleccion_id IN
    SELECT h.latitud, h.longitud, h.categoria, h.fecha_carga, ch.coleccion_handle
    FROM public.coleccion_hechos ch
    JOIN public.hechos h ON h.uuid = ch.hecho_uuid
    WHERE h.latitud IS NOT NULL AND h.longitud IS NOT NULL
  LOOP
    v_provincia := analiticas._get_provincia(v_lat, v_lon);
    v_hora := EXTRACT(HOUR FROM v_fecha)::int;

    -- Insertar en dimensiones
    v_ubicacion_id := analiticas._get_ubicacion_id(v_lat, v_lon);
    v_categoria_id := analiticas._get_categoria_id(v_cat);
    v_tiempo_id := analiticas._get_tiempo_id(v_fecha);
    v_coleccion_id := analiticas._get_coleccion_id(v_coleccion_id);

    -- Actualizar agregados
    PERFORM analiticas._inc_hechos_por_provincia(v_coleccion_id, v_provincia, +1);
    PERFORM analiticas._inc_hechos_por_categoria(v_cat, +1);
    PERFORM analiticas._inc_provincia_por_categoria(v_provincia, v_cat, +1);
    PERFORM analiticas._inc_hora_por_categoria(v_cat, v_hora, +1);
  END LOOP;

  -- Reconstruir solicitudes spam
  UPDATE analiticas.agg_solicitudes_spam
  SET total = s.total,
      spam_total = s.spam_total
  FROM (
    SELECT COUNT(*) AS total,
           COUNT(*) FILTER (WHERE estado = 'SPAM') AS spam_total
    FROM public.solicitudes_eliminacion
  ) s;
END;
$$;

COMMIT;
