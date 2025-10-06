"use client"

import type { HechoDTO } from "@/types/api"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, MapPin, Tag, ImageIcon } from "lucide-react"
import Image from "next/image"

interface HechoCardProps {
  hecho: HechoDTO
  onClick?: () => void
}

export function HechoCard({ hecho, onClick }: HechoCardProps) {
  const imagenes = hecho.multimedia?.filter((m) => m.tipo === "IMAGEN") || []
  const primeraImagen = imagenes[0]

  return (
    <Card className="overflow-hidden hover:bg-accent/50 transition-colors cursor-pointer" onClick={onClick}>
      {primeraImagen && (
        <div className="relative w-full h-40 bg-muted">
          <Image src={primeraImagen.url || "/placeholder.svg"} alt={hecho.titulo} fill className="object-cover" />
          {imagenes.length > 1 && (
            <div className="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded-md flex items-center gap-1">
              <ImageIcon className="h-3 w-3" />
              {imagenes.length}
            </div>
          )}
        </div>
      )}

      <div className="p-4 space-y-2">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-sm leading-tight">{hecho.titulo}</h3>
          <Badge variant="secondary" className="shrink-0 text-xs">
            {hecho.categoria}
          </Badge>
        </div>

        <p className="text-sm text-muted-foreground line-clamp-2">{hecho.descripcion}</p>

        <div className="flex flex-col gap-1 text-xs text-muted-foreground">
          <div className="flex items-center gap-1">
            <Calendar className="h-3 w-3" />
            <span>{new Date(hecho.fechaAcontecimiento).toLocaleDateString("es-AR")}</span>
          </div>

          <div className="flex items-center gap-1">
            <MapPin className="h-3 w-3" />
            <span>
              {hecho.latitud.toFixed(4)}, {hecho.longitud.toFixed(4)}
            </span>
          </div>
        </div>

        {hecho.etiquetas.length > 0 && (
          <div className="flex items-center gap-1 flex-wrap">
            <Tag className="h-3 w-3 text-muted-foreground" />
            {hecho.etiquetas.slice(0, 3).map((etiqueta) => (
              <Badge key={etiqueta} variant="outline" className="text-xs">
                {etiqueta}
              </Badge>
            ))}
            {hecho.etiquetas.length > 3 && (
              <span className="text-xs text-muted-foreground">+{hecho.etiquetas.length - 3}</span>
            )}
          </div>
        )}
      </div>
    </Card>
  )
}
