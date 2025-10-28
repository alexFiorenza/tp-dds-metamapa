-- =========================================================
-- OLAP / Analíticas — Modelo Estrella + Views
-- =========================================================
BEGIN;

-- =======================
-- 1) Esquema
-- =======================
CREATE SCHEMA IF NOT EXISTS analiticas;

-- =======================
-- 2) Dimensiones
-- =======================
CREATE TABLE IF NOT EXISTS analiticas.dim_ubicacion (
  ubicacion_id SERIAL PRIMARY KEY,
  provincia    text NOT NULL,
  latitud      double precision NOT NULL,
  longitud     double precision NOT NULL,
  UNIQUE (provincia, latitud, longitud)
);

CREATE TABLE IF NOT EXISTS analiticas.dim_categoria (
  categoria_id SERIAL PRIMARY KEY,
  categoria    text NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS analiticas.dim_tiempo (
  tiempo_id SERIAL PRIMARY KEY,
  hora      integer NOT NULL CHECK (hora BETWEEN 0 AND 23),
  dia       integer,
  mes       integer,
  anio      integer,
  UNIQUE (hora, dia, mes, anio)
);

CREATE TABLE IF NOT EXISTS analiticas.dim_coleccion (
  coleccion_id     SERIAL PRIMARY KEY,
  coleccion_handle text NOT NULL UNIQUE
);

-- =======================
-- 3) Tabla de Hechos
-- =======================
CREATE TABLE IF NOT EXISTS analiticas.fact_hechos (
  hecho_uuid   text NOT NULL,
  ubicacion_id integer REFERENCES analiticas.dim_ubicacion(ubicacion_id),
  categoria_id integer REFERENCES analiticas.dim_categoria(categoria_id),
  tiempo_id    integer REFERENCES analiticas.dim_tiempo(tiempo_id),
  coleccion_id integer REFERENCES analiticas.dim_coleccion(coleccion_id),
  PRIMARY KEY (hecho_uuid, coleccion_id)
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS ix_fact_hechos_ubicacion ON analiticas.fact_hechos(ubicacion_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_categoria ON analiticas.fact_hechos(categoria_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_tiempo    ON analiticas.fact_hechos(tiempo_id);
CREATE INDEX IF NOT EXISTS ix_fact_hechos_coleccion ON analiticas.fact_hechos(coleccion_id);
CREATE INDEX IF NOT EXISTS ix_fact_coleccion_ubicacion ON analiticas.fact_hechos(coleccion_id, ubicacion_id);
CREATE INDEX IF NOT EXISTS ix_fact_categoria_tiempo    ON analiticas.fact_hechos(categoria_id, tiempo_id);

CREATE INDEX IF NOT EXISTS ix_ch_hecho_uuid ON public.coleccion_hechos(hecho_uuid);
CREATE INDEX IF NOT EXISTS ix_ch_coleccion  ON public.coleccion_hechos(coleccion_handle);
CREATE INDEX IF NOT EXISTS ix_h_uuid        ON public.hechos(uuid);

-- =======================
-- 4) Funciones AUX — resolver IDs de dimensiones
-- =======================
CREATE OR REPLACE FUNCTION analiticas._get_provincia(
  p_lat double precision,
  p_lon double precision
) RETURNS text LANGUAGE plpgsql AS $$
BEGIN
  -- Lógica dummy: ajustá a PostGIS o tu mapeo real
  IF p_lat BETWEEN -35 AND -33 AND p_lon BETWEEN -59 AND -57 THEN
    RETURN 'Buenos Aires';
  ELSIF p_lat BETWEEN -32 AND -30 AND p_lon BETWEEN -65 AND -63 THEN
    RETURN 'Córdoba';
  ELSIF p_lat BETWEEN -34 AND -28 AND p_lon BETWEEN -62 AND -60 THEN
    RETURN 'Santa Fe';
  ELSIF p_lat BETWEEN -34 AND -32 AND p_lon BETWEEN -69 AND -68 THEN
    RETURN 'Mendoza';
  ELSE
    RETURN 'Otra';
  END IF;
END; $$;

CREATE OR REPLACE FUNCTION analiticas._get_ubicacion_id(
  p_lat double precision,
  p_lon double precision
) RETURNS integer LANGUAGE plpgsql AS $$
DECLARE v_id integer; v_prov text;
BEGIN
  IF p_lat IS NULL OR p_lon IS NULL THEN RETURN NULL; END IF;

  v_prov := analiticas._get_provincia(p_lat, p_lon);

  INSERT INTO analiticas.dim_ubicacion(provincia, latitud, longitud)
  VALUES (v_prov, p_lat, p_lon)
  ON CONFLICT (provincia, latitud, longitud) DO NOTHING;

  SELECT ubicacion_id INTO v_id
  FROM analiticas.dim_ubicacion
  WHERE provincia=v_prov AND latitud=p_lat AND longitud=p_lon;

  RETURN v_id;
END; $$;

CREATE OR REPLACE FUNCTION analiticas._get_categoria_id(p_cat text)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE v_id integer;
BEGIN
  IF p_cat IS NULL THEN RETURN NULL; END IF;

  INSERT INTO analiticas.dim_categoria(categoria)
  VALUES (p_cat)
  ON CONFLICT (categoria) DO NOTHING;

  SELECT categoria_id INTO v_id
  FROM analiticas.dim_categoria
  WHERE categoria=p_cat;

  RETURN v_id;
END; $$;

CREATE OR REPLACE FUNCTION analiticas._get_tiempo_id(p_fecha timestamp)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE v_h int; v_d int; v_m int; v_a int; v_id int;
BEGIN
  IF p_fecha IS NULL THEN RETURN NULL; END IF;

  v_h := EXTRACT(HOUR   FROM p_fecha)::int;
  v_d := EXTRACT(DAY    FROM p_fecha)::int;
  v_m := EXTRACT(MONTH  FROM p_fecha)::int;
  v_a := EXTRACT(YEAR   FROM p_fecha)::int;

  INSERT INTO analiticas.dim_tiempo(hora, dia, mes, anio)
  VALUES (v_h, v_d, v_m, v_a)
  ON CONFLICT (hora, dia, mes, anio) DO NOTHING;

  SELECT tiempo_id INTO v_id
  FROM analiticas.dim_tiempo
  WHERE hora=v_h AND dia=v_d AND mes=v_m AND anio=v_a;

  RETURN v_id;
END; $$;

CREATE OR REPLACE FUNCTION analiticas._get_coleccion_id(p_handle text)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE v_id integer;
BEGIN
  IF p_handle IS NULL THEN RETURN NULL; END IF;

  INSERT INTO analiticas.dim_coleccion(coleccion_handle)
  VALUES (p_handle)
  ON CONFLICT (coleccion_handle) DO NOTHING;

  SELECT coleccion_id INTO v_id
  FROM analiticas.dim_coleccion
  WHERE coleccion_handle=p_handle;

  RETURN v_id;
END; $$;

-- =======================
-- 5) Triggers
-- =======================
CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_lat double precision; v_lon double precision; v_cat text; v_fecha timestamp;
  v_uid int; v_cid int; v_tid int; v_colid int;
BEGIN
  SELECT latitud, longitud, categoria, fecha_carga
    INTO v_lat, v_lon, v_cat, v_fecha
  FROM public.hechos
  WHERE uuid = NEW.hecho_uuid;

  IF v_lat IS NULL OR v_lon IS NULL THEN
    RETURN NEW; -- sin coordenadas, no insertamos en la estrella
  END IF;

  v_uid   := analiticas._get_ubicacion_id(v_lat, v_lon);
  v_cid   := analiticas._get_categoria_id(v_cat);
  v_tid   := analiticas._get_tiempo_id(v_fecha);
  v_colid := analiticas._get_coleccion_id(NEW.coleccion_handle);

  INSERT INTO analiticas.fact_hechos(hecho_uuid, ubicacion_id, categoria_id, tiempo_id, coleccion_id)
  VALUES (NEW.hecho_uuid, v_uid, v_cid, v_tid, v_colid)
  ON CONFLICT (hecho_uuid, coleccion_id) DO UPDATE
    SET ubicacion_id = EXCLUDED.ubicacion_id,
        categoria_id = EXCLUDED.categoria_id,
        tiempo_id    = EXCLUDED.tiempo_id;

  RETURN NEW;
END; $$;

CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_del()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  DELETE FROM analiticas.fact_hechos
  WHERE hecho_uuid = OLD.hecho_uuid
    AND coleccion_id = (
      SELECT coleccion_id
      FROM analiticas.dim_coleccion
      WHERE coleccion_handle = OLD.coleccion_handle
    );
  RETURN OLD;
END; $$;

CREATE OR REPLACE FUNCTION analiticas.trg_hechos_upd()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_uid int; v_cid int; v_tid int;
BEGIN
  IF (OLD.latitud   IS DISTINCT FROM NEW.latitud)
     OR(OLD.longitud IS DISTINCT FROM NEW.longitud)
     OR(OLD.categoria IS DISTINCT FROM NEW.categoria)
     OR(OLD.fecha_carga IS DISTINCT FROM NEW.fecha_carga) THEN

    v_uid := CASE WHEN NEW.latitud IS NOT NULL AND NEW.longitud IS NOT NULL
                  THEN analiticas._get_ubicacion_id(NEW.latitud, NEW.longitud) END;
    v_cid := analiticas._get_categoria_id(NEW.categoria);
    v_tid := analiticas._get_tiempo_id(NEW.fecha_carga);

    UPDATE analiticas.fact_hechos fh
    SET ubicacion_id = COALESCE(v_uid, fh.ubicacion_id),
        categoria_id = v_cid,
        tiempo_id    = v_tid
    WHERE fh.hecho_uuid = NEW.uuid;
  END IF;

  RETURN NEW;
END; $$;

-- Registrar/asegurar triggers
DROP TRIGGER IF EXISTS trg_ch_ins  ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_ins
AFTER INSERT ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_ins();

DROP TRIGGER IF EXISTS trg_ch_del  ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_del
AFTER DELETE ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_del();

DROP TRIGGER IF EXISTS trg_hechos_upd ON public.hechos;
CREATE TRIGGER trg_hechos_upd
AFTER UPDATE OF latitud, longitud, categoria, fecha_carga ON public.hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_hechos_upd();

-- =======================
-- 6) Views analíticas
-- =======================
CREATE OR REPLACE VIEW analiticas.agg_hechos_por_provincia AS
SELECT dc.coleccion_handle, du.provincia, COUNT(*)::int AS total
FROM analiticas.fact_hechos fh
JOIN analiticas.dim_coleccion dc ON dc.coleccion_id = fh.coleccion_id
JOIN analiticas.dim_ubicacion du ON du.ubicacion_id = fh.ubicacion_id
GROUP BY dc.coleccion_handle, du.provincia;

CREATE OR REPLACE VIEW analiticas.agg_hechos_por_categoria AS
SELECT dcat.categoria, COUNT(*)::int AS total
FROM analiticas.fact_hechos fh
JOIN analiticas.dim_categoria dcat ON dcat.categoria_id = fh.categoria_id
GROUP BY dcat.categoria;

CREATE OR REPLACE VIEW analiticas.agg_provincia_por_categoria AS
SELECT du.provincia, dcat.categoria, COUNT(*)::int AS total
FROM analiticas.fact_hechos fh
JOIN analiticas.dim_ubicacion du ON du.ubicacion_id = fh.ubicacion_id
JOIN analiticas.dim_categoria dcat ON dcat.categoria_id = fh.categoria_id
GROUP BY du.provincia, dcat.categoria;

CREATE OR REPLACE VIEW analiticas.agg_hora_por_categoria AS
SELECT dcat.categoria, dt.hora, COUNT(*)::int AS total
FROM analiticas.fact_hechos fh
JOIN analiticas.dim_tiempo dt ON dt.tiempo_id = fh.tiempo_id
JOIN analiticas.dim_categoria dcat ON dcat.categoria_id = fh.categoria_id
GROUP BY dcat.categoria, dt.hora;

CREATE OR REPLACE VIEW analiticas.agg_solicitudes_spam AS
SELECT
  COUNT(*) FILTER (WHERE estado = 'SPAM')::int AS spam_total,
  COUNT(*)::int                                AS total
FROM public.solicitudes_eliminacion;

-- =======================
-- 7) Rebuild (opcional — solo estrella)
-- =======================
CREATE OR REPLACE FUNCTION analiticas.rebuild_all() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  v_lat double precision; v_lon double precision; v_cat text; v_fecha timestamp; v_handle text;
  v_uid int; v_cid int; v_tid int; v_colid int; v_uuid text;
BEGIN
  TRUNCATE analiticas.fact_hechos;
  TRUNCATE analiticas.dim_ubicacion  RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_categoria  RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_tiempo     RESTART IDENTITY CASCADE;
  TRUNCATE analiticas.dim_coleccion  RESTART IDENTITY CASCADE;

  FOR v_uuid, v_lat, v_lon, v_cat, v_fecha, v_handle IN
    SELECT h.uuid, h.latitud, h.longitud, h.categoria, h.fecha_carga, ch.coleccion_handle
    FROM public.coleccion_hechos ch
    JOIN public.hechos h ON h.uuid = ch.hecho_uuid
    WHERE h.latitud IS NOT NULL AND h.longitud IS NOT NULL
  LOOP
    v_uid   := analiticas._get_ubicacion_id(v_lat, v_lon);
    v_cid   := analiticas._get_categoria_id(v_cat);
    v_tid   := analiticas._get_tiempo_id(v_fecha);
    v_colid := analiticas._get_coleccion_id(v_handle);

    INSERT INTO analiticas.fact_hechos(hecho_uuid, ubicacion_id, categoria_id, tiempo_id, coleccion_id)
    VALUES (v_uuid, v_uid, v_cid, v_tid, v_colid)
    ON CONFLICT DO NOTHING;
  END LOOP;
END; $$;

COMMIT;
