import { ApiClient } from "@/lib/api-client"
import { SidebarLayout } from "@/components/sidebar-layout"
import { InfiniteScrollHechos } from "@/components/infinite-scroll-hechos"
import { MapPlaceholder } from "@/components/map-placeholder"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

interface HechosPageProps {
  searchParams: {
    categoria?: string
    titulo?: string
  }
}

export default async function HechosPage({ searchParams }: HechosPageProps) {
  const respuesta = await ApiClient.obtenerHechos({
    pagina: 0,
    tamanioPagina: 10,
    categoria: searchParams.categoria,
    titulo: searchParams.titulo,
  })

  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <Link href="/">
              <Button variant="ghost" size="sm" className="mb-3">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver
              </Button>
            </Link>
            <h1 className="text-2xl font-bold">Todos los Hechos</h1>
            <p className="text-sm text-muted-foreground mt-1">{respuesta.totalElementos} hechos encontrados</p>
          </div>

          {/* Filters would go here */}
          <div className="p-4 border-b border-border">
            <p className="text-xs text-muted-foreground">Filtros disponibles próximamente</p>
          </div>

          <div className="flex-1 overflow-y-auto p-4">
            <InfiniteScrollHechos
              datosIniciales={respuesta}
              fetchMas={async (pagina) => {
                "use server"
                return ApiClient.obtenerHechos({
                  pagina,
                  tamanioPagina: 10,
                  categoria: searchParams.categoria,
                  titulo: searchParams.titulo,
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
