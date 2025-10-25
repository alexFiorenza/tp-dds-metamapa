'use client'

import { useState, useEffect } from 'react'
import { MapWrapper } from './map-wrapper'
import { HechoListItem } from './hecho-list-item'
import { Button, Pagination, Spinner } from '@heroui/react'
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from './layout-wrapper'
import type { HechoDTO, RespuestaPaginadaDTO } from '@/types/api'

interface HechosMapViewProps {
  initialData: RespuestaPaginadaDTO<HechoDTO>
  fetchPage: (page: number) => Promise<RespuestaPaginadaDTO<HechoDTO>>
}

export function HechosMapView({ initialData, fetchPage }: HechosMapViewProps) {
  const { isCollapsed } = useSidebarContext()
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

  // Efecto para redimensionar el mapa cuando cambie el estado del sidebar
  useEffect(() => {
    const timer = setTimeout(() => {
      // Disparar un evento de resize para que el mapa se redimensione
      window.dispatchEvent(new Event('resize'))
    }, 300) // Esperar a que termine la animación del sidebar

    return () => clearTimeout(timer)
  }, [isCollapsed])

  return (
    <div className="flex h-full w-full">
      {/* Sidebar con lista de hechos - se oculta completamente cuando el sidebar principal está colapsado */}
      <AnimatePresence>
        {!isCollapsed && (
          <motion.div 
            className="flex flex-col bg-content1 border-r border-divider"
            initial={{ width: 0, opacity: 0 }}
            animate={{ width: 384, opacity: 1 }} // 384px = w-96
            exit={{ width: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
          >
            {/* Header */}
            <div className="p-6 border-b border-divider">
              <h2 className="text-2xl font-bold text-foreground mb-2">Hechos</h2>
              <p className="text-sm text-default-500">
                {data.totalElementos} {data.totalElementos === 1 ? 'hecho' : 'hechos'} en total
              </p>
            </div>

            {/* Lista de hechos */}
            <div className="flex-1 overflow-y-auto p-3 relative">
              {isLoading && (
                <div className="absolute inset-0 bg-content1/80 backdrop-blur-sm flex items-center justify-center z-10">
                  <Spinner size="lg" color="primary" />
                </div>
              )}
              <motion.div
                key={currentPage}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="space-y-2"
              >
                {data.datos.map((hecho) => (
                  <HechoListItem
                    key={hecho.uuid}
                    hecho={hecho}
                    isSelected={selectedHechoId === hecho.uuid}
                    onClick={() => setSelectedHechoId(hecho.uuid === selectedHechoId ? undefined : hecho.uuid)}
                    onHover={(hovered) => setHoveredHechoId(hovered ? hecho.uuid : undefined)}
                  />
                ))}
              </motion.div>
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
                    <i className="ri-arrow-left-s-line w-4 h-4" />
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
                    <i className="ri-arrow-right-s-line w-4 h-4" />
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
          </motion.div>
        )}
      </AnimatePresence>

      {/* Mapa */}
      <div className="flex-1 relative min-w-0">
        <MapWrapper
          hechos={data.datos}
          selectedHechoId={selectedHechoId}
          hoveredHechoId={hoveredHechoId}
          onHechoSelect={(hecho) => setSelectedHechoId(hecho?.uuid)}
          height="100%"
          width="100%"
        />
      </div>
    </div>
  )
}
