// src/core/normalizar.ts
// Lógica de negocio PURA - reutilizable en HTTP server y Lambda

import { NormalizadorPipeline } from '../pipeline.js'
import mapeosCategoria from '../mapeos/categorias.json' with { type: 'json' }
import type { NormalizarRequest, NormalizarResponse, HechoRechazado } from '../types.js'

/**
 * Función principal de normalización - agnóstica del transporte (HTTP/Lambda)
 * Esta función puede ser llamada desde cualquier handler
 */
export function normalizarHechos(request: NormalizarRequest): NormalizarResponse {
  const startTime = Date.now()
  const { hechos, config } = request

  // Validaciones
  if (!Array.isArray(hechos)) {
    throw new Error('Se esperaba un array de hechos')
  }

  if (hechos.length === 0) {
    throw new Error('El array de hechos está vacío')
  }

  if (hechos.length > 1000) {
    throw new Error('Máximo 1000 hechos por request. Procesar en lotes.')
  }

  // Log
  console.log(`[${new Date().toISOString()}] Procesando ${hechos.length} hechos`)

  // Crear pipeline y normalizar
  const pipeline = new NormalizadorPipeline(config)
  const hechosNormalizados = []
  const hechosRechazados: HechoRechazado[] = []
  const cambiosPorTipo: Record<string, number> = {}

  for (const hecho of hechos) {
    const resultado = pipeline.normalizar(hecho)

    if (resultado.rechazado) {
      hechosRechazados.push({
        hechoOriginal: hecho,
        motivo: resultado.motivoRechazo || 'Error desconocido',
        errores: resultado.advertencias.map(a => a.mensaje)
      })
    } else {
      hechosNormalizados.push(resultado.hechoNormalizado)

      // Contar cambios por tipo
      resultado.cambios.forEach(cambio => {
        cambiosPorTipo[cambio.campo] = (cambiosPorTipo[cambio.campo] || 0) + 1
      })
    }
  }

  const tiempoEjecucion = Date.now() - startTime

  // Log resultado
  console.log(`[${new Date().toISOString()}] Completado en ${tiempoEjecucion}ms - ${hechosNormalizados.length} normalizados, ${hechosRechazados.length} rechazados`)

  // Respuesta
  return {
    hechosNormalizados,
    hechosRechazados,
    estadisticas: {
      totalProcesados: hechos.length,
      totalNormalizados: hechosNormalizados.length,
      totalRechazados: hechosRechazados.length,
      cambiosPorTipo,
      tiempoEjecucion
    }
  }
}

/**
 * Obtener categorías disponibles
 */
export function obtenerCategorias() {
  return {
    categorias: mapeosCategoria.categoriasValidas
  }
}
