'use client'

import { useEffect } from 'react'
import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody, Chip } from "@heroui/react"
import Link from "next/link"
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from '@/components/layout-wrapper'
import type { ColeccionDTO } from "@/types/api"

interface ColeccionesPageClientProps {
  colecciones: ColeccionDTO[]
  totalColecciones: number
}

export function ColeccionesPageClient({ colecciones, totalColecciones }: ColeccionesPageClientProps) {
  const { isCollapsed } = useSidebarContext()

  // Efecto para redimensionar el mapa cuando cambie el estado del sidebar
  useEffect(() => {
    const timer = setTimeout(() => {
      // Disparar un evento de resize para que el mapa se redimensione
      window.dispatchEvent(new Event('resize'))
    }, 300) // Esperar a que termine la animación del sidebar

    return () => clearTimeout(timer)
  }, [isCollapsed])

  return (
    <div className="h-full w-full flex">
      {/* Sidebar - se oculta completamente cuando el sidebar principal está colapsado */}
      <AnimatePresence>
        {!isCollapsed && (
          <motion.div
            className="flex flex-col bg-content1 border-r border-divider"
            initial={{ width: 0, opacity: 0 }}
            animate={{ width: 420, opacity: 1 }} // 420px
            exit={{ width: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
          >
            {/* Header */}
            <div className="p-6 border-b border-divider">
              <h1 className="text-2xl font-bold text-foreground mb-2">Colecciones</h1>
              <p className="text-sm text-default-500">{totalColecciones} colecciones</p>
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
                            <Link href={`/colecciones/${coleccion.handle}`}>
                              <Button color="primary" size="sm" startContent={<i className="ri-eye-line w-4 h-4" />}>
                                Ver Hechos
                              </Button>
                            </Link>
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
  )
}
