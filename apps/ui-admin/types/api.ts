export type EstadoHecho = "ACTIVO" | "OCULTO"
export type EstadoSolicitud = "PENDIENTE" | "ACEPTADA" | "RECHAZADA"

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
  estado: EstadoHecho
  etiquetas: string[]
  multimedia?: MultimediaDTO[]
}

export interface MultimediaDTO {
  tipo: "IMAGEN" | "VIDEO" | "AUDIO"
  url: string
}

export interface ColeccionDTO {
  handle: string
  titulo: string
  descripcion: string
  fuentes: FuenteDTO[]
  criteriosDePertenencia: CriterioCreateDTO[]
}

export interface FuenteDTO {
  host: string
  params: Record<string, any>
  uuid: string
}

export interface CriterioCreateDTO {
  tipo: string
  titulo?: string
  categoria?: string
  descripcion?: string
  origen?: string
  fechaAcontecimiento?: string
  fechaCarga?: string
  longitud?: number
  latitud?: number
  estado?: string
  etiquetas?: string
}

export interface SolicitudEliminacionDTO {
  uuid: string
  texto: string
  hecho: string
  fechaSolicitud: string
  estado: EstadoSolicitud
}

export interface RespuestaPaginadaDTO<T> {
  datos: T[]
  pagina: number
  tamanioPagina: number
  totalElementos: number
  totalPaginas: number
  tieneSiguiente: boolean
  tieneAnterior: boolean
}

export interface FiltrosHechos {
  categoria?: string
  titulo?: string
  descripcion?: string
  origen?: string
  fechaAcontecimiento?: string
  longitud?: number
  latitud?: number
  estado?: EstadoHecho
  fechaCarga?: string
  etiquetas?: string
  pagina?: number
  tamanioPagina?: number
}
