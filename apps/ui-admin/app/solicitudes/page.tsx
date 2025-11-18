import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { Button, Card, CardBody, Chip, ButtonGroup, Spinner } from "@heroui/react"
import Link from "next/link"
import { Suspense } from "react"

async function SolicitudesList() {
  const token = await getAuthToken()
  const respuesta = await ApiClient.obtenerSolicitudes(0, 20, token)

  return (
    <div className="space-y-3">
      {respuesta.datos.map((solicitud) => (
        <Card key={solicitud.uuid}>
          <CardBody className="p-4">
            <div className="space-y-3">
              <div className="flex items-start justify-between gap-2">
                <Chip
                  size="sm"
                  variant="flat"
                  color={solicitud.estado === "ACTIVO" ? "warning" : "default"}
                  startContent={solicitud.estado === "ACTIVO" ? <i className="ri-time-line w-3 h-3" /> : null}
                >
                  {solicitud.estado === "ACTIVO" ? "PENDIENTE" : "PROCESADA"}
                </Chip>
                <span className="text-xs text-default-500">
                  {new Date(solicitud.fechaSolicitud).toLocaleDateString("es-AR", {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric'
                  })}
                </span>
              </div>

              <div>
                <p className="text-sm font-semibold mb-1 text-foreground">Hecho: {solicitud.hecho}</p>
                <p className="text-sm text-default-600">{solicitud.texto}</p>
              </div>

              <div className="flex gap-2">
                <Button
                  variant="flat"
                  size="sm"
                  className="flex-1"
                  startContent={<i className="ri-eye-line w-4 h-4" />}
                >
                  Ver Hecho
                </Button>
                <Button
                  variant="flat"
                  size="sm"
                  className="flex-1"
                  startContent={<i className="ri-map-pin-line w-4 h-4" />}
                >
                  Ver en Mapa
                </Button>
              </div>
            </div>
          </CardBody>
        </Card>
      ))}
    </div>
  )
}

export default function SolicitudesPage() {
  return (
    <div className="h-full w-full">
      <div className="flex h-full">
        {/* Sidebar con lista de solicitudes */}
        <div className="w-96 flex flex-col bg-content1 border-r border-divider">
          {/* Header */}
          <div className="p-6 border-b border-divider">
            <h2 className="text-2xl font-bold text-foreground mb-2">Mis Solicitudes</h2>
            <p className="text-sm text-default-500">
              Solicitudes de eliminación que has enviado
            </p>
          </div>

          {/* Filters */}
          <div className="p-4 border-b border-divider">
            <ButtonGroup className="w-full" size="sm" variant="bordered">
              <Button className="flex-1">Todas</Button>
              <Button className="flex-1">Pendientes</Button>
              <Button className="flex-1">Resueltas</Button>
            </ButtonGroup>
          </div>

          {/* Lista de solicitudes */}
          <div className="flex-1 overflow-y-auto p-4">
            <Suspense fallback={
              <div className="flex items-center justify-center h-32">
                <Spinner size="lg" color="primary" />
              </div>
            }>
              <SolicitudesList />
            </Suspense>
          </div>
        </div>

        {/* Mapa */}
        <div className="flex-1 relative min-w-0">
          <div className="w-full h-full flex items-center justify-center bg-muted">
            <div className="text-center space-y-4">
              <div className="text-6xl">📋</div>
              <h3 className="text-xl font-semibold text-foreground">Tus Solicitudes</h3>
              <p className="text-default-500">
                Aquí puedes ver el estado de tus solicitudes de eliminación
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
