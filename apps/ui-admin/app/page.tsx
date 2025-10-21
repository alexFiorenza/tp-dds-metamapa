import { ApiClient } from "@/lib/api-client"
import { SidebarLayout } from "@/components/sidebar-layout"
import { HechoCard } from "@/components/hecho-card"
import { MapWrapper } from "@/components/map-wrapper"
import { Button } from "@/components/ui/button"
import { Map, List } from "lucide-react"
import Link from "next/link"

export default async function HomePage() {
  const respuesta = await ApiClient.obtenerHechos({ tamanioPagina: 5 })

  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-2xl font-bold">MetaMapa</h1>
              <Link href="/administrador">
                <Button variant="outline" size="sm">
                  Admin
                </Button>
              </Link>
            </div>
            <p className="text-sm text-muted-foreground">Sistema colaborativo de mapeo de información</p>
          </div>

          {/* Navigation */}
          <div className="flex gap-2 p-4 border-b border-border">
            <Link href="/hechos" className="flex-1">
              <Button variant="default" className="w-full" size="sm">
                <List className="h-4 w-4 mr-2" />
                Ver todos los hechos
              </Button>
            </Link>
            <Link href="/colecciones" className="flex-1">
              <Button variant="outline" className="w-full bg-transparent" size="sm">
                <Map className="h-4 w-4 mr-2" />
                Colecciones
              </Button>
            </Link>
          </div>

          {/* Recent facts */}
          <div className="flex-1 overflow-y-auto p-4">
            <h2 className="text-sm font-semibold mb-3 text-muted-foreground uppercase tracking-wide">
              Hechos recientes
            </h2>
            <div className="space-y-3">
              {respuesta.datos.map((hecho) => (
                <HechoCard key={hecho.uuid} hecho={hecho} />
              ))}
            </div>
          </div>
        </div>
      }
    >
      <MapWrapper hechos={respuesta.datos} />
    </SidebarLayout>
  )
}
