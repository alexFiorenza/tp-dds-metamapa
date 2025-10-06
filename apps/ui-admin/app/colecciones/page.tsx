import { ApiClient } from "@/lib/api-client"
import { SidebarLayout } from "@/components/sidebar-layout"
import { InfiniteScrollColecciones } from "@/components/infinite-scroll-colecciones"
import { MapPlaceholder } from "@/components/map-placeholder"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

export default async function ColeccionesPage() {
  const respuesta = await ApiClient.obtenerColecciones(0, 10)

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
            <h1 className="text-2xl font-bold">Colecciones</h1>
            <p className="text-sm text-muted-foreground mt-1">{respuesta.totalElementos} colecciones disponibles</p>
          </div>

          <div className="flex-1 overflow-y-auto p-4">
            <InfiniteScrollColecciones
              datosIniciales={respuesta}
              fetchMas={async (pagina) => {
                "use server"
                return ApiClient.obtenerColecciones(pagina, 10)
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
