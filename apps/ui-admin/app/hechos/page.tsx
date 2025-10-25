"use client"

import { useState, useEffect } from "react"
import { ApiClient } from "@/lib/api-client"
import { HechosMapView } from "@/components/hechos-map-view"
import { FiltrosHechos } from "@/components/filtros-hechos"
import type { RespuestaPaginadaDTO, HechoDTO, FiltrosHechos as FiltrosHechosType } from "@/types/api"

interface HechosPageProps {
  searchParams: Promise<{
    categoria?: string
    titulo?: string
  }>
}

export default function HechosPage({ searchParams }: HechosPageProps) {
  const [initialData, setInitialData] = useState<RespuestaPaginadaDTO<HechoDTO> | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [filtrosAplicados, setFiltrosAplicados] = useState<FiltrosHechosType>({})

  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const params = await searchParams
        const respuesta = await ApiClient.obtenerHechos({
          pagina: 0,
          tamanioPagina: 10,
          categoria: params.categoria,
          titulo: params.titulo,
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
    const params = await searchParams
    return await ApiClient.obtenerHechos({
      ...filtrosAplicados,
      pagina: page,
      tamanioPagina: 10,
      categoria: params.categoria,
      titulo: params.titulo,
    })
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
    
    try {
      const params = await searchParams
      const nuevaData = await ApiClient.obtenerHechos({
        ...filtrosLimpios,
        pagina: 0,
        tamanioPagina: 10,
        categoria: params.categoria,
        titulo: params.titulo,
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
    
    try {
      const params = await searchParams
      const nuevaData = await ApiClient.obtenerHechos({
        pagina: 0,
        tamanioPagina: 10,
        categoria: params.categoria,
        titulo: params.titulo,
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
        />
      </div>

      {/* Contenido principal */}
      <div className="flex-1 min-h-0">
        <HechosMapView initialData={initialData} fetchPage={fetchPage} />
      </div>
    </div>
  )
}
