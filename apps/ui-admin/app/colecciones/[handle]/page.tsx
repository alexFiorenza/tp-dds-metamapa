import { ApiClient } from "@/lib/api-client"
import { SidebarLayout } from "@/components/sidebar-layout"
import { InfiniteScrollHechos } from "@/components/infinite-scroll-hechos"
import { MapPlaceholder } from "@/components/map-placeholder"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

interface ColeccionPageProps {
  params: {
    handle: string
  }
}

export default async function ColeccionPage({ params }: ColeccionPageProps) {
  const coleccion = await ApiClient.obtenerColeccion(params.handle)
  const hechos = await ApiClient.obtenerHechosDeColeccion(params.handle, {
    pagina: 0,
    tamanioPagina: 10,
  })

  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <Link href="/colecciones">
              <Button variant="ghost" size="sm" className="mb-3">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver a colecciones
              </Button>
            </Link>
            <h1 className="text-xl font-bold mb-2">{coleccion.titulo}</h1>
            <p className="text-sm text-muted-foreground mb-3">{coleccion.descripcion}</p>
            <div className="flex flex-wrap gap-2">
              {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                <Badge key={idx} variant="secondary" className="text-xs">
                  {criterio.tipo}: {criterio.categoria || criterio.estado}
                </Badge>
              ))}
            </div>
          </div>

          {/* Stats */}
          <div className="p-4 border-b border-border bg-muted/30">
            <div className="text-sm">
              <span className="font-semibold">{hechos.totalElementos}</span>
              <span className="text-muted-foreground"> hechos en esta colección</span>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto p-4">
            <InfiniteScrollHechos
              datosIniciales={hechos}
              fetchMas={async (pagina) => {
                "use server"
                return ApiClient.obtenerHechosDeColeccion(params.handle, {
                  pagina,
                  tamanioPagina: 10,
                })
              }}
            />
          </div>
        </div>
      }
    >
      <MapPlaceholder />
    </SidebarLayout>
  )
}
