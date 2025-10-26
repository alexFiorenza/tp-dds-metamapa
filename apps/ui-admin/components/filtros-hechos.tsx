"use client"

import { useState, useEffect } from "react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { motion, AnimatePresence } from "motion/react"
import type { FiltrosHechos, EstadoHecho } from "@/types/api"

interface FiltrosHechosProps {
  onFiltrar: (filtros: FiltrosHechos) => void
  onLimpiar: () => void
  filtrosIniciales?: FiltrosHechos
}

const categorias = ["INCENDIO", "CONTAMINACION", "MANIFESTACION", "INUNDACION", "FAUNA"]
const estados: EstadoHecho[] = ["ACTIVO", "OCULTO"]

export function FiltrosHechos({ onFiltrar, onLimpiar, filtrosIniciales = {} }: FiltrosHechosProps) {
  const [filtros, setFiltros] = useState<FiltrosHechos>(filtrosIniciales)
  const [mostrarAvanzados, setMostrarAvanzados] = useState(false)

  // Sincronizar filtros con los valores de la URL
  useEffect(() => {
    setFiltros(filtrosIniciales)

    // Auto-expandir filtros avanzados si hay filtros avanzados aplicados
    const tieneAvanzados = filtrosIniciales.descripcion ||
                          filtrosIniciales.origen ||
                          filtrosIniciales.estado ||
                          filtrosIniciales.fechaAcontecimiento ||
                          filtrosIniciales.etiquetas ||
                          filtrosIniciales.latitud ||
                          filtrosIniciales.longitud

    if (tieneAvanzados) {
      setMostrarAvanzados(true)
    }
  }, [filtrosIniciales])

  const handleFiltrar = () => {
    // Limpiar valores vacíos
    const filtrosLimpios = Object.fromEntries(
      Object.entries(filtros).filter(([_, value]) => 
        value !== undefined && value !== null && value !== ""
      )
    ) as FiltrosHechos
    
    onFiltrar(filtrosLimpios)
  }

  const handleLimpiar = () => {
    setFiltros({})
    onLimpiar()
  }

  const actualizarFiltro = (campo: keyof FiltrosHechos, valor: any) => {
    setFiltros(prev => ({
      ...prev,
      [campo]: valor
    }))
  }

  return (
    <div className="mb-3 p-3 bg-content1 rounded-lg border border-divider">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-foreground">Filtros</h3>
        <Button
          variant="light"
          size="sm"
          onPress={() => setMostrarAvanzados(!mostrarAvanzados)}
          startContent={<i className={`ri-arrow-${mostrarAvanzados ? 'up' : 'down'}-s-line`} />}
        >
          {mostrarAvanzados ? 'Ocultar' : 'Avanzados'}
        </Button>
      </div>
      
      <div className="space-y-3">
        {/* Filtros básicos */}
        <div className="flex gap-2">
          {/* Búsqueda por título */}
          <div className="flex-1">
            <div className="relative">
              <i className="ri-search-line absolute left-2 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por título..."
                value={filtros.titulo || ""}
                onChange={(e) => actualizarFiltro("titulo", e.target.value)}
                className="pl-8 h-9"
                size="sm"
              />
            </div>
          </div>

          {/* Categoría */}
          <div className="w-40">
            <Select 
              value={filtros.categoria || "all"} 
              onValueChange={(value) => actualizarFiltro("categoria", value === "all" ? undefined : value)}
            >
              <SelectTrigger className="h-9">
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
          </div>

          {/* Botones de acción */}
          <div className="flex gap-1">
            <Button 
              onClick={handleFiltrar} 
              color="primary"
              size="sm"
              className="h-9 px-3"
              startContent={<i className="ri-filter-line" />}
            >
              Filtrar
            </Button>
            <Button 
              onClick={handleLimpiar} 
              variant="flat"
              size="sm"
              className="h-9 px-3"
              startContent={<i className="ri-refresh-line" />}
            >
              Limpiar
            </Button>
          </div>
        </div>

        {/* Filtros avanzados */}
        <AnimatePresence>
          {mostrarAvanzados && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.3 }}
              className="space-y-3 border-t pt-3"
            >
              <div className="grid grid-cols-2 gap-2">
                {/* Descripción */}
                <Input
                  placeholder="Descripción..."
                  value={filtros.descripcion || ""}
                  onChange={(e) => actualizarFiltro("descripcion", e.target.value)}
                  size="sm"
                  className="h-8"
                />

                {/* Origen */}
                <Input
                  placeholder="Origen..."
                  value={filtros.origen || ""}
                  onChange={(e) => actualizarFiltro("origen", e.target.value)}
                  size="sm"
                  className="h-8"
                />

                {/* Estado */}
                <Select 
                  value={filtros.estado || "all"} 
                  onValueChange={(value) => actualizarFiltro("estado", value === "all" ? undefined : value)}
                >
                  <SelectTrigger className="h-8">
                    <SelectValue placeholder="Estado" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Todos</SelectItem>
                    {estados.map((estado) => (
                      <SelectItem key={estado} value={estado}>
                        {estado}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                {/* Fecha de acontecimiento */}
                <Input
                  type="date"
                  value={filtros.fechaAcontecimiento || ""}
                  onChange={(e) => actualizarFiltro("fechaAcontecimiento", e.target.value || undefined)}
                  size="sm"
                  className="h-8"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                {/* Etiquetas */}
                <Input
                  placeholder="Etiquetas (separadas por comas)..."
                  value={filtros.etiquetas || ""}
                  onChange={(e) => actualizarFiltro("etiquetas", e.target.value)}
                  size="sm"
                  className="h-8"
                />

                {/* Coordenadas */}
                <div className="flex gap-1">
                  <Input
                    type="number"
                    step="any"
                    placeholder="Lat"
                    value={filtros.latitud?.toString() || ""}
                    onChange={(e) => actualizarFiltro("latitud", e.target.value ? parseFloat(e.target.value) : undefined)}
                    size="sm"
                    className="h-8"
                  />
                  <Input
                    type="number"
                    step="any"
                    placeholder="Lng"
                    value={filtros.longitud?.toString() || ""}
                    onChange={(e) => actualizarFiltro("longitud", e.target.value ? parseFloat(e.target.value) : undefined)}
                    size="sm"
                    className="h-8"
                  />
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}
