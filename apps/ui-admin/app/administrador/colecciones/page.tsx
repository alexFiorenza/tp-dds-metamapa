import { ApiClient } from "@/lib/api-client"
import { SidebarLayout } from "@/components/sidebar-layout"
import { MapWrapper } from "@/components/map-wrapper"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Folder, Plus } from "lucide-react"
import Link from "next/link"

export default async function AdminColeccionesPage() {
  const respuesta = await ApiClient.obtenerColecciones()

  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <Link href="/administrador">
              <Button variant="ghost" size="sm" className="mb-3">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver
              </Button>
            </Link>
            <div className="flex items-center justify-between mb-2">
              <h1 className="text-2xl font-bold">Colecciones</h1>
              <Button size="sm">
                <Plus className="h-4 w-4 mr-2" />
                Nueva
              </Button>
            </div>
            <p className="text-sm text-muted-foreground">{respuesta.totalElementos} colecciones</p>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="space-y-3">
              {respuesta.datos.map((coleccion) => (
                <Card key={coleccion.handle} className="p-4">
                  <div className="flex items-start gap-3">
                    <div className="p-2 bg-primary/10 rounded-lg">
                      <Folder className="h-5 w-5 text-primary" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-sm mb-1">{coleccion.titulo}</h3>
                      <p className="text-xs text-muted-foreground line-clamp-2 mb-2">{coleccion.descripcion}</p>
                      <div className="flex flex-wrap gap-1 mb-3">
                        {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                          <Badge key={idx} variant="outline" className="text-xs">
                            {criterio.categoria}
                          </Badge>
                        ))}
                      </div>
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm">
                          Editar
                        </Button>
                        <Button variant="outline" size="sm">
                          Eliminar
                        </Button>
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </div>
        </div>
      }
    >
      <MapWrapper />
    </SidebarLayout>
  )
}
