'use client'

import { useEffect } from 'react'
import { MapWrapper } from "@/components/map-wrapper"
import { Card, CardBody } from "@heroui/react"
import { useRouter } from "next/navigation"
import { motion, AnimatePresence } from 'motion/react'
import { useSidebarContext } from '@/components/layout-wrapper'

export default function AdminPage() {
  const { isCollapsed } = useSidebarContext()
  const router = useRouter()

  // Efecto para redimensionar el mapa cuando cambie el estado del sidebar
  useEffect(() => {
    const timer = setTimeout(() => {
      window.dispatchEvent(new Event('resize'))
    }, 300)
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
            animate={{ width: 420, opacity: 1 }}
            exit={{ width: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
          >
            {/* Header */}
            <div className="p-6 border-b border-divider">
              <h1 className="text-2xl font-bold text-foreground mb-2">Panel de Administrador</h1>
              <p className="text-sm text-default-500">Gestión de colecciones y solicitudes</p>
            </div>

            {/* Menu */}
            <div className="flex-1 overflow-y-auto p-4">
              <div className="space-y-3">
                <Card
                  isPressable
                  isHoverable
                  onPress={() => router.push('/administrador/colecciones')}
                  className="cursor-pointer w-full"
                >
                  <CardBody className="p-4">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 flex items-center justify-center bg-primary/10 rounded-xl shrink-0">
                        <i className="ri-folder-open-line w-6 h-6 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-base text-foreground">Gestionar Colecciones</h3>
                        <p className="text-sm text-default-500 mt-1">Crear, editar y eliminar colecciones</p>
                      </div>
                    </div>
                  </CardBody>
                </Card>

                <Card
                  isPressable
                  isHoverable
                  onPress={() => router.push('/administrador/solicitudes')}
                  className="cursor-pointer w-full"
                >
                  <CardBody className="p-4">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 flex items-center justify-center bg-warning/10 rounded-xl shrink-0">
                        <i className="ri-alert-line w-6 h-6 text-warning" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-base text-foreground">Solicitudes de Eliminación</h3>
                        <p className="text-sm text-default-500 mt-1">Revisar y gestionar solicitudes</p>
                      </div>
                    </div>
                  </CardBody>
                </Card>
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
