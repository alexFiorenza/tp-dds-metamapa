"use client"

import { useState } from "react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Search, Filter } from "lucide-react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

interface FiltrosHechosProps {
  onFiltrar: (filtros: any) => void
}

const categorias = ["INCENDIO", "CONTAMINACION", "MANIFESTACION", "INUNDACION", "FAUNA"]

export function FiltrosHechos({ onFiltrar }: FiltrosHechosProps) {
  const [busqueda, setBusqueda] = useState("")
  const [categoria, setCategoria] = useState<string>("")

  const handleBuscar = () => {
    onFiltrar({
      titulo: busqueda || undefined,
      categoria: categoria || undefined,
    })
  }

  return (
    <div className="space-y-3 p-4 border-b border-border">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar hechos..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleBuscar()}
          className="pl-9"
        />
      </div>

      <div className="flex gap-2">
        <Select value={categoria} onValueChange={setCategoria}>
          <SelectTrigger className="flex-1">
            <SelectValue placeholder="Categoría" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Todas</SelectItem>
            {categorias.map((cat) => (
              <SelectItem key={cat} value={cat}>
                {cat}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Button onClick={handleBuscar} size="icon" variant="secondary">
          <Filter className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}
