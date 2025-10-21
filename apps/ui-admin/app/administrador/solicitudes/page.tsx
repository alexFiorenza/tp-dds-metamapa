import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { SidebarLayout } from "@/components/sidebar-layout"
import { MapWrapper } from "@/components/map-wrapper"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Check, X, Clock } from "lucide-react"
import Link from "next/link"

export default async function AdminSolicitudesPage() {
  const token = await getAuthToken()
  const respuesta = await ApiClient.obtenerSolicitudes(0, 10, token)

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
            <h1 className="text-2xl font-bold">Solicitudes de Eliminación</h1>
            <p className="text-sm text-muted-foreground mt-1">{respuesta.totalElementos} solicitudes</p>
          </div>

          {/* Filters */}
          <div className="p-4 border-b border-border">
            <div className="flex gap-2">
              <Button variant="outline" size="sm" className="flex-1 bg-transparent">
                Todas
              </Button>
              <Button variant="outline" size="sm" className="flex-1 bg-transparent">
                Pendientes
              </Button>
              <Button variant="outline" size="sm" className="flex-1 bg-transparent">
                Resueltas
              </Button>
            </div>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="space-y-3">
              {respuesta.datos.map((solicitud) => (
                <Card key={solicitud.uuid} className="p-4">
                  <div className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <Badge
                        variant={
                          solicitud.estado === "PENDIENTE"
                            ? "default"
                            : solicitud.estado === "ACEPTADA"
                              ? "secondary"
                              : "outline"
                        }
                        className="shrink-0"
                      >
                        {solicitud.estado === "PENDIENTE" && <Clock className="h-3 w-3 mr-1" />}
                        {solicitud.estado}
                      </Badge>
                      <span className="text-xs text-muted-foreground">
                        {new Date(solicitud.fechaSolicitud).toLocaleDateString("es-AR")}
                      </span>
                    </div>

                    <div>
                      <p className="text-sm font-medium mb-1">Hecho: {solicitud.hecho}</p>
                      <p className="text-sm text-muted-foreground">{solicitud.texto}</p>
                    </div>

                    {solicitud.estado === "PENDIENTE" && (
                      <div className="flex gap-2">
                        <Button variant="default" size="sm" className="flex-1">
                          <Check className="h-4 w-4 mr-1" />
                          Aceptar
                        </Button>
                        <Button variant="outline" size="sm" className="flex-1 bg-transparent">
                          <X className="h-4 w-4 mr-1" />
                          Rechazar
                        </Button>
                      </div>
                    )}
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
