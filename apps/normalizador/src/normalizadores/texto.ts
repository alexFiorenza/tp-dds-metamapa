// src/normalizadores/texto.ts
import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion, CambioAplicado } from '../types.js'

/**
 * Normalizador de texto: limpia espacios, saltos de línea y formato general
 * de los campos de texto del hecho.
 */
export class TextoNormalizador {
  readonly nombre = 'TextoNormalizador'
  readonly nivel = 'OPCIONAL' as const

  normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion {
    const cambios: CambioAplicado[] = []
    const hechoNormalizado = { ...hecho }

    // Normalizar título
    if (hecho.titulo) {
      const tituloNormalizado = this.normalizarTexto(hecho.titulo)
      if (tituloNormalizado !== hecho.titulo) {
        hechoNormalizado.titulo = tituloNormalizado
        cambios.push({
          campo: 'titulo',
          valorOriginal: hecho.titulo,
          valorNormalizado: tituloNormalizado,
          metodo: 'limpiar_texto'
        })
      }
    }

    // Normalizar descripción
    if (hecho.descripcion) {
      const descripcionNormalizada = this.normalizarTexto(hecho.descripcion)
      if (descripcionNormalizada !== hecho.descripcion) {
        hechoNormalizado.descripcion = descripcionNormalizada
        cambios.push({
          campo: 'descripcion',
          valorOriginal: hecho.descripcion,
          valorNormalizado: descripcionNormalizada,
          metodo: 'limpiar_texto'
        })
      }
    }

    return {
      hechoNormalizado,
      cambios,
      advertencias: [],
      rechazado: false
    }
  }

  /**
   * Normaliza un texto eliminando espacios extra y saltos de línea excesivos
   */
  private normalizarTexto(texto: string): string {
    return texto
      .trim()                          // Quitar espacios al inicio/final
      .replace(/\s+/g, ' ')           // Múltiples espacios → uno solo
      .replace(/\n{3,}/g, '\n\n')     // Múltiples saltos de línea → máximo dos
  }
}
