'use client'

import { useState, useEffect } from 'react'
import { MapWrapper } from './map-wrapper'
import { HechoListItem } from './hecho-list-item'
import { Button, Pagination, Card, CardBody, Chip, Spinner } from '@heroui/react'
import { ChevronLeft, ChevronRight, Folder, Edit, Trash2, ArrowLeft } from 'lucide-react'
import Link from 'next/link'
import type { HechoDTO, RespuestaPaginadaDTO } from '@/types/api'
import type { ColeccionDTO } from '@/types/api'

interface ColeccionDetailViewProps {
  coleccion: ColeccionDTO
  initialData: RespuestaPaginadaDTO<HechoDTO>
  fetchPage: (page: number) => Promise<RespuestaPaginadaDTO<HechoDTO>>
}

export function ColeccionDetailView({ coleccion, initialData, fetchPage }: ColeccionDetailViewProps) {
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
      {/* Sidebar con detalles de colección */}
      <div className="w-96 flex flex-col bg-content1 border-r border-divider">
        {/* Header */}
        <div className="p-6 border-b border-divider">
          <Link href="/administrador/colecciones">
            <Button variant="light" size="sm" startContent={<ArrowLeft className="w-4 h-4" />} className="mb-4">
              Volver a Colecciones
            </Button>
          </Link>

          <div className="flex items-start gap-3 mb-4">
            <div className="w-12 h-12 flex items-center justify-center bg-primary/10 rounded-xl shrink-0">
              <Folder className="w-6 h-6 text-primary" />
            </div>
            <div className="flex-1 min-w-0">
              <h1 className="text-xl font-bold text-foreground mb-1">{coleccion.titulo}</h1>
              <p className="text-sm text-default-500">{coleccion.descripcion}</p>
            </div>
          </div>

          {/* Metadata */}
          <Card>
            <CardBody className="p-3">
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="font-semibold text-foreground">Handle:</span>
                  <span className="text-default-600">{coleccion.handle}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-semibold text-foreground">Hechos:</span>
                  <span className="text-default-600">{data.totalElementos}</span>
                </div>
              </div>
            </CardBody>
          </Card>

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

          {/* Actions */}
          <div className="flex gap-2 mt-4">
            <Button variant="flat" size="sm" className="flex-1" startContent={<Edit className="w-4 h-4" />}>
              Editar
            </Button>
            <Button variant="flat" color="danger" size="sm" className="flex-1" startContent={<Trash2 className="w-4 h-4" />}>
              Eliminar
            </Button>
          </div>
        </div>

        {/* Lista de hechos */}
        <div className="p-4 border-b border-divider">
          <h2 className="text-sm font-semibold text-foreground">
            Hechos en esta colección ({data.totalElementos})
          </h2>
        </div>

        <div className="flex-1 overflow-y-auto p-3 space-y-2 relative">
          {isLoading && (
            <div className="absolute inset-0 bg-content1/80 backdrop-blur-sm flex items-center justify-center z-10">
              <Spinner size="lg" color="primary" />
            </div>
          )}
          {data.datos.length === 0 ? (
            <div className="text-center py-12 text-default-400">
              <Folder className="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p className="text-sm">No hay hechos en esta colección</p>
            </div>
          ) : (
            data.datos.map((hecho) => (
              <HechoListItem
                key={hecho.uuid}
                hecho={hecho}
                isSelected={selectedHechoId === hecho.uuid}
                onClick={() => setSelectedHechoId(hecho.uuid === selectedHechoId ? undefined : hecho.uuid)}
                onHover={(hovered) => setHoveredHechoId(hovered ? hecho.uuid : undefined)}
              />
            ))
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
