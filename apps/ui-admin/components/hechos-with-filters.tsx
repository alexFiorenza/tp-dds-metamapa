'use client'

import { useState, useEffect } from 'react'
import { HechosMapView } from './hechos-map-view'
import { FiltrosHechos } from './filtros-hechos'
import { ApiClient } from '@/lib/api-client'
import { Spinner } from '@heroui/react'
import type { HechoDTO, RespuestaPaginadaDTO, FiltrosHechos as FiltrosHechosType } from '@/types/api'

interface HechosWithFiltersProps {
  initialData: RespuestaPaginadaDTO<HechoDTO>
}

export function HechosWithFilters({ initialData }: HechosWithFiltersProps) {
  const [data, setData] = useState<RespuestaPaginadaDTO<HechoDTO>>(initialData)
  const [isLoading, setIsLoading] = useState(false)
  const [filtrosAplicados, setFiltrosAplicados] = useState<FiltrosHechosType>({})

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
      const nuevaData = await ApiClient.obtenerHechos({
        ...filtrosLimpios,
        pagina: 0,
        tamanioPagina: 10
      })
      setData(nuevaData)
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
      const nuevaData = await ApiClient.obtenerHechos({
        pagina: 0,
        tamanioPagina: 10
      })
      setData(nuevaData)
    } catch (error) {
      console.error('Error al limpiar filtros:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const fetchPage = async (page: number) => {
    // Solo incluir filtros que tengan valores válidos
    const filtrosLimpios = Object.fromEntries(
      Object.entries(filtrosAplicados).filter(([_, value]) => 
        value !== undefined && value !== null && value !== ""
      )
    ) as FiltrosHechosType


    return await ApiClient.obtenerHechos({
      ...filtrosLimpios,
      pagina: page,
      tamanioPagina: 10
    })
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
      <div className="flex-1 min-h-0 relative">
        {isLoading && (
          <div className="absolute inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center z-10">
            <Spinner size="lg" color="primary" />
          </div>
        )}
        <HechosMapView 
          initialData={data}
          fetchPage={fetchPage}
        />
      </div>
    </div>
  )
}
