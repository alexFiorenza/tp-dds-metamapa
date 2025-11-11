// src/types.ts
export interface HechoDTO {
  uuid: string
  titulo: string
  descripcion: string
  categoria: string
  origen: string
  fechaAcontecimiento: string
  fechaCarga: string
  longitud: number
  latitud: number
  estado: 'ACTIVO' | 'OCULTO'
  etiquetas: string[]
  multimedia?: string[]
}

export interface ConfiguracionNormalizacion {
  normalizarCategorias?: boolean
  normalizarFechas?: boolean
  normalizarCoordenadas?: boolean
  normalizarTexto?: boolean
  normalizarEtiquetas?: boolean
  usarFuzzyMatching?: boolean
  umbralSimilitud?: number
  rechazarCoordenadasInvalidas?: boolean
}

export interface CambioAplicado {
  campo: string
  valorOriginal: any
  valorNormalizado: any
  metodo: string
}

export interface Advertencia {
  tipo: string
  mensaje: string
  severidad: 'info' | 'warning' | 'error'
}

export interface ResultadoNormalizacion {
  hechoNormalizado: HechoDTO
  cambios: CambioAplicado[]
  advertencias: Advertencia[]
  rechazado: boolean
  motivoRechazo?: string
}

export interface HechoRechazado {
  hechoOriginal: HechoDTO
  motivo: string
  errores: string[]
}

export interface EstadisticasNormalizacion {
  totalProcesados: number
  totalNormalizados: number
  totalRechazados: number
  cambiosPorTipo: Record<string, number>
  tiempoEjecucion: number
}

export interface NormalizarRequest {
  hechos: HechoDTO[]
  config?: ConfiguracionNormalizacion
}

export interface NormalizarResponse {
  hechosNormalizados: HechoDTO[]
  hechosRechazados: HechoRechazado[]
  estadisticas: EstadisticasNormalizacion
}
