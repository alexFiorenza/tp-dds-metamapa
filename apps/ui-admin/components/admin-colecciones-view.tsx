"use client"

import { useState, useEffect } from "react"
import { ApiClient } from "@/lib/api-client"
import { MapWrapper } from "@/components/map-wrapper"
import { FormularioColeccion } from "@/components/formulario-coleccion"
import { Button, Card, CardBody, Chip } from "@heroui/react"
import { useRouter } from "next/navigation"
import type { ColeccionDTO, ColeccionCreateDTO, FuenteDTO } from "@/types/api"
import { useAuth } from "@clerk/nextjs"

interface AdminColeccionesViewProps {
  coleccionesIniciales: ColeccionDTO[]
  totalElementos: number
}

export function AdminColeccionesView({ coleccionesIniciales, totalElementos }: AdminColeccionesViewProps) {
  const { getToken } = useAuth()
  const router = useRouter()
  const [colecciones, setColecciones] = useState<ColeccionDTO[]>(coleccionesIniciales)
  const [fuentes, setFuentes] = useState<FuenteDTO[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    cargarFuentes()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const cargarFuentes = async () => {
    try {
      const token = await getToken()
      console.log("Token obtenido:", token ? "✓" : "✗")
      const fuentesData = await ApiClient.obtenerFuentes(token)
      console.log("Fuentes cargadas:", fuentesData)
      setFuentes(fuentesData)
    } catch (error) {
      console.error("Error al cargar fuentes:", error)
      // Si falla, usar array vacío para que no rompa el componente
      setFuentes([])
    }
  }

  const handleCrearColeccion = async (coleccion: ColeccionCreateDTO) => {
    setLoading(true)
    try {
      const token = await getToken()
      const nuevaColeccion = await ApiClient.crearColeccion(coleccion, token)
      setColecciones([nuevaColeccion, ...colecciones])
      setIsModalOpen(false)
    } catch (error) {
      console.error("Error al crear colección:", error)
      throw error
    } finally {
      setLoading(false)
    }
  }

  const handleVerHechos = (handle: string) => {
    router.push(`/colecciones/${handle}`)
  }

  return (
    <>
      <div className="h-full w-full flex">
        {/* Sidebar */}
        <div className="w-[420px] flex flex-col bg-content1 border-r border-divider">
          {/* Header */}
          <div className="p-6 border-b border-divider">
            <Button 
              variant="light" 
              size="sm" 
              startContent={<i className="ri-arrow-left-line w-4 h-4" />} 
              className="mb-4"
              onPress={() => router.push("/administrador")}
            >
              Volver
            </Button>
            <div className="flex items-center justify-between mb-2">
              <h1 className="text-2xl font-bold text-foreground">Colecciones</h1>
              <Button
                color="primary"
                size="sm"
                startContent={<i className="ri-add-line w-4 h-4" />}
                onPress={() => setIsModalOpen(true)}
              >
                Nueva
              </Button>
            </div>
            <p className="text-sm text-default-500">{colecciones.length} colecciones</p>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="space-y-3">
              {colecciones.map((coleccion) => (
                <Card key={coleccion.handle}>
                  <CardBody className="p-4">
                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 flex items-center justify-center bg-primary/10 rounded-lg shrink-0">
                        <i className="ri-folder-line w-5 h-5 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-base mb-1 text-foreground">{coleccion.titulo}</h3>
                        <p className="text-sm text-default-500 line-clamp-2 mb-3">{coleccion.descripcion}</p>

                        {coleccion.criteriosDePertenencia.length > 0 && (
                          <div className="flex flex-wrap gap-1.5 mb-3">
                            {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                              <Chip key={idx} size="sm" variant="flat" color="secondary">
                                {criterio.categoria}
                              </Chip>
                            ))}
                          </div>
                        )}

                        <div className="flex gap-2 flex-wrap">
                          <Button 
                            color="primary" 
                            size="sm" 
                            startContent={<i className="ri-eye-line w-4 h-4" />}
                            onPress={() => handleVerHechos(coleccion.handle)}
                          >
                            Ver Hechos
                          </Button>
                          <Button variant="flat" size="sm" startContent={<i className="ri-edit-line w-4 h-4" />}>
                            Editar
                          </Button>
                          <Button variant="flat" color="danger" size="sm" startContent={<i className="ri-delete-bin-line w-4 h-4" />}>
                            Eliminar
                          </Button>
                        </div>
                      </div>
                    </div>
                  </CardBody>
                </Card>
              ))}
            </div>
          </div>
        </div>

        {/* Map */}
        <div className="flex-1">
          <MapWrapper />
        </div>
      </div>

      {/* Modal de creación */}
      <FormularioColeccion
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCrearColeccion}
        fuentes={fuentes}
        titulo="Nueva Colección"
      />
    </>
  )
}
