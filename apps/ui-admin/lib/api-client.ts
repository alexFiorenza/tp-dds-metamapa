import type { HechoDTO, ColeccionDTO, SolicitudEliminacionDTO, RespuestaPaginadaDTO, FiltrosHechos } from "@/types/api"
import { mockHechos, mockColecciones, mockSolicitudes, crearRespuestaPaginada } from "./mock-data"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"
const USE_MOCK = process.env.NEXT_PUBLIC_USE_MOCK === "true"

// Cliente API para consumir el backend
export class ApiClient {
  /**
   * Crea headers con autenticación si se proporciona un token
   */
  private static createHeaders(token?: string | null): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    return headers
  }
  // Obtener hechos con filtros
  static async obtenerHechos(filtros: FiltrosHechos = {}): Promise<RespuestaPaginadaDTO<HechoDTO>> {
    if (USE_MOCK) {
      // Simular filtrado
      let hechosFiltrados = [...mockHechos]

      if (filtros.categoria) {
        hechosFiltrados = hechosFiltrados.filter((h) => h.categoria === filtros.categoria)
      }
      if (filtros.titulo) {
        hechosFiltrados = hechosFiltrados.filter((h) => h.titulo.toLowerCase().includes(filtros.titulo!.toLowerCase()))
      }
      if (filtros.etiquetas) {
        const etiquetasBuscadas = filtros.etiquetas.split(",")
        hechosFiltrados = hechosFiltrados.filter((h) => etiquetasBuscadas.some((e) => h.etiquetas.includes(e.trim())))
      }

      return crearRespuestaPaginada(hechosFiltrados, filtros.pagina || 0, filtros.tamanioPagina || 10)
    }

    // Implementación real con la API
    const params = new URLSearchParams()
    Object.entries(filtros).forEach(([key, value]) => {
      if (value !== undefined) {
        params.append(key, String(value))
      }
    })

    const response = await fetch(`${API_BASE_URL}/api/hechos?${params}`, {
      cache: 'no-store' // Asegurar datos frescos en cada request
    })

    if (!response.ok) {
      throw new Error(`Error al obtener hechos: ${response.statusText}`)
    }

    return response.json()
  }

  // Obtener colecciones
  static async obtenerColecciones(pagina = 0, tamanioPagina = 10, token?: string | null): Promise<RespuestaPaginadaDTO<ColeccionDTO>> {
    if (USE_MOCK) {
      return crearRespuestaPaginada(mockColecciones, pagina, tamanioPagina)
    }

    const response = await fetch(
      `${API_BASE_URL}/administrador/coleccion?page=${pagina}&size=${tamanioPagina}`,
      {
        cache: 'no-store',
        headers: this.createHeaders(token)
      }
    )

    if (!response.ok) {
      throw new Error(`Error al obtener colecciones: ${response.statusText}`)
    }

    return response.json()
  }

  // Obtener colección por identificador
  static async obtenerColeccion(identificador: string, token?: string | null): Promise<ColeccionDTO> {
    if (USE_MOCK) {
      const coleccion = mockColecciones.find((c) => c.handle === identificador)
      if (!coleccion) throw new Error("Colección no encontrada")
      return coleccion
    }

    const response = await fetch(
      `${API_BASE_URL}/administrador/coleccion/${identificador}`,
      {
        cache: 'no-store',
        headers: this.createHeaders(token)
      }
    )

    if (!response.ok) {
      throw new Error(`Error al obtener colección: ${response.statusText}`)
    }

    return response.json()
  }

  // Obtener hechos de una colección (admin)
  static async obtenerHechosDeColeccionAdmin(
    id: string,
    filtros: FiltrosHechos = {},
    token?: string | null
  ): Promise<RespuestaPaginadaDTO<HechoDTO>> {
    if (USE_MOCK) {
      const coleccion = mockColecciones.find((c) => c.handle === id)
      if (!coleccion) throw new Error("Colección no encontrada")

      // Filtrar hechos según criterios de la colección
      const criterioCategoria = coleccion.criteriosDePertenencia.find((c) => c.tipo === "categoria")
      let hechosFiltrados = mockHechos

      if (criterioCategoria?.categoria) {
        hechosFiltrados = hechosFiltrados.filter((h) => h.categoria === criterioCategoria.categoria)
      }

      return crearRespuestaPaginada(hechosFiltrados, filtros.pagina || 0, filtros.tamanioPagina || 10)
    }

    const params = new URLSearchParams()

    // Mapear los parámetros de paginación al formato esperado por la API
    if (filtros.pagina !== undefined) {
      params.append('page', String(filtros.pagina))
    }
    if (filtros.tamanioPagina !== undefined) {
      params.append('size', String(filtros.tamanioPagina))
    }

    // Agregar el resto de filtros
    Object.entries(filtros).forEach(([key, value]) => {
      if (value !== undefined && key !== 'pagina' && key !== 'tamanioPagina') {
        params.append(key, String(value))
      }
    })

    const response = await fetch(
      `${API_BASE_URL}/administrador/coleccion/${id}/hechos?${params}`,
      {
        cache: 'no-store',
        headers: this.createHeaders(token)
      }
    )

    if (!response.ok) {
      throw new Error(`Error al obtener hechos de colección: ${response.statusText}`)
    }

    return response.json()
  }

  // Obtener hechos de una colección
  static async obtenerHechosDeColeccion(
    identificador: string,
    filtros: FiltrosHechos = {},
  ): Promise<RespuestaPaginadaDTO<HechoDTO>> {
    if (USE_MOCK) {
      const coleccion = mockColecciones.find((c) => c.handle === identificador)
      if (!coleccion) throw new Error("Colección no encontrada")

      // Filtrar hechos según criterios de la colección
      const criterioCategoria = coleccion.criteriosDePertenencia.find((c) => c.tipo === "categoria")
      let hechosFiltrados = mockHechos

      if (criterioCategoria?.categoria) {
        hechosFiltrados = hechosFiltrados.filter((h) => h.categoria === criterioCategoria.categoria)
      }

      return crearRespuestaPaginada(hechosFiltrados, filtros.pagina || 0, filtros.tamanioPagina || 10)
    }

    const params = new URLSearchParams()

    // Mapear los parámetros de paginación al formato esperado por la API
    if (filtros.pagina !== undefined) {
      params.append('page', String(filtros.pagina))
    }
    if (filtros.tamanioPagina !== undefined) {
      params.append('size', String(filtros.tamanioPagina))
    }

    // Agregar el resto de filtros
    Object.entries(filtros).forEach(([key, value]) => {
      if (value !== undefined && key !== 'pagina' && key !== 'tamanioPagina') {
        params.append(key, String(value))
      }
    })

    const response = await fetch(
      `${API_BASE_URL}/api/colecciones/${identificador}/hechos?${params}`,
      { cache: 'no-store' }
    )

    if (!response.ok) {
      throw new Error(`Error al obtener hechos de colección: ${response.statusText}`)
    }

    return response.json()
  }

  // Obtener solicitudes de eliminación
  static async obtenerSolicitudes(
    pagina = 0,
    tamanioPagina = 10,
    token?: string | null
  ): Promise<RespuestaPaginadaDTO<SolicitudEliminacionDTO>> {
    if (USE_MOCK) {
      return crearRespuestaPaginada(mockSolicitudes, pagina, tamanioPagina)
    }

    const response = await fetch(
      `${API_BASE_URL}/administrador/solicitudes?page=${pagina}&size=${tamanioPagina}`,
      {
        cache: 'no-store',
        headers: this.createHeaders(token)
      }
    )

    if (!response.ok) {
      throw new Error(`Error al obtener solicitudes: ${response.statusText}`)
    }

    return response.json()
  }

  // Reportar un hecho
  static async reportarHecho(uuid: string): Promise<void> {
    if (USE_MOCK) {
      console.log(`[MOCK] Hecho ${uuid} reportado`)
      return
    }

    const response = await fetch(`${API_BASE_URL}/api/hechos/${uuid}/reportar`, {
      method: "POST",
    })

    if (!response.ok) {
      throw new Error(`Error al reportar hecho: ${response.statusText}`)
    }
  }

  // Aceptar solicitud de eliminación
  static async aceptarSolicitud(uuid: string, token?: string | null): Promise<void> {
    if (USE_MOCK) {
      console.log(`[MOCK] Solicitud ${uuid} aceptada`)
      const solicitud = mockSolicitudes.find((s) => s.uuid === uuid)
      if (solicitud) solicitud.estado = "ACEPTADA"
      return
    }

    const response = await fetch(`${API_BASE_URL}/administrador/solicitud/${uuid}/aceptar`, {
      method: "PUT",
      headers: this.createHeaders(token)
    })

    if (!response.ok) {
      throw new Error(`Error al aceptar solicitud: ${response.statusText}`)
    }
  }

  // Rechazar solicitud de eliminación
  static async rechazarSolicitud(uuid: string, token?: string | null): Promise<void> {
    if (USE_MOCK) {
      console.log(`[MOCK] Solicitud ${uuid} rechazada`)
      const solicitud = mockSolicitudes.find((s) => s.uuid === uuid)
      if (solicitud) solicitud.estado = "RECHAZADA"
      return
    }

    const response = await fetch(`${API_BASE_URL}/administrador/solicitud/${uuid}/rechazar`, {
      method: "PUT",
      headers: this.createHeaders(token)
    })

    if (!response.ok) {
      throw new Error(`Error al rechazar solicitud: ${response.statusText}`)
    }
  }
}
