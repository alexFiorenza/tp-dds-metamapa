import type { HechoDTO, ColeccionDTO, SolicitudEliminacionDTO, RespuestaPaginadaDTO } from "@/types/api"

// Datos mock para desarrollo
export const mockHechos: HechoDTO[] = [
  {
    uuid: "hecho-001",
    titulo: "Incendio forestal en Córdoba",
    descripcion: "Se reportó un incendio forestal en las sierras de Córdoba que afectó aproximadamente 50 hectáreas.",
    categoria: "INCENDIO",
    origen: "Reporte ciudadano",
    fechaAcontecimiento: "2024-01-15",
    fechaCarga: "2024-01-15T14:30:00",
    longitud: -64.1888,
    latitud: -31.4201,
    estado: "ACTIVO",
    etiquetas: ["incendio", "córdoba", "emergencia"],
  },
  {
    uuid: "hecho-002",
    titulo: "Contaminación en Río de la Plata",
    descripcion: "Ciudadanos reportan alta contaminación en la costa del Río de la Plata.",
    categoria: "CONTAMINACION",
    origen: "ONG Ambiental",
    fechaAcontecimiento: "2024-01-20",
    fechaCarga: "2024-01-20T09:15:00",
    longitud: -58.3816,
    latitud: -34.6037,
    estado: "ACTIVO",
    etiquetas: ["contaminación", "río", "ambiente"],
  },
  {
    uuid: "hecho-003",
    titulo: "Manifestación pacífica en Plaza de Mayo",
    descripcion: "Manifestación de organizaciones sociales en Plaza de Mayo.",
    categoria: "MANIFESTACION",
    origen: "Medios de comunicación",
    fechaAcontecimiento: "2024-02-01",
    fechaCarga: "2024-02-01T11:00:00",
    longitud: -58.3731,
    latitud: -34.6083,
    estado: "ACTIVO",
    etiquetas: ["manifestación", "buenos aires", "social"],
  },
  {
    uuid: "hecho-004",
    titulo: "Inundación en zona rural de Santa Fe",
    descripcion: "Fuertes lluvias causaron inundaciones en zonas rurales de Santa Fe.",
    categoria: "INUNDACION",
    origen: "Defensa Civil",
    fechaAcontecimiento: "2024-02-10",
    fechaCarga: "2024-02-10T16:45:00",
    longitud: -60.6393,
    latitud: -31.6107,
    estado: "ACTIVO",
    etiquetas: ["inundación", "santa fe", "clima"],
  },
  {
    uuid: "hecho-005",
    titulo: "Avistamiento de fauna en peligro",
    descripcion: "Se avistó un yaguareté en el Parque Nacional Iguazú.",
    categoria: "FAUNA",
    origen: "Guardaparques",
    fechaAcontecimiento: "2024-02-15",
    fechaCarga: "2024-02-15T08:20:00",
    longitud: -54.4396,
    latitud: -25.6953,
    estado: "ACTIVO",
    etiquetas: ["fauna", "conservación", "misiones"],
  },
]

export const mockColecciones: ColeccionDTO[] = [
  {
    handle: "incendios-2024",
    titulo: "Incendios Forestales 2024",
    descripcion: "Recopilación de incendios forestales reportados durante 2024",
    fuentes: [
      {
        host: "http://localhost:7006",
        params: {},
        uuid: "fuente-001",
      },
    ],
    criteriosDePertenencia: [
      {
        tipo: "categoria",
        categoria: "INCENDIO",
      },
    ],
  },
  {
    handle: "contaminacion-ambiental",
    titulo: "Contaminación Ambiental",
    descripcion: "Reportes de contaminación en diferentes regiones",
    fuentes: [
      {
        host: "http://localhost:7006",
        params: {},
        uuid: "fuente-002",
      },
    ],
    criteriosDePertenencia: [
      {
        tipo: "categoria",
        categoria: "CONTAMINACION",
      },
    ],
  },
]

export const mockSolicitudes: SolicitudEliminacionDTO[] = [
  {
    uuid: "solicitud-001",
    texto: "Este hecho contiene información incorrecta sobre la ubicación del evento.",
    hecho: "hecho-001",
    fechaSolicitud: "2024-02-20T10:30:00",
    estado: "PENDIENTE",
  },
  {
    uuid: "solicitud-002",
    texto: "La información está duplicada con otro reporte.",
    hecho: "hecho-003",
    fechaSolicitud: "2024-02-21T14:15:00",
    estado: "PENDIENTE",
  },
]

// Función helper para crear respuestas paginadas
export function crearRespuestaPaginada<T>(datos: T[], pagina = 0, tamanioPagina = 10): RespuestaPaginadaDTO<T> {
  const inicio = pagina * tamanioPagina
  const fin = inicio + tamanioPagina
  const datosPaginados = datos.slice(inicio, fin)
  const totalPaginas = Math.ceil(datos.length / tamanioPagina)

  return {
    datos: datosPaginados,
    pagina,
    tamanioPagina,
    totalElementos: datos.length,
    totalPaginas,
    tieneSiguiente: pagina < totalPaginas - 1,
    tieneAnterior: pagina > 0,
  }
}
