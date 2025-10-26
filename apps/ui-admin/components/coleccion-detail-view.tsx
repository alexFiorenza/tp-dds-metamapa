'use client'

import { useState, useEffect } from 'react'
import { MapWrapper } from './map-wrapper'
import { HechoListItem } from './hecho-list-item'
import { Button, Pagination, Card, CardBody, Chip, Spinner } from '@heroui/react'
import { motion, AnimatePresence } from 'motion/react'
import Link from 'next/link'
import { useUserRole } from '@/hooks/use-user-role'
import { useSidebarContext } from './layout-wrapper'
import type { HechoDTO, RespuestaPaginadaDTO } from '@/types/api'
import type { ColeccionDTO } from '@/types/api'

interface ColeccionDetailViewProps {
  coleccion: ColeccionDTO
  initialData: RespuestaPaginadaDTO<HechoDTO>
  fetchPage: (page: number) => Promise<RespuestaPaginadaDTO<HechoDTO>>
  backUrl?: string
  backLabel?: string
}

export function ColeccionDetailView({ coleccion, initialData, fetchPage, backUrl = "/colecciones", backLabel = "Volver a Colecciones" }: ColeccionDetailViewProps) {
  const { isAdmin } = useUserRole()
  const { isCollapsed } = useSidebarContext()
  const [selectedHechoId, setSelectedHechoId] = useState<string | undefined>()
  const [hoveredHechoId, setHoveredHechoId] = useState<string | undefined>()
  const [currentPage, setCurrentPage] = useState(0) // Backend usa 0-indexed
  const [data, setData] = useState<RespuestaPaginadaDTO<HechoDTO>>(initialData)
  const [isLoading, setIsLoading] = useState(false)

  // Resetear página cuando cambian los datos iniciales (por filtros)
  useEffect(() => {
    setCurrentPage(0)
    setData(initialData)
  }, [initialData])

  // Fetch data cuando cambia la página
  useEffect(() => {
    // Solo fetch si no es la página inicial
    if (currentPage === 0) {
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
  }, [currentPage, fetchPage])

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
      {/* Sidebar con detalles de colección - se oculta completamente cuando el sidebar principal está colapsado */}
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
          <Link href={backUrl}>
            <Button variant="light" size="sm" startContent={<i className="ri-arrow-left-line w-4 h-4" />} className="mb-4">
              {backLabel}
            </Button>
          </Link>

          <div className="flex items-start gap-3 mb-4">
            <div className="w-12 h-12 flex items-center justify-center bg-primary/10 rounded-xl shrink-0">
              <i className="ri-folder-line w-6 h-6 text-primary" />
            </div>
            <div className="flex-1 min-w-0">
              <h1 className="text-xl font-bold text-foreground mb-1">{coleccion.titulo}</h1>
              <p className="text-sm text-default-500">{coleccion.descripcion}</p>
            </div>
          </div>

          {/* Criterios de pertenencia */}
          {coleccion.criteriosDePertenencia.length > 0 && (
            <div className="mt-4">
              <h3 className="text-sm font-semibold mb-2 text-foreground">Criterios de Pertenencia</h3>
              <div className="flex flex-wrap gap-1.5">
                {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                  <Chip key={idx} size="sm" variant="flat" color="secondary">
                    {criterio.categoria}
                  </Chip>
                ))}
              </div>
            </div>
          )}

          {/* Actions - Solo para administradores */}
          {isAdmin && (
            <div className="flex gap-2 mt-4">
              <Button variant="flat" size="sm" className="flex-1" startContent={<i className="ri-edit-line w-4 h-4" />}>
                Editar
              </Button>
              <Button variant="flat" color="danger" size="sm" className="flex-1" startContent={<i className="ri-delete-bin-line w-4 h-4" />}>
                Eliminar
              </Button>
            </div>
          )}
        </div>

        {/* Lista de hechos */}
        <div className="p-4 border-b border-divider">
          <h2 className="text-sm font-semibold text-foreground">
            Hechos en esta colección ({data.totalElementos})
          </h2>
        </div>

        <div className="flex-1 overflow-y-auto p-3 relative">
          {isLoading && (
            <div className="absolute inset-0 bg-content1/80 backdrop-blur-sm flex items-center justify-center z-10">
              <Spinner size="lg" color="primary" />
            </div>
          )}
          {data.datos.length === 0 ? (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="text-center py-12 text-default-400"
            >
              <i className="ri-folder-line w-12 h-12 mx-auto mb-3 opacity-50" />
              <p className="text-sm">No hay hechos en esta colección</p>
            </motion.div>
          ) : (
            <AnimatePresence mode="wait">
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
            </AnimatePresence>
          )}
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
