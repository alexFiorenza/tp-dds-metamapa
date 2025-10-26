"use client"

import { Button } from "@heroui/react"

interface PaginacionProps {
  paginaActual: number
  totalPaginas: number
  onCambioPagina: (pagina: number) => void
}

export function Paginacion({ paginaActual, totalPaginas, onCambioPagina }: PaginacionProps) {
  return (
    <div className="flex items-center justify-between p-4 border-t border-border">
      <Button
        variant="bordered"
        size="sm"
        onPress={() => onCambioPagina(paginaActual - 1)}
        isDisabled={paginaActual === 0}
      >
        <i className="ri-arrow-left-s-line h-4 w-4 mr-1" />
        Anterior
      </Button>

      <span className="text-sm text-muted-foreground">
        Página {paginaActual + 1} de {totalPaginas}
      </span>

      <Button
        variant="bordered"
        size="sm"
        onPress={() => onCambioPagina(paginaActual + 1)}
        isDisabled={paginaActual >= totalPaginas - 1}
      >
        Siguiente
        <i className="ri-arrow-right-s-line h-4 w-4 ml-1" />
      </Button>
    </div>
  )
}
