"use client"

import { useState, useEffect } from "react"
import { ApiClient } from "@/lib/api-client"
import { MapWrapper } from "@/components/map-wrapper"
import { FormularioColeccion } from "@/components/formulario-coleccion"
import { Button, Card, CardBody, Chip, Modal, ModalContent, ModalHeader, ModalBody, ModalFooter } from "@heroui/react"
import { useRouter } from "next/navigation"
import type { ColeccionDTO, ColeccionCreateDTO, FuenteDTO } from "@/types/api"
import { useAuth } from "@clerk/nextjs"
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from '@/components/layout-wrapper'

interface AdminColeccionesViewProps {
  coleccionesIniciales: ColeccionDTO[]
  totalElementos: number
}

export function AdminColeccionesView({ coleccionesIniciales, totalElementos }: AdminColeccionesViewProps) {
  const { getToken } = useAuth()
  const { isCollapsed } = useSidebarContext()
  const router = useRouter()
  const [colecciones, setColecciones] = useState<ColeccionDTO[]>(coleccionesIniciales)
  const [fuentes, setFuentes] = useState<FuenteDTO[]>([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)
  const [coleccionAEditar, setColeccionAEditar] = useState<ColeccionDTO | null>(null)
  const [coleccionAEliminar, setColeccionAEliminar] = useState<ColeccionDTO | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    cargarFuentes()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Efecto para redimensionar el mapa cuando cambie el estado del sidebar
  useEffect(() => {
    const timer = setTimeout(() => {
      window.dispatchEvent(new Event('resize'))
    }, 300)
    return () => clearTimeout(timer)
  }, [isCollapsed])

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
      setColeccionAEditar(null)
    } catch (error) {
      console.error("Error al crear colección:", error)
      throw error
    } finally {
      setLoading(false)
    }
  }

  const handleEditarColeccion = async (coleccion: ColeccionCreateDTO) => {
    if (!coleccionAEditar) return

    setLoading(true)
    try {
      const token = await getToken()
      const coleccionActualizada = await ApiClient.actualizarColeccion(coleccionAEditar.handle, coleccion, token)
      setColecciones(colecciones.map(c => c.handle === coleccionAEditar.handle ? coleccionActualizada : c))
      setIsModalOpen(false)
      setColeccionAEditar(null)
    } catch (error) {
      console.error("Error al editar colección:", error)
      throw error
    } finally {
      setLoading(false)
    }
  }

  const handleEliminarColeccion = async () => {
    if (!coleccionAEliminar) return

    setLoading(true)
    try {
      const token = await getToken()
      await ApiClient.eliminarColeccion(coleccionAEliminar.handle, token)
      setColecciones(colecciones.filter(c => c.handle !== coleccionAEliminar.handle))
      setIsDeleteModalOpen(false)
      setColeccionAEliminar(null)
    } catch (error) {
      console.error("Error al eliminar colección:", error)
      throw error
    } finally {
      setLoading(false)
    }
  }

  const abrirModalEdicion = (coleccion: ColeccionDTO) => {
    setColeccionAEditar(coleccion)
    setIsModalOpen(true)
  }

  const abrirModalEliminacion = (coleccion: ColeccionDTO) => {
    setColeccionAEliminar(coleccion)
    setIsDeleteModalOpen(true)
  }

  const cerrarModalCreacion = () => {
    setIsModalOpen(false)
    setColeccionAEditar(null)
  }

  const handleVerHechos = (handle: string) => {
    router.push(`/colecciones/${handle}`)
  }

  return (
    <>
      <div className="h-full w-full flex">
        {/* Sidebar - se oculta completamente cuando el sidebar principal está colapsado */}
        <AnimatePresence>
          {!isCollapsed && (
            <motion.div
              className="flex flex-col bg-content1 border-r border-divider"
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 420, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              transition={{ duration: 0.3, ease: "easeInOut" }}
            >
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
                          <Button
                            variant="flat"
                            size="sm"
                            startContent={<i className="ri-edit-line w-4 h-4" />}
                            onPress={() => abrirModalEdicion(coleccion)}
                          >
                            Editar
                          </Button>
                          <Button
                            variant="flat"
                            color="danger"
                            size="sm"
                            startContent={<i className="ri-delete-bin-line w-4 h-4" />}
                            onPress={() => abrirModalEliminacion(coleccion)}
                          >
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
            </motion.div>
          )}
        </AnimatePresence>

        {/* Map */}
        <div className="flex-1 relative min-w-0">
          <MapWrapper height="100%" width="100%" />
        </div>
      </div>

      {/* Modal de creación/edición */}
      <FormularioColeccion
        isOpen={isModalOpen}
        onClose={cerrarModalCreacion}
        onSubmit={coleccionAEditar ? handleEditarColeccion : handleCrearColeccion}
        fuentes={fuentes}
        titulo={coleccionAEditar ? "Editar Colección" : "Nueva Colección"}
        coleccionInicial={coleccionAEditar ? {
          titulo: coleccionAEditar.titulo,
          descripcion: coleccionAEditar.descripcion,
          fuentesIds: coleccionAEditar.fuentes.map(f => f.uuid),
          criteriosDePertenencia: coleccionAEditar.criteriosDePertenencia,
        } : undefined}
      />

      {/* Modal de confirmación de eliminación */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          setIsDeleteModalOpen(false)
          setColeccionAEliminar(null)
        }}
        size="md"
        backdrop="opaque"
        placement="center"
        classNames={{
          backdrop: "bg-black/70 backdrop-blur-md",
          wrapper: "items-center",
          base: "bg-content1",
        }}
      >
        <ModalContent className="bg-content1">
          {(onClose) => (
            <>
              <ModalHeader className="flex flex-col gap-1 border-b border-divider px-6 py-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 flex items-center justify-center bg-danger/10 rounded-lg">
                    <i className="ri-alert-line w-5 h-5 text-danger" />
                  </div>
                  <span>Confirmar Eliminación</span>
                </div>
              </ModalHeader>
              <ModalBody className="px-6 py-6">
                <p className="text-default-600">
                  ¿Estás seguro de que deseas eliminar la colección{" "}
                  <span className="font-semibold text-foreground">
                    "{coleccionAEliminar?.titulo}"
                  </span>
                  ?
                </p>
                <p className="text-sm text-danger mt-2">
                  Esta acción no se puede deshacer.
                </p>
              </ModalBody>
              <ModalFooter className="border-t border-divider px-6 py-4">
                <Button
                  variant="light"
                  onPress={() => {
                    setIsDeleteModalOpen(false)
                    setColeccionAEliminar(null)
                    onClose()
                  }}
                  isDisabled={loading}
                >
                  Cancelar
                </Button>
                <Button
                  color="danger"
                  onPress={handleEliminarColeccion}
                  isLoading={loading}
                  startContent={!loading ? <i className="ri-delete-bin-line" /> : undefined}
                >
                  {loading ? "Eliminando..." : "Eliminar"}
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </>
  )
}
