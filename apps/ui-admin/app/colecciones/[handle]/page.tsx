"use client"

import { useState, useEffect } from "react"
import { useParams } from "next/navigation"
import { ApiClient } from "@/lib/api-client"
import { ColeccionPageClient } from "./page-client"
import type { ColeccionDTO } from "@/types/api"

export default function ColeccionPage() {
  const params = useParams()
  const handle = params.handle as string

  const [coleccion, setColeccion] = useState<ColeccionDTO | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    const loadColeccion = async () => {
      try {
        const data = await ApiClient.obtenerColeccionPublica(handle)
        setColeccion(data)
      } catch (error) {
        console.error("Error loading colección:", error)
        setError(true)
      } finally {
        setIsLoading(false)
      }
    }

    loadColeccion()
  }, [handle])

  if (isLoading) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-default-500">Cargando colección...</p>
        </div>
      </div>
    )
  }

  if (error || !coleccion) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <div className="text-center">
          <i className="ri-error-warning-line text-4xl text-danger mb-4" />
          <p className="text-default-500">No se encontró la colección</p>
        </div>
      </div>
    )
  }

  return <ColeccionPageClient handle={handle} coleccion={coleccion} />
}
