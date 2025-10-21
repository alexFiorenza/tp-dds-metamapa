"use client"

import { useEffect, useRef, useState } from "react"
import type { HechoDTO, RespuestaPaginadaDTO } from "@/types/api"
import { HechoCard } from "./hecho-card"
import { HechoCardSkeleton } from "./hecho-card-skeleton"
import { Loader2 } from "lucide-react"

interface InfiniteScrollHechosProps {
  datosIniciales: RespuestaPaginadaDTO<HechoDTO>
  fetchMas: (pagina: number) => Promise<RespuestaPaginadaDTO<HechoDTO>>
}

export function InfiniteScrollHechos({ datosIniciales, fetchMas }: InfiniteScrollHechosProps) {
  const [hechos, setHechos] = useState<HechoDTO[]>(datosIniciales.datos)
  const [pagina, setPagina] = useState(datosIniciales.pagina)
  const [tieneSiguiente, setTieneSiguiente] = useState(datosIniciales.tieneSiguiente)
  const [cargando, setCargando] = useState(false)
  const observerTarget = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && tieneSiguiente && !cargando) {
          cargarMas()
        }
      },
      { threshold: 0.1 },
    )

    if (observerTarget.current) {
      observer.observe(observerTarget.current)
    }

    return () => observer.disconnect()
  }, [tieneSiguiente, cargando, pagina])

  const cargarMas = async () => {
    setCargando(true)
    try {
      const siguientePagina = pagina + 1
      const respuesta = await fetchMas(siguientePagina)
      setHechos((prev) => [...prev, ...respuesta.datos])
      setPagina(respuesta.pagina)
      setTieneSiguiente(respuesta.tieneSiguiente)
    } catch (error) {
      console.error("[v0] Error cargando más hechos:", error)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="space-y-3">
      {hechos.map((hecho) => (
        <HechoCard key={hecho.uuid} hecho={hecho} />
      ))}

      {/* Loading indicator */}
      {cargando && (
        <>
          <HechoCardSkeleton />
          <HechoCardSkeleton />
        </>
      )}

      {/* Intersection observer target */}
      <div ref={observerTarget} className="h-4" />

      {/* End message */}
      {!tieneSiguiente && hechos.length > 0 && (
        <div className="text-center py-4 text-sm text-muted-foreground">No hay más hechos para mostrar</div>
      )}

      {/* Loading spinner */}
      {cargando && (
        <div className="flex justify-center py-4">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      )}
    </div>
  )
}
