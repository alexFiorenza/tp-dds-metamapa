-- =========================================================
-- OLAP / Analíticas (idempotente)
-- =========================================================
BEGIN;

CREATE SCHEMA IF NOT EXISTS analiticas;

-- ---------------------------------------------------------
-- Tablas de agregados
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS analiticas.agg_hechos_por_ubicacion (
  coleccion_handle text NOT NULL,
  latitud          double precision NOT NULL,
  longitud         double precision NOT NULL,
  total            integer NOT NULL DEFAULT 0,
  PRIMARY KEY (coleccion_handle, latitud, longitud)
);

CREATE TABLE IF NOT EXISTS analiticas.agg_hechos_por_categoria (
  coleccion_handle text NOT NULL,
  categoria        text NOT NULL,
  total            integer NOT NULL DEFAULT 0,
  PRIMARY KEY (coleccion_handle, categoria)
);

CREATE TABLE IF NOT EXISTS analiticas.agg_categoria_por_ubicacion (
  categoria  text NOT NULL,
  latitud    double precision NOT NULL,
  longitud   double precision NOT NULL,
  total      integer NOT NULL DEFAULT 0,
  PRIMARY KEY (categoria, latitud, longitud)
);

CREATE TABLE IF NOT EXISTS analiticas.agg_cat_por_hora (
  categoria  text NOT NULL,
  hora       integer NOT NULL CHECK (hora BETWEEN 0 AND 23),
  total      integer NOT NULL DEFAULT 0,
  PRIMARY KEY (categoria, hora)
);

CREATE TABLE IF NOT EXISTS analiticas.agg_solicitudes_spam (
  id         boolean PRIMARY KEY DEFAULT true, -- 1 sola fila
  spam_total integer NOT NULL DEFAULT 0,
  total      integer NOT NULL DEFAULT 0
);
INSERT INTO analiticas.agg_solicitudes_spam(id) VALUES(true)
ON CONFLICT (id) DO NOTHING;

CREATE INDEX IF NOT EXISTS ix_ch_hecho_uuid ON public.coleccion_hechos(hecho_uuid);
CREATE INDEX IF NOT EXISTS ix_ch_coleccion   ON public.coleccion_hechos(coleccion_handle);
CREATE INDEX IF NOT EXISTS ix_h_uuid         ON public.hechos(uuid);

-- ---------------------------------------------------------
-- Helpers de incremento/decremento (UPSERTs)
-- ---------------------------------------------------------

-- Hechos x ubicación y colección
CREATE OR REPLACE FUNCTION analiticas._inc_hechos_por_ubicacion(
  p_col text, p_lat double precision, p_lon double precision, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  INSERT INTO analiticas.agg_hechos_por_ubicacion(coleccion_handle, latitud, longitud, total)
  VALUES (p_col, p_lat, p_lon, GREATEST(p_delta,0))
  ON CONFLICT (coleccion_handle, latitud, longitud) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_hechos_por_ubicacion.total + p_delta);
END;
$$;

-- Hechos x categoría y colección
CREATE OR REPLACE FUNCTION analiticas._inc_hechos_por_categoria(
  p_col text, p_cat text, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_cat IS NULL THEN
    RETURN;
  END IF;
  INSERT INTO analiticas.agg_hechos_por_categoria(coleccion_handle, categoria, total)
  VALUES (p_col, p_cat, GREATEST(p_delta,0))
  ON CONFLICT (coleccion_handle, categoria) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_hechos_por_categoria.total + p_delta);
END;
$$;

-- Ubicación x categoría
CREATE OR REPLACE FUNCTION analiticas._inc_categoria_por_ubicacion(
  p_cat text, p_lat double precision, p_lon double precision, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_cat IS NULL THEN
    RETURN;
  END IF;
  INSERT INTO analiticas.agg_categoria_por_ubicacion(categoria, latitud, longitud, total)
  VALUES (p_cat, p_lat, p_lon, GREATEST(p_delta,0))
  ON CONFLICT (categoria, latitud, longitud) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_categoria_por_ubicacion.total + p_delta);
END;
$$;

-- Hora x categoría
CREATE OR REPLACE FUNCTION analiticas._inc_cat_por_hora(
  p_cat text, p_hora int, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_cat IS NULL OR p_hora IS NULL THEN
    RETURN;
  END IF;
  INSERT INTO analiticas.agg_cat_por_hora(categoria, hora, total)
  VALUES (p_cat, p_hora, GREATEST(p_delta,0))
  ON CONFLICT (categoria, hora) DO UPDATE
  SET total = GREATEST(0, analiticas.agg_cat_por_hora.total + p_delta);
END;
$$;

-- Solicitudes SPAM
CREATE OR REPLACE FUNCTION analiticas._inc_solicitudes_spam(
  p_is_spam boolean, p_delta int
) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  UPDATE analiticas.agg_solicitudes_spam
  SET total = GREATEST(0, total + p_delta),
      spam_total = GREATEST(0, spam_total + (CASE WHEN p_is_spam THEN p_delta ELSE 0 END));
END;
$$;

-- ---------------------------------------------------------
-- Triggers en coleccion_hechos (INS/DEL)
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_lat  double precision;
  v_lon  double precision;
  v_cat  text;
  v_hora int;
BEGIN
  SELECT latitud, longitud, categoria, EXTRACT(HOUR FROM fecha_carga)::int
    INTO v_lat, v_lon, v_cat, v_hora
  FROM public.hechos
  WHERE uuid = NEW.hecho_uuid;

  IF v_lat IS NULL OR v_lon IS NULL THEN
    RETURN NEW;
  END IF;

  PERFORM analiticas._inc_hechos_por_ubicacion(NEW.coleccion_handle, v_lat, v_lon, +1);
  PERFORM analiticas._inc_hechos_por_categoria(NEW.coleccion_handle, v_cat, +1);
  PERFORM analiticas._inc_categoria_por_ubicacion(v_cat, v_lat, v_lon, +1);
  PERFORM analiticas._inc_cat_por_hora(v_cat, v_hora, +1);

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION analiticas.trg_coleccion_hechos_del()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_lat  double precision;
  v_lon  double precision;
  v_cat  text;
  v_hora int;
BEGIN
  SELECT latitud, longitud, categoria, EXTRACT(HOUR FROM fecha_carga)::int
    INTO v_lat, v_lon, v_cat, v_hora
  FROM public.hechos
  WHERE uuid = OLD.hecho_uuid;

  IF v_lat IS NULL OR v_lon IS NULL THEN
    RETURN OLD;
  END IF;

  PERFORM analiticas._inc_hechos_por_ubicacion(OLD.coleccion_handle, v_lat, v_lon, -1);
  PERFORM analiticas._inc_hechos_por_categoria(OLD.coleccion_handle, v_cat, -1);
  PERFORM analiticas._inc_categoria_por_ubicacion(v_cat, v_lat, v_lon, -1);
  PERFORM analiticas._inc_cat_por_hora(v_cat, v_hora, -1);

  RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS trg_ch_ins ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_ins
AFTER INSERT ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_ins();

DROP TRIGGER IF EXISTS trg_ch_del ON public.coleccion_hechos;
CREATE TRIGGER trg_ch_del
AFTER DELETE ON public.coleccion_hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_coleccion_hechos_del();

-- ---------------------------------------------------------
-- Trigger en hechos (UPDATE relevantes)
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION analiticas.trg_hechos_upd()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_hora_old int := NULL;
  v_hora_new int := NULL;
  r record;
BEGIN
  IF (OLD.latitud IS DISTINCT FROM NEW.latitud)
     OR (OLD.longitud IS DISTINCT FROM NEW.longitud)
     OR (OLD.categoria IS DISTINCT FROM NEW.categoria)
     OR (OLD.fecha_carga IS DISTINCT FROM NEW.fecha_carga) THEN

    IF OLD.fecha_carga IS NOT NULL THEN
      v_hora_old := EXTRACT(HOUR FROM OLD.fecha_carga)::int;
    END IF;
    IF NEW.fecha_carga IS NOT NULL THEN
      v_hora_new := EXTRACT(HOUR FROM NEW.fecha_carga)::int;
    END IF;

    FOR r IN
      SELECT coleccion_handle
      FROM public.coleccion_hechos
      WHERE hecho_uuid = NEW.uuid
    LOOP
      -- restar OLD
      IF OLD.latitud IS NOT NULL AND OLD.longitud IS NOT NULL THEN
        PERFORM analiticas._inc_hechos_por_ubicacion(r.coleccion_handle, OLD.latitud, OLD.longitud, -1);
        PERFORM analiticas._inc_hechos_por_categoria(r.coleccion_handle, OLD.categoria, -1);
        PERFORM analiticas._inc_categoria_por_ubicacion(OLD.categoria, OLD.latitud, OLD.longitud, -1);
        PERFORM analiticas._inc_cat_por_hora(OLD.categoria, v_hora_old, -1);
      END IF;

      -- sumar NEW
      IF NEW.latitud IS NOT NULL AND NEW.longitud IS NOT NULL THEN
        PERFORM analiticas._inc_hechos_por_ubicacion(r.coleccion_handle, NEW.latitud, NEW.longitud, +1);
        PERFORM analiticas._inc_hechos_por_categoria(r.coleccion_handle, NEW.categoria, +1);
        PERFORM analiticas._inc_categoria_por_ubicacion(NEW.categoria, NEW.latitud, NEW.longitud, +1);
        PERFORM analiticas._inc_cat_por_hora(NEW.categoria, v_hora_new, +1);
      END IF;
    END LOOP;
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_hechos_upd ON public.hechos;
CREATE TRIGGER trg_hechos_upd
AFTER UPDATE OF latitud, longitud, categoria, fecha_carga ON public.hechos
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_hechos_upd();

-- ---------------------------------------------------------
-- Triggers para solicitudes de eliminación
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION analiticas.trg_sol_elim_ins()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  PERFORM analiticas._inc_solicitudes_spam(NEW.estado = 'SPAM', +1);
  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION analiticas.trg_sol_elim_del()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  PERFORM analiticas._inc_solicitudes_spam(OLD.estado = 'SPAM', -1);
  RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS trg_selim_ins ON public.solicitudes_eliminacion;
CREATE TRIGGER trg_selim_ins
AFTER INSERT ON public.solicitudes_eliminacion
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_sol_elim_ins();

DROP TRIGGER IF EXISTS trg_selim_del ON public.solicitudes_eliminacion;
CREATE TRIGGER trg_selim_del
AFTER DELETE ON public.solicitudes_eliminacion
FOR EACH ROW EXECUTE FUNCTION analiticas.trg_sol_elim_del();

-- ---------------------------------------------------------
-- Rebuild completo (opcional, para saneo/cargas masivas)
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION analiticas.rebuild_all() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  v_hora int;
BEGIN
  TRUNCATE analiticas.agg_hechos_por_ubicacion;
  TRUNCATE analiticas.agg_hechos_por_categoria;
  TRUNCATE analiticas.agg_categoria_por_ubicacion;
  TRUNCATE analiticas.agg_cat_por_hora;
  UPDATE analiticas.agg_solicitudes_spam SET spam_total = 0, total = 0;

  -- Recalcular desde OLTP
  INSERT INTO analiticas.agg_hechos_por_ubicacion(coleccion_handle, latitud, longitud, total)
  SELECT ch.coleccion_handle, h.latitud, h.longitud, COUNT(*)
  FROM public.coleccion_hechos ch
  JOIN public.hechos h ON h.uuid = ch.hecho_uuid
  WHERE h.latitud IS NOT NULL AND h.longitud IS NOT NULL
  GROUP BY 1,2,3;

  INSERT INTO analiticas.agg_hechos_por_categoria(coleccion_handle, categoria, total)
  SELECT ch.coleccion_handle, h.categoria, COUNT(*)
  FROM public.coleccion_hechos ch
  JOIN public.hechos h ON h.uuid = ch.hecho_uuid
  WHERE h.categoria IS NOT NULL
  GROUP BY 1,2;

  INSERT INTO analiticas.agg_categoria_por_ubicacion(categoria, latitud, longitud, total)
  SELECT h.categoria, h.latitud, h.longitud, COUNT(*)
  FROM public.hechos h
  WHERE h.categoria IS NOT NULL AND h.latitud IS NOT NULL AND h.longitud IS NOT NULL
  GROUP BY 1,2,3;

  INSERT INTO analiticas.agg_cat_por_hora(categoria, hora, total)
  SELECT h.categoria, EXTRACT(HOUR FROM h.fecha_carga)::int AS hora, COUNT(*)
  FROM public.hechos h
  WHERE h.categoria IS NOT NULL AND h.fecha_carga IS NOT NULL
  GROUP BY 1,2;

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
