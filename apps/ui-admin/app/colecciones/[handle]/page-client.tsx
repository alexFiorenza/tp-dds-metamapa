"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { ApiClient } from "@/lib/api-client"
import { ColeccionDetailView } from "@/components/coleccion-detail-view"
import { FiltrosHechos } from "@/components/filtros-hechos"
import type { RespuestaPaginadaDTO, HechoDTO, ColeccionDTO, FiltrosHechos as FiltrosHechosType } from "@/types/api"

interface ColeccionPageClientProps {
  handle: string
  coleccion: ColeccionDTO
}

export function ColeccionPageClient({ handle, coleccion }: ColeccionPageClientProps) {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [initialData, setInitialData] = useState<RespuestaPaginadaDTO<HechoDTO> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [filtrosAplicados, setFiltrosAplicados] = useState<FiltrosHechosType>({})

  // Cargar filtros iniciales desde la URL
  useEffect(() => {
    const filtrosDesdeUrl: FiltrosHechosType = {}

    searchParams.forEach((value, key) => {
      if (key === 'pagina' || key === 'tamanioPagina') return

      if (key === 'latitud' || key === 'longitud') {
        const numValue = parseFloat(value)
        if (!isNaN(numValue)) {
          filtrosDesdeUrl[key] = numValue
        }
      } else {
        filtrosDesdeUrl[key] = value
      }
    })

    setFiltrosAplicados(filtrosDesdeUrl)
  }, [searchParams])

  // Cargar datos iniciales
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const filtros: FiltrosHechosType = {}
        searchParams.forEach((value, key) => {
          if (key === 'latitud' || key === 'longitud') {
            const numValue = parseFloat(value)
            if (!isNaN(numValue)) {
              filtros[key] = numValue
            }
          } else {
            filtros[key] = value
          }
        })

        const respuesta = await ApiClient.obtenerHechosDeColeccion(handle, {
          ...filtros,
          pagina: 0,
          tamanioPagina: 10,
        })
        setInitialData(respuesta)
      } catch (error) {
        console.error('Error loading initial data:', error)
      } finally {
        setIsLoading(false)
      }
    }

    loadInitialData()
  }, [handle, searchParams])

  // Función para fetch de páginas
  const fetchPage = async (page: number) => {
    return await ApiClient.obtenerHechosDeColeccion(handle, {
      ...filtrosAplicados,
      pagina: page,
      tamanioPagina: 10,
    })
  }

  // Actualizar la URL con los filtros aplicados
  const actualizarUrl = (filtros: FiltrosHechosType) => {
    const params = new URLSearchParams()

    Object.entries(filtros).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params.set(key, String(value))
      }
    })

    const queryString = params.toString()
    router.push(queryString ? `/colecciones/${handle}?${queryString}` : `/colecciones/${handle}`, { scroll: false })
  }

  const handleFiltrar = async (filtros: FiltrosHechosType) => {
    setIsLoading(true)

    // Limpiar filtros vacíos antes de aplicar
    const filtrosLimpios = Object.fromEntries(
      Object.entries(filtros).filter(([_, value]) =>
        value !== undefined && value !== null && value !== ""
      )
    ) as FiltrosHechosType

    setFiltrosAplicados(filtrosLimpios)

    // Actualizar URL con los filtros
    actualizarUrl(filtrosLimpios)

    try {
      const nuevaData = await ApiClient.obtenerHechosDeColeccion(handle, {
        ...filtrosLimpios,
        pagina: 0,
        tamanioPagina: 10,
      })
      setInitialData(nuevaData)
    } catch (error) {
      console.error('Error al filtrar hechos:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleLimpiar = async () => {
    setIsLoading(true)
    setFiltrosAplicados({})

    // Limpiar la URL
    router.push(`/colecciones/${handle}`, { scroll: false })

    try {
      const nuevaData = await ApiClient.obtenerHechosDeColeccion(handle, {
        pagina: 0,
        tamanioPagina: 10,
      })
      setInitialData(nuevaData)
    } catch (error) {
      console.error('Error al limpiar filtros:', error)
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading || !initialData) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Cargando hechos...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="h-full w-full flex flex-col">
      {/* Filtros */}
      <div className="flex-shrink-0">
        <FiltrosHechos
          onFiltrar={handleFiltrar}
          onLimpiar={handleLimpiar}
          filtrosIniciales={filtrosAplicados}
        />
      </div>

      {/* Contenido principal */}
      <div className="flex-1 min-h-0">
        <ColeccionDetailView
          coleccion={coleccion}
          initialData={initialData}
          fetchPage={fetchPage}
          backUrl="/colecciones"
          backLabel="Volver a Colecciones"
        />
      </div>
    </div>
  )
}
