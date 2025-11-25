// src/normalizadores/coordenadas.ts
import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion, CambioAplicado, Advertencia } from '../types.js'

/**
 * Normalizador de coordenadas: valida rangos geográficos y redondea a precisión razonable.
 * Latitud válida: [-90, 90]
 * Longitud válida: [-180, 180]
 * Precisión: 6 decimales (~11cm)
 */
export class CoordenadasNormalizador {
  readonly nombre = 'CoordenadasNormalizador'
  readonly nivel = 'CRITICO' as const

  normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion {
    const cambios: CambioAplicado[] = []
    const advertencias: Advertencia[] = []

    const { latitud, longitud } = hecho

    // Validar rangos
    const latitudValida = latitud >= -90 && latitud <= 90
    const longitudValida = longitud >= -180 && longitud <= 180

    if (!latitudValida || !longitudValida) {
      const error = `Coordenadas inválidas: lat=${latitud}, lng=${longitud}`

      if (config.rechazarCoordenadasInvalidas) {
        // Modo estricto: rechazar el hecho
        return {
          hechoNormalizado: hecho,
          cambios: [],
          advertencias: [],
          rechazado: true,
          motivoRechazo: error
        }
      } else {
        // Modo permisivo: solo advertir
        advertencias.push({
          tipo: 'coordenadas_invalidas',
          mensaje: error,
          severidad: 'error'
        })
      }
    }

    // Redondear a 6 decimales (precisión ~11cm)
    const latitudRedondeada = Math.round(latitud * 1000000) / 1000000
    const longitudRedondeada = Math.round(longitud * 1000000) / 1000000

    const hechoNormalizado = { ...hecho }

    if (latitudRedondeada !== latitud) {
      hechoNormalizado.latitud = latitudRedondeada
      cambios.push({
        campo: 'latitud',
        valorOriginal: latitud,
        valorNormalizado: latitudRedondeada,
        metodo: 'redondeo_precision'
      })
    }

    if (longitudRedondeada !== longitud) {
      hechoNormalizado.longitud = longitudRedondeada
      cambios.push({
        campo: 'longitud',
        valorOriginal: longitud,
        valorNormalizado: longitudRedondeada,
        metodo: 'redondeo_precision'
      })
    }

    // Advertencia si las coordenadas son (0, 0) - probablemente un placeholder
    if (latitudRedondeada === 0 && longitudRedondeada === 0) {
      advertencias.push({
        tipo: 'coordenadas_sospechosas',
        mensaje: 'Coordenadas en (0, 0) - podría ser un valor por defecto',
        severidad: 'warning'
      })
    }

    return {
      hechoNormalizado,
      cambios,
      advertencias,
      rechazado: false
    }
  }
}
