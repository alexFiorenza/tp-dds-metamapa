// src/pipeline.ts
import { CategoriaNormalizador } from './normalizadores/categoria.js'
import { FechaNormalizador } from './normalizadores/fecha.js'
import { CoordenadasNormalizador } from './normalizadores/coordenadas.js'
import { TextoNormalizador } from './normalizadores/texto.js'
import { EtiquetasNormalizador } from './normalizadores/etiquetas.js'
import type {
  HechoDTO,
  ConfiguracionNormalizacion,
  ResultadoNormalizacion,
  CambioAplicado,
  Advertencia
} from './types.js'

interface Normalizador {
  normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion
  nombre: string
  nivel: 'CRITICO' | 'ADVERTENCIA' | 'OPCIONAL'
}

const CONFIG_DEFAULT: Required<ConfiguracionNormalizacion> = {
  normalizarCategorias: true,
  normalizarFechas: true,
  normalizarCoordenadas: true,
  normalizarTexto: true,
  normalizarEtiquetas: true,
  usarFuzzyMatching: true,
  umbralSimilitud: 0.85,
  rechazarCoordenadasInvalidas: true
}

/**
 * Pipeline de normalización que ejecuta múltiples normalizadores en secuencia.
 *
 * Orden de ejecución:
 * 1. TextoNormalizador - Limpia espacios y formato
 * 2. CategoriaNormalizador - Mapea categorías
 * 3. FechaNormalizador - Normaliza fechas
 * 4. CoordenadasNormalizador - Valida y redondea coordenadas
 * 5. EtiquetasNormalizador - Normaliza etiquetas
 */
export class NormalizadorPipeline {
  private pipeline: Normalizador[]
  private config: Required<ConfiguracionNormalizacion>

  constructor(config?: ConfiguracionNormalizacion) {
    this.config = { ...CONFIG_DEFAULT, ...config }

    // Pipeline en orden específico (el orden importa!)
    this.pipeline = [
      new TextoNormalizador(),
      new CategoriaNormalizador(),
      new FechaNormalizador(),
      new CoordenadasNormalizador(),
      new EtiquetasNormalizador()
    ]
  }

  /**
   * Normaliza un hecho ejecutando todos los normalizadores habilitados
   */
  normalizar(hecho: HechoDTO): ResultadoNormalizacion {
    let hechoActual = { ...hecho }
    const todosLosCambios: CambioAplicado[] = []
    const todasLasAdvertencias: Advertencia[] = []

    for (const normalizador of this.pipeline) {
      // Verificar si está habilitado
      if (!this.estaHabilitado(normalizador)) {
        continue
      }

      const resultado = normalizador.normalizar(hechoActual, this.config)

      // Si es crítico y falló → rechazar inmediatamente
      if (resultado.rechazado && normalizador.nivel === 'CRITICO') {
        return {
          hechoNormalizado: hecho, // Devolver original sin cambios
          cambios: todosLosCambios,
          advertencias: todasLasAdvertencias,
          rechazado: true,
          motivoRechazo: resultado.motivoRechazo
        }
      }

      // Actualizar hecho con los cambios
      hechoActual = resultado.hechoNormalizado
      todosLosCambios.push(...resultado.cambios)
      todasLasAdvertencias.push(...resultado.advertencias)
    }

    return {
      hechoNormalizado: hechoActual,
      cambios: todosLosCambios,
      advertencias: todasLasAdvertencias,
      rechazado: false
    }
  }

  /**
   * Verifica si un normalizador está habilitado según la configuración
   */
  private estaHabilitado(normalizador: Normalizador): boolean {
    const nombre = normalizador.nombre
    if (nombre === 'TextoNormalizador') return this.config.normalizarTexto
    if (nombre === 'CategoriaNormalizador') return this.config.normalizarCategorias
    if (nombre === 'FechaNormalizador') return this.config.normalizarFechas
    if (nombre === 'CoordenadasNormalizador') return this.config.normalizarCoordenadas
    if (nombre === 'EtiquetasNormalizador') return this.config.normalizarEtiquetas
    return true // Por defecto habilitado
  }
}
