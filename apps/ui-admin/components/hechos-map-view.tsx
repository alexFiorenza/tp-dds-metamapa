'use client'

import { useState, useEffect } from 'react'
import { MapWrapper } from './map-wrapper'
import { HechoListItem } from './hecho-list-item'
import { Button, Pagination, Spinner } from '@heroui/react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { HechoDTO, RespuestaPaginadaDTO } from '@/types/api'

interface HechosMapViewProps {
  initialData: RespuestaPaginadaDTO<HechoDTO>
  fetchPage: (page: number) => Promise<RespuestaPaginadaDTO<HechoDTO>>
}

export function HechosMapView({ initialData, fetchPage }: HechosMapViewProps) {
  const [selectedHechoId, setSelectedHechoId] = useState<string | undefined>()
  const [hoveredHechoId, setHoveredHechoId] = useState<string | undefined>()
  const [currentPage, setCurrentPage] = useState(0) // Backend usa 0-indexed
  const [data, setData] = useState<RespuestaPaginadaDTO<HechoDTO>>(initialData)
  const [isLoading, setIsLoading] = useState(false)

  // Fetch data cuando cambia la página
  useEffect(() => {
    // Solo fetch si no es la página inicial
    if (currentPage === 0) {
      setData(initialData)
      return
    }

    const loadPage = async () => {
      setIsLoading(true)
      try {
        const newData = await fetchPage(currentPage)
        setData(newData)
      } catch (error) {
        console.error('Error loading page:', error)
      } finally {
        setIsLoading(false)
      }
    }

    loadPage()
  }, [currentPage, fetchPage, initialData])

  return (
    <div className="flex h-full">
      {/* Sidebar con lista de hechos */}
      <div className="w-96 flex flex-col bg-content1 border-r border-divider">
        {/* Header */}
        <div className="p-6 border-b border-divider">
          <h2 className="text-2xl font-bold text-foreground mb-2">Hechos</h2>
          <p className="text-sm text-default-500">
            {data.totalElementos} {data.totalElementos === 1 ? 'hecho' : 'hechos'} en total
          </p>
        </div>

        {/* Lista de hechos */}
        <div className="flex-1 overflow-y-auto p-3 space-y-2 relative">
          {isLoading && (
            <div className="absolute inset-0 bg-content1/80 backdrop-blur-sm flex items-center justify-center z-10">
              <Spinner size="lg" color="primary" />
            </div>
          )}
          {data.datos.map((hecho) => (
            <HechoListItem
              key={hecho.uuid}
              hecho={hecho}
              isSelected={selectedHechoId === hecho.uuid}
              onClick={() => setSelectedHechoId(hecho.uuid === selectedHechoId ? undefined : hecho.uuid)}
              onHover={(hovered) => setHoveredHechoId(hovered ? hecho.uuid : undefined)}
            />
          ))}
        </div>

        {/* Paginación */}
        {data.totalPaginas > 1 && (
          <div className="p-4 border-t border-divider">
            <div className="flex items-center justify-between mb-3">
              <Button
                isIconOnly
                size="sm"
                variant="flat"
                isDisabled={!data.tieneAnterior || isLoading}
                onPress={() => setCurrentPage(p => Math.max(0, p - 1))}
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>

              <span className="text-sm text-default-600">
                Página {currentPage + 1} de {data.totalPaginas}
              </span>

              <Button
                isIconOnly
                size="sm"
                variant="flat"
                isDisabled={!data.tieneSiguiente || isLoading}
                onPress={() => setCurrentPage(p => Math.min(data.totalPaginas - 1, p + 1))}
              >
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>

            <Pagination
              total={data.totalPaginas}
              page={currentPage + 1} // UI muestra 1-indexed
              onChange={(page) => setCurrentPage(page - 1)} // Backend usa 0-indexed
              size="sm"
              showControls={false}
              isDisabled={isLoading}
              classNames={{
                wrapper: "gap-1",
                item: "w-8 h-8 text-xs"
              }}
            />
          </div>
        )}
      </div>

      {/* Mapa */}
      <div className="flex-1 relative">
        <MapWrapper
          hechos={data.datos}
          selectedHechoId={selectedHechoId}
          hoveredHechoId={hoveredHechoId}
          onHechoSelect={(hecho) => setSelectedHechoId(hecho?.uuid)}
        />
      </div>
    </div>
  )
}
