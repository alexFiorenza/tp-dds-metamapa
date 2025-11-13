"use client"

import { useState, useEffect } from "react"
import { useAuth } from "@clerk/nextjs"
import { ApiClient } from "@/lib/api-client"
import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody, Chip, ButtonGroup } from "@heroui/react"
import { useRouter } from "next/navigation"
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from '@/components/layout-wrapper'

export default function AdminSolicitudesPage() {
  const { getToken } = useAuth()
  const { isCollapsed } = useSidebarContext()
  const router = useRouter()
  const [solicitudes, setSolicitudes] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loadSolicitudes = async () => {
      try {
        const token = await getToken()
        const respuesta = await ApiClient.obtenerSolicitudes(0, 10, token)
        setSolicitudes(respuesta.datos)
        setTotal(respuesta.totalElementos)
      } catch (error) {
        console.error("Error loading solicitudes:", error)
      } finally {
        setIsLoading(false)
      }
    }

    loadSolicitudes()
  }, [getToken])

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
            <Button className="flex-1">Todas</Button>
            <Button className="flex-1">Pendientes</Button>
            <Button className="flex-1">Resueltas</Button>
          </ButtonGroup>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-3">
            {solicitudes.map((solicitud) => (
              <Card key={solicitud.uuid}>
                <CardBody className="p-4">
                  <div className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <Chip
                        size="sm"
                        variant="flat"
                        color={
                          solicitud.estado === "PENDIENTE"
                            ? "warning"
                            : solicitud.estado === "ACEPTADA"
                              ? "success"
                              : "default"
                        }
                        startContent={solicitud.estado === "PENDIENTE" ? <i className="ri-time-line w-3 h-3" /> : null}
                      >
                        {solicitud.estado}
                      </Chip>
                      <span className="text-xs text-default-500">
                        {new Date(solicitud.fechaSolicitud).toLocaleDateString("es-AR", {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric'
                        })}
                      </span>
                    </div>

                    <div>
                      <p className="text-sm font-semibold mb-1 text-foreground">Hecho: {solicitud.hecho}</p>
                      <p className="text-sm text-default-600">{solicitud.texto}</p>
                    </div>

                    {solicitud.estado === "PENDIENTE" && (
                      <div className="flex gap-2">
                        <Button
                          color="success"
                          size="sm"
                          className="flex-1"
                          startContent={<i className="ri-check-line w-4 h-4" />}
                        >
                          Aceptar
                        </Button>
                        <Button
                          color="danger"
                          variant="flat"
                          size="sm"
                          className="flex-1"
                          startContent={<i className="ri-close-line w-4 h-4" />}
                        >
                          Rechazar
                        </Button>
                      </div>
                    )}
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
  )
}
