"use client"

import { useState, useEffect } from "react"
import { ApiClient } from "@/lib/api-client"
import { ColeccionesPageClient } from "./page-client"
import type { ColeccionDTO } from "@/types/api"

export default function ColeccionesPage() {
  const [colecciones, setColecciones] = useState<ColeccionDTO[]>([])
  const [total, setTotal] = useState(0)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loadColecciones = async () => {
      try {
        const respuesta = await ApiClient.obtenerColeccionesPublicas(0, 20)
        setColecciones(respuesta.datos)
        setTotal(respuesta.totalElementos)
      } catch (error) {
        console.error("Error loading colecciones:", error)
      } finally {
        setIsLoading(false)
      }
    }

    loadColecciones()
  }, [])

  if (isLoading) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-default-500">Cargando colecciones...</p>
        </div>
      </div>
    )
  }

  return <ColeccionesPageClient colecciones={colecciones} totalColecciones={total} />
}
