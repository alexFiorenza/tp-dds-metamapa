"use client"

import { useState, useEffect } from "react"
import { useAuth } from "@clerk/nextjs"
import { ApiClient } from "@/lib/api-client"
import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody, Chip, ButtonGroup, Accordion, AccordionItem, Spinner } from "@heroui/react"
import { useRouter } from "next/navigation"
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from '@/components/layout-wrapper'
import type { HechoDTO, SolicitudEliminacionDTO } from "@/types/api"

export default function AdminSolicitudesPage() {
  const { getToken } = useAuth()
  const { isCollapsed } = useSidebarContext()
  const router = useRouter()
  const [solicitudes, setSolicitudes] = useState<SolicitudEliminacionDTO[]>([])
  const [total, setTotal] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [procesando, setProcesando] = useState<Record<string, boolean>>({})
  const [filtro, setFiltro] = useState<"todas" | "pendientes" | "resueltas">("todas")
  const [expandidas, setExpandidas] = useState<Set<string>>(new Set())

  const loadSolicitudes = async () => {
    try {
      setIsLoading(true)
      const token = await getToken()
      const respuesta = await ApiClient.obtenerSolicitudes(0, 50, token)
      setSolicitudes(respuesta.datos)
      setTotal(respuesta.totalElementos)
    } catch (error) {
      console.error("Error loading solicitudes:", error)
    } finally {
      setIsLoading(false)
    }
  }

  const toggleExpand = (solicitudUuid: string) => {
    const newExpandidas = new Set(expandidas)
    if (newExpandidas.has(solicitudUuid)) {
      newExpandidas.delete(solicitudUuid)
    } else {
      newExpandidas.add(solicitudUuid)
    }
    setExpandidas(newExpandidas)
  }

  useEffect(() => {
    loadSolicitudes()
  }, [getToken])

  const handleAceptar = async (uuid: string) => {
    try {
      setProcesando(prev => ({ ...prev, [uuid]: true }))
      const token = await getToken()
      await ApiClient.aceptarSolicitud(uuid, token)

      // Recargar solicitudes
      await loadSolicitudes()
    } catch (error) {
      console.error("Error al aceptar solicitud:", error)
      alert("Error al aceptar la solicitud")
    } finally {
      setProcesando(prev => ({ ...prev, [uuid]: false }))
    }
  }

  const handleRechazar = async (uuid: string) => {
    try {
      setProcesando(prev => ({ ...prev, [uuid]: true }))
      const token = await getToken()
      await ApiClient.rechazarSolicitud(uuid, token)

      // Recargar solicitudes
      await loadSolicitudes()
    } catch (error) {
      console.error("Error al rechazar solicitud:", error)
      alert("Error al rechazar la solicitud")
    } finally {
      setProcesando(prev => ({ ...prev, [uuid]: false }))
    }
  }

  // Efecto para redimensionar el mapa cuando cambie el estado del sidebar
  useEffect(() => {
    const timer = setTimeout(() => {
      window.dispatchEvent(new Event('resize'))
    }, 300)
    return () => clearTimeout(timer)
  }, [isCollapsed])

  if (isLoading) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-default-500">Cargando solicitudes...</p>
        </div>
      </div>
    )
  }

  return (
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
          <h1 className="text-2xl font-bold text-foreground mb-2">Solicitudes de Eliminación</h1>
          <p className="text-sm text-default-500">{total} solicitudes</p>
        </div>

        {/* Filters */}
        <div className="p-4 border-b border-divider">
          <ButtonGroup className="w-full" size="sm" variant="bordered">
            <Button
              className="flex-1"
              variant={filtro === "todas" ? "solid" : "bordered"}
              onPress={() => setFiltro("todas")}
            >
              Todas
            </Button>
            <Button
              className="flex-1"
              variant={filtro === "pendientes" ? "solid" : "bordered"}
              onPress={() => setFiltro("pendientes")}
            >
              Pendientes
            </Button>
            <Button
              className="flex-1"
              variant={filtro === "resueltas" ? "solid" : "bordered"}
              onPress={() => setFiltro("resueltas")}
            >
              Resueltas
            </Button>
          </ButtonGroup>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-3">
            {solicitudes
              .filter(s => {
                if (filtro === "pendientes") return s.estado === "ACTIVO"
                if (filtro === "resueltas") return s.estado === "OCULTO"
                return true
              })
              .map((solicitud) => {
                const isExpanded = expandidas.has(solicitud.uuid)
                const hecho = solicitud.hechoDTO

                return (
                  <Card key={solicitud.uuid} className="border-l-4" style={{ borderLeftColor: solicitud.estado === "ACTIVO" ? "#f59e0b" : "#6b7280" }}>
                    <CardBody className="p-4">
                      <div className="space-y-3">
                        {/* Header con estado y fecha */}
                        <div className="flex items-start justify-between gap-2">
                          <Chip
                            size="sm"
                            variant="flat"
                            color={solicitud.estado === "ACTIVO" ? "warning" : "default"}
                            startContent={solicitud.estado === "ACTIVO" ? <i className="ri-time-line w-3 h-3" /> : <i className="ri-checkbox-circle-line w-3 h-3" />}
                          >
                            {solicitud.estado === "ACTIVO" ? "PENDIENTE" : "PROCESADA"}
                          </Chip>
                          <span className="text-xs text-default-500">
                            {new Date(solicitud.fechaSolicitud).toLocaleDateString("es-AR", {
                              day: '2-digit',
                              month: 'short',
                              year: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit'
                            })}
                          </span>
                        </div>

                        {/* Motivo de la solicitud */}
                        <div>
                          <p className="text-xs text-default-500 mb-1">Motivo de eliminación:</p>
                          <p className="text-sm text-default-700 line-clamp-2">{solicitud.texto}</p>
                        </div>

                        {/* Botón para expandir detalles del hecho */}
                        <Button
                          size="sm"
                          variant="flat"
                          className="w-full"
                          startContent={<i className={`${isExpanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'} w-4 h-4`} />}
                          onPress={() => toggleExpand(solicitud.uuid)}
                        >
                          {isExpanded ? 'Ocultar' : 'Ver'} detalles del hecho
                        </Button>

                        {/* Detalles expandidos del hecho */}
                        {isExpanded && (
                          <div className="p-3 bg-default-50 rounded-lg space-y-2">
                            {!hecho ? (
                              <div className="flex items-center justify-center py-4">
                                <span className="text-sm text-default-500">No se pudo cargar el hecho</span>
                              </div>
                            ) : (
                              <>
                                <div>
                                  <p className="text-xs text-default-500">Título</p>
                                  <p className="text-sm font-semibold text-foreground">{hecho.titulo}</p>
                                </div>
                                <div>
                                  <p className="text-xs text-default-500">Descripción</p>
                                  <p className="text-sm text-default-600">{hecho.descripcion}</p>
                                </div>
                                <div className="grid grid-cols-2 gap-2">
                                  <div>
                                    <p className="text-xs text-default-500">Categoría</p>
                                    <Chip size="sm" variant="flat" color="primary">{hecho.categoria}</Chip>
                                  </div>
                                  <div>
                                    <p className="text-xs text-default-500">Fecha acontecimiento</p>
                                    <p className="text-xs text-default-600">{new Date(hecho.fechaAcontecimiento).toLocaleDateString("es-AR")}</p>
                                  </div>
                                </div>
                                <div>
                                  <p className="text-xs text-default-500">UUID del hecho</p>
                                  <p className="text-xs font-mono text-default-600 break-all">{hecho.uuid}</p>
                                </div>
                                {hecho.etiquetas && hecho.etiquetas.length > 0 && (
                                  <div>
                                    <p className="text-xs text-default-500 mb-1">Etiquetas</p>
                                    <div className="flex flex-wrap gap-1">
                                      {hecho.etiquetas.map(etiqueta => (
                                        <Chip key={etiqueta} size="sm" variant="flat">{etiqueta}</Chip>
                                      ))}
                                    </div>
                                  </div>
                                )}
                              </>
                            )}
                          </div>
                        )}

                        {/* Botones de acción */}
                        {solicitud.estado === "ACTIVO" && (
                          <div className="flex gap-2 pt-2">
                            <Button
                              color="success"
                              size="sm"
                              className="flex-1"
                              startContent={!procesando[solicitud.uuid] && <i className="ri-check-line w-4 h-4" />}
                              onPress={() => handleAceptar(solicitud.uuid)}
                              isLoading={procesando[solicitud.uuid]}
                              isDisabled={procesando[solicitud.uuid]}
                            >
                              Aceptar
                            </Button>
                            <Button
                              color="danger"
                              variant="flat"
                              size="sm"
                              className="flex-1"
                              startContent={!procesando[solicitud.uuid] && <i className="ri-close-line w-4 h-4" />}
                              onPress={() => handleRechazar(solicitud.uuid)}
                              isLoading={procesando[solicitud.uuid]}
                              isDisabled={procesando[solicitud.uuid]}
                            >
                              Rechazar
                            </Button>
                          </div>
                        )}
                      </div>
                    </CardBody>
                  </Card>
                )
              })}
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
  )
}
