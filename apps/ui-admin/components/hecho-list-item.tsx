"use client"

import type { HechoDTO } from "@/types/api"
import { Card, CardBody } from "@heroui/react"
import { motion } from "motion/react"
import { cn } from "@/lib/utils"

interface HechoListItemProps {
  hecho: HechoDTO
  onClick?: () => void
  onDetailsClick?: () => void
  isSelected?: boolean
  onHover?: (hovered: boolean) => void
}

// Mapa de categorías a iconos y colores
const getCategoriaIcon = (categoria: string) => {
  switch (categoria) {
    case "INCENDIO":
      return { iconClass: "ri-fire-line", color: "text-danger", bgColor: "bg-danger/10" }
    case "CONTAMINACION":
      return { iconClass: "ri-drop-line", color: "text-warning", bgColor: "bg-warning/10" }
    case "MANIFESTACION":
      return { iconClass: "ri-group-line", color: "text-primary", bgColor: "bg-primary/10" }
    case "INUNDACION":
      return { iconClass: "ri-water-flash-line", color: "text-blue-500", bgColor: "bg-blue-500/10" }
    case "FAUNA":
      return { iconClass: "ri-plant-line", color: "text-success", bgColor: "bg-success/10" }
    default:
      return { iconClass: "ri-map-pin-line", color: "text-default-500", bgColor: "bg-default-100" }
  }
}

export function HechoListItem({ hecho, onClick, onDetailsClick, isSelected, onHover }: HechoListItemProps) {
  const { iconClass, color, bgColor } = getCategoriaIcon(hecho.categoria)
  const isOculto = hecho.estado === "OCULTO"

  return (
    <motion.div
      whileHover={{ x: 2 }}
      whileTap={{ scale: 0.98 }}
      onMouseEnter={() => onHover?.(true)}
      onMouseLeave={() => onHover?.(false)}
      className="w-full"
    >
      <Card
        isPressable
        isHoverable
        onPress={onClick}
        className={cn(
          "transition-all duration-200 w-full",
          isSelected && "ring-2 ring-primary shadow-lg",
          isOculto && "opacity-50 border-l-4 border-default-400"
        )}
      >
        <CardBody className="p-3">
          {isOculto && (
            <div className="flex items-center gap-1 text-xs text-default-500 mb-2 px-1">
              <i className="ri-eye-off-line w-3.5 h-3.5" />
              <span className="font-medium">Oculto</span>
            </div>
          )}
          <div className="flex gap-3">
            {/* Icono de categoría */}
            <div className={cn("w-10 h-10 rounded-full flex items-center justify-center shrink-0", bgColor)}>
              <i className={cn(iconClass, "w-5 h-5", color)} />
            </div>

            {/* Contenido simplificado */}
            <div className="flex-1 min-w-0 space-y-1.5">
              <h3 className="font-semibold text-sm leading-tight line-clamp-1 text-foreground">
                {hecho.titulo}
              </h3>

              <div className="flex flex-col gap-1 text-xs text-default-500">
                <div className="flex items-center gap-1.5">
                  <i className="ri-calendar-line w-3.5 h-3.5" />
                  <span>
                    {new Date(hecho.fechaAcontecimiento).toLocaleDateString("es-AR", {
                      day: '2-digit',
                      month: 'short',
                      year: 'numeric'
                    })}
                  </span>
                </div>
                <div className="flex items-center gap-1.5">
                  <i className="ri-map-pin-line w-3.5 h-3.5" />
                  <span className="font-mono">
                    {hecho.latitud.toFixed(4)}, {hecho.longitud.toFixed(4)}
                  </span>
                </div>
              </div>

              {/* Botón Ver detalles */}
              {isSelected && onDetailsClick && (
                <div
                  role="button"
                  tabIndex={0}
                  onClick={(e) => {
                    e.stopPropagation()
                    onDetailsClick()
                  }}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault()
                      e.stopPropagation()
                      onDetailsClick()
                    }
                  }}
                  className="mt-2 text-xs text-primary hover:text-primary-600 font-medium flex items-center gap-1 transition-colors cursor-pointer"
                >
                  <i className="ri-fullscreen-line w-3.5 h-3.5" />
                  <span>Ver detalles</span>
                </div>
              )}
            </div>
          </div>
        </CardBody>
      </Card>
    </motion.div>
  )
}
