"use client"

import type { HechoDTO } from "@/types/api"
import { Card, CardBody } from "@heroui/react"
import { Calendar, MapPin, Flame, Droplets, Users, Waves, TreePine } from "lucide-react"
import { cn } from "@/lib/utils"

interface HechoListItemProps {
  hecho: HechoDTO
  onClick?: () => void
  isSelected?: boolean
  onHover?: (hovered: boolean) => void
}

// Mapa de categorías a iconos y colores
const getCategoriaIcon = (categoria: string) => {
  switch (categoria) {
    case "INCENDIO":
      return { Icon: Flame, color: "text-danger", bgColor: "bg-danger/10" }
    case "CONTAMINACION":
      return { Icon: Droplets, color: "text-warning", bgColor: "bg-warning/10" }
    case "MANIFESTACION":
      return { Icon: Users, color: "text-primary", bgColor: "bg-primary/10" }
    case "INUNDACION":
      return { Icon: Waves, color: "text-blue-500", bgColor: "bg-blue-500/10" }
    case "FAUNA":
      return { Icon: TreePine, color: "text-success", bgColor: "bg-success/10" }
    default:
      return { Icon: MapPin, color: "text-default-500", bgColor: "bg-default-100" }
  }
}

export function HechoListItem({ hecho, onClick, isSelected, onHover }: HechoListItemProps) {
  const { Icon, color, bgColor } = getCategoriaIcon(hecho.categoria)
  const hasImages = (hecho.multimedia?.filter((m) => m.tipo === "IMAGEN") || []).length > 0

  return (
    <Card
      isPressable
      isHoverable
      onPress={onClick}
      className={cn(
        "transition-all duration-200",
        isSelected && "ring-2 ring-primary shadow-lg scale-[1.02]"
      )}
      onMouseEnter={() => onHover?.(true)}
      onMouseLeave={() => onHover?.(false)}
    >
      <CardBody className="p-3">
        <div className="flex gap-3">
          {/* Icono de categoría */}
          <div className={cn("w-10 h-10 rounded-full flex items-center justify-center shrink-0", bgColor)}>
            <Icon className={cn("w-5 h-5", color)} />
          </div>

          {/* Contenido */}
          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-2 mb-1">
              <h3 className="font-semibold text-sm leading-tight line-clamp-1">{hecho.titulo}</h3>
              {hasImages && (
                <div className="w-2 h-2 rounded-full bg-primary shrink-0 mt-1" />
              )}
            </div>

            <p className="text-xs text-default-500 line-clamp-2 mb-2">{hecho.descripcion}</p>

            <div className="flex items-center gap-3 text-xs text-default-400">
              <div className="flex items-center gap-1">
                <Calendar className="w-3 h-3" />
                <span>{new Date(hecho.fechaAcontecimiento).toLocaleDateString("es-AR", {
                  day: '2-digit',
                  month: 'short'
                })}</span>
              </div>
              <div className="flex items-center gap-1">
                <MapPin className="w-3 h-3" />
                <span className="line-clamp-1">
                  {hecho.latitud.toFixed(3)}, {hecho.longitud.toFixed(3)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </CardBody>
    </Card>
  )
}
