"use client"

import { Button } from "@/components/ui/button"

interface PaginacionProps {
  paginaActual: number
  totalPaginas: number
  onCambioPagina: (pagina: number) => void
}

export function Paginacion({ paginaActual, totalPaginas, onCambioPagina }: PaginacionProps) {
  return (
    <div className="flex items-center justify-between p-4 border-t border-border">
      <Button
        variant="outline"
        size="sm"
        onClick={() => onCambioPagina(paginaActual - 1)}
        disabled={paginaActual === 0}
      >
        <i className="ri-arrow-left-s-line h-4 w-4 mr-1" />
        Anterior
      </Button>

      <span className="text-sm text-muted-foreground">
        Página {paginaActual + 1} de {totalPaginas}
      </span>

      <Button
        variant="outline"
        size="sm"
        onClick={() => onCambioPagina(paginaActual + 1)}
        disabled={paginaActual >= totalPaginas - 1}
      >
        Siguiente
        <i className="ri-arrow-right-s-line h-4 w-4 ml-1" />
      </Button>
    </div>
  )
}
