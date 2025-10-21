"use client"

import { useEffect, useRef, useState } from "react"
import type { ColeccionDTO, RespuestaPaginadaDTO } from "@/types/api"
import { Card } from "@/components/ui/card"
import { Folder, Loader2 } from "lucide-react"
import Link from "next/link"
import { ColeccionCardSkeleton } from "./coleccion-card-skeleton"

interface InfiniteScrollColeccionesProps {
  datosIniciales: RespuestaPaginadaDTO<ColeccionDTO>
  fetchMas: (pagina: number) => Promise<RespuestaPaginadaDTO<ColeccionDTO>>
}

export function InfiniteScrollColecciones({ datosIniciales, fetchMas }: InfiniteScrollColeccionesProps) {
  const [colecciones, setColecciones] = useState<ColeccionDTO[]>(datosIniciales.datos)
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
      setColecciones((prev) => [...prev, ...respuesta.datos])
      setPagina(respuesta.pagina)
      setTieneSiguiente(respuesta.tieneSiguiente)
    } catch (error) {
      console.error("[v0] Error cargando más colecciones:", error)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="space-y-3">
      {colecciones.map((coleccion) => (
        <Link key={coleccion.handle} href={`/colecciones/${coleccion.handle}`}>
          <Card className="p-4 hover:bg-accent/50 transition-colors cursor-pointer">
            <div className="flex items-start gap-3">
              <div className="p-2 bg-primary/10 rounded-lg">
                <Folder className="h-5 w-5 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-sm mb-1">{coleccion.titulo}</h3>
                <p className="text-xs text-muted-foreground line-clamp-2">{coleccion.descripcion}</p>
                <div className="mt-2 text-xs text-muted-foreground">{coleccion.fuentes.length} fuente(s)</div>
              </div>
            </div>
          </Card>
        </Link>
      ))}

      {/* Loading indicator */}
      {cargando && (
        <>
          <ColeccionCardSkeleton />
          <ColeccionCardSkeleton />
        </>
      )}

      {/* Intersection observer target */}
      <div ref={observerTarget} className="h-4" />

      {/* End message */}
      {!tieneSiguiente && colecciones.length > 0 && (
        <div className="text-center py-4 text-sm text-muted-foreground">No hay más colecciones para mostrar</div>
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
