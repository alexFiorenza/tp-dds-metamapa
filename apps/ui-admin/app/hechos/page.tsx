"use client"

import { useState, useEffect, Suspense } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { ApiClient } from "@/lib/api-client"
import { HechosMapView } from "@/components/hechos-map-view"
import { FiltrosHechos } from "@/components/filtros-hechos"
import type { RespuestaPaginadaDTO, HechoDTO, FiltrosHechos as FiltrosHechosType } from "@/types/api"

function HechosPageContent() {
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
          (filtrosDesdeUrl as any)[key] = numValue
        }
      } else {
        (filtrosDesdeUrl as any)[key] = value
      }
    })

    setFiltrosAplicados(filtrosDesdeUrl)
  }, [searchParams])

  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const filtros: FiltrosHechosType = {}
        searchParams.forEach((value, key) => {
          if (key === 'latitud' || key === 'longitud') {
            const numValue = parseFloat(value)
            if (!isNaN(numValue)) {
              (filtros as any)[key] = numValue
            }
          } else {
            (filtros as any)[key] = value
          }
        })

        const respuesta = await ApiClient.obtenerHechos({
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
  }, [searchParams])

  // Función para fetch de páginas
  const fetchPage = async (page: number) => {
    return await ApiClient.obtenerHechos({
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
    router.push(queryString ? `/hechos?${queryString}` : '/hechos', { scroll: false })
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
      const nuevaData = await ApiClient.obtenerHechos({
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
    router.push('/hechos', { scroll: false })

    try {
      const nuevaData = await ApiClient.obtenerHechos({
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
        <HechosMapView initialData={initialData} fetchPage={fetchPage} />
      </div>
    </div>
  )
}

export default function HechosPage() {
  return (
    <Suspense fallback={
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Cargando...</p>
        </div>
      </div>
    }>
      <HechosPageContent />
    </Suspense>
  )
}
