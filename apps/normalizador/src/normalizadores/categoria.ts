// src/normalizadores/categoria.ts
import { levenshteinDistance } from '../utils/similitud.js'
import mapeosCategoria from '../mapeos/categorias.json' with { type: 'json' }
import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion, CambioAplicado, Advertencia } from '../types.js'

/**
 * Normalizador de categorías: mapea variaciones de texto a categorías estándar
 * usando mapeo exacto, regex y fuzzy matching (Levenshtein).
 */
export class CategoriaNormalizador {
  readonly nombre = 'CategoriaNormalizador'
  readonly nivel = 'ADVERTENCIA' as const

  normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion {
    const categoriaOriginal = hecho.categoria
    const cambios: CambioAplicado[] = []
    const advertencias: Advertencia[] = []

    // Si ya está normalizada, no hacer nada
    if (mapeosCategoria.categoriasValidas.includes(categoriaOriginal as any)) {
      return {
        hechoNormalizado: hecho,
        cambios: [],
        advertencias: [],
        rechazado: false
      }
    }

    let categoriaNormalizada: string | null = null
    let metodo: string | null = null

    // Nivel 1: Mapeo exacto (case-insensitive)
    const clave = categoriaOriginal.toLowerCase()
    if (clave in mapeosCategoria.exactos) {
      categoriaNormalizada = mapeosCategoria.exactos[clave as keyof typeof mapeosCategoria.exactos]
      metodo = 'mapeo_exacto'
    }

    // Nivel 2: Mapeo por regex
    if (!categoriaNormalizada) {
      for (const [pattern, categoria] of Object.entries(mapeosCategoria.regex)) {
        const regex = new RegExp(pattern, 'i')
        if (regex.test(categoriaOriginal)) {
          categoriaNormalizada = categoria
          metodo = 'mapeo_regex'
          break
        }
      }
    }

    // Nivel 3: Fuzzy matching (si está habilitado)
    if (!categoriaNormalizada && config.usarFuzzyMatching) {
      const resultado = this.encontrarMasSimilar(
        categoriaOriginal,
        mapeosCategoria.categoriasValidas,
        config.umbralSimilitud
      )

      if (resultado) {
        categoriaNormalizada = resultado.categoria
        metodo = 'fuzzy_matching'

        advertencias.push({
          tipo: 'categoria_fuzzy',
          mensaje: `Categoría '${categoriaOriginal}' mapeada a '${categoriaNormalizada}' por similitud (${(resultado.similitud * 100).toFixed(1)}%)`,
          severidad: 'warning'
        })
      }
    }

    // Nivel 4: Fallback a "OTRO"
    if (!categoriaNormalizada) {
      categoriaNormalizada = 'OTRO'
      metodo = 'fallback_otro'

      advertencias.push({
        tipo: 'categoria_desconocida',
        mensaje: `Categoría '${categoriaOriginal}' no reconocida, asignada como 'OTRO'`,
        severidad: 'info'
      })
    }

    // Aplicar cambio
    const hechoNormalizado: HechoDTO = {
      ...hecho,
      categoria: categoriaNormalizada
    }

    cambios.push({
      campo: 'categoria',
      valorOriginal: categoriaOriginal,
      valorNormalizado: categoriaNormalizada,
      metodo: metodo || 'desconocido'
    })

    return {
      hechoNormalizado,
      cambios,
      advertencias,
      rechazado: false
    }
  }

  /**
   * Encuentra la categoría más similar usando distancia de Levenshtein
   */
  private encontrarMasSimilar(
    texto: string,
    candidatos: readonly string[],
    umbral: number
  ): { categoria: string; similitud: number } | null {
    let mejor: { categoria: string; similitud: number } | null = null

    for (const candidato of candidatos) {
      const distancia = levenshteinDistance(texto.toLowerCase(), candidato.toLowerCase())
      const max = Math.max(texto.length, candidato.length)
      const similitud = 1 - (distancia / max)

      if (similitud >= umbral && (!mejor || similitud > mejor.similitud)) {
        mejor = { categoria: candidato, similitud }
      }
    }

    return mejor
  }
}
