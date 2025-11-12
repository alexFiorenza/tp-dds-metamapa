-- =========================================================
-- Script de Migración: OLAP Viejo → Sistema de Estadísticas
-- =========================================================
-- Este script elimina el sistema OLAP antiguo (triggers, dimensiones, fact_hechos)
-- y prepara la base de datos para el nuevo sistema de estadísticas programadas.
--
-- IMPORTANTE: Ejecutar este script ANTES de ejecutar estadisticas.sql
--
-- Orden de ejecución:
-- 1. migracion-olap-viejo.sql  (este archivo)
-- 2. estadisticas.sql          (crea el nuevo sistema)
-- =========================================================

BEGIN;

-- =========================================================
-- PASO 1: Eliminar Triggers
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando triggers del sistema OLAP antiguo...';
END $$;

DROP TRIGGER IF EXISTS trg_ch_ins ON public.coleccion_hechos;
DROP TRIGGER IF EXISTS trg_ch_del ON public.coleccion_hechos;
DROP TRIGGER IF EXISTS trg_hechos_upd ON public.hechos;

DO $$
BEGIN
  RAISE NOTICE 'Triggers eliminados correctamente.';
END $$;

-- =========================================================
-- PASO 2: Eliminar Funciones de Triggers
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando funciones de triggers...';
END $$;

DROP FUNCTION IF EXISTS analiticas.trg_coleccion_hechos_ins() CASCADE;
DROP FUNCTION IF EXISTS analiticas.trg_coleccion_hechos_del() CASCADE;
DROP FUNCTION IF EXISTS analiticas.trg_hechos_upd() CASCADE;

DO $$
BEGIN
  RAISE NOTICE 'Funciones de triggers eliminadas correctamente.';
END $$;

-- =========================================================
-- PASO 3: Eliminar Vistas Analíticas
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando vistas analíticas antiguas...';
END $$;

DROP VIEW IF EXISTS analiticas.agg_hechos_por_provincia CASCADE;
DROP VIEW IF EXISTS analiticas.agg_hechos_por_categoria CASCADE;
DROP VIEW IF EXISTS analiticas.agg_provincia_por_categoria CASCADE;
DROP VIEW IF EXISTS analiticas.agg_hora_por_categoria CASCADE;
DROP VIEW IF EXISTS analiticas.agg_solicitudes_spam CASCADE;

DO $$
BEGIN
  RAISE NOTICE 'Vistas analíticas eliminadas correctamente.';
END $$;

-- =========================================================
-- PASO 4: Eliminar Tabla de Hechos (Fact Table)
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando tabla de hechos (fact_hechos)...';
END $$;

DROP TABLE IF EXISTS analiticas.fact_hechos CASCADE;

DO $$
BEGIN
  RAISE NOTICE 'Tabla de hechos eliminada correctamente.';
END $$;

-- =========================================================
-- PASO 5: Eliminar Tablas de Dimensiones
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando tablas de dimensiones...';
END $$;

DROP TABLE IF EXISTS analiticas.dim_ubicacion CASCADE;
DROP TABLE IF EXISTS analiticas.dim_categoria CASCADE;
DROP TABLE IF EXISTS analiticas.dim_tiempo CASCADE;
DROP TABLE IF EXISTS analiticas.dim_coleccion CASCADE;

DO $$
BEGIN
  RAISE NOTICE 'Tablas de dimensiones eliminadas correctamente.';
END $$;

-- =========================================================
-- PASO 6: Eliminar Funciones Auxiliares del Sistema Viejo
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando funciones auxiliares del sistema OLAP...';
END $$;

DROP FUNCTION IF EXISTS analiticas._get_ubicacion_id(double precision, double precision) CASCADE;
DROP FUNCTION IF EXISTS analiticas._get_categoria_id(text) CASCADE;
DROP FUNCTION IF EXISTS analiticas._get_tiempo_id(timestamp) CASCADE;
DROP FUNCTION IF EXISTS analiticas._get_coleccion_id(text) CASCADE;
DROP FUNCTION IF EXISTS analiticas._get_provincia(double precision, double precision) CASCADE;
DROP FUNCTION IF EXISTS analiticas.rebuild_all() CASCADE;

DO $$
BEGIN
  RAISE NOTICE 'Funciones auxiliares eliminadas correctamente.';
END $$;

-- =========================================================
-- PASO 7: Eliminar Índices Antiguos en Tablas Públicas
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Eliminando índices creados por el sistema OLAP...';
END $$;

DROP INDEX IF EXISTS public.ix_ch_hecho_uuid;
DROP INDEX IF EXISTS public.ix_ch_coleccion;
DROP INDEX IF EXISTS public.ix_h_uuid;

DO $$
BEGIN
  RAISE NOTICE 'Índices eliminados correctamente.';
END $$;

-- =========================================================
-- PASO 8: Verificación Final
-- =========================================================
DO $$
BEGIN
  RAISE NOTICE 'Verificando limpieza del esquema analiticas...';
END $$;

-- Listar objetos restantes en el esquema analiticas
DO $$
DECLARE
  v_tables_count INTEGER;
  v_views_count INTEGER;
  v_functions_count INTEGER;
BEGIN
  -- Contar tablas
  SELECT COUNT(*) INTO v_tables_count
  FROM information_schema.tables
  WHERE table_schema = 'analiticas' AND table_type = 'BASE TABLE';

  -- Contar vistas
  SELECT COUNT(*) INTO v_views_count
  FROM information_schema.views
  WHERE table_schema = 'analiticas';

  -- Contar funciones
  SELECT COUNT(*) INTO v_functions_count
  FROM pg_proc p
  JOIN pg_namespace n ON p.pronamespace = n.oid
  WHERE n.nspname = 'analiticas';

  RAISE NOTICE 'Objetos restantes en esquema analiticas:';
  RAISE NOTICE '  - Tablas: %', v_tables_count;
  RAISE NOTICE '  - Vistas: %', v_views_count;
  RAISE NOTICE '  - Funciones: %', v_functions_count;

  IF v_tables_count = 0 AND v_views_count = 0 AND v_functions_count = 0 THEN
    RAISE NOTICE 'Migración completada exitosamente. El esquema está limpio.';
  ELSE
    RAISE NOTICE 'Algunos objetos permanecen en el esquema. Revisar manualmente.';
  END IF;
END $$;

COMMIT;