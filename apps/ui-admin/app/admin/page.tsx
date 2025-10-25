import { Button, Card, CardBody } from "@heroui/react"
import Link from "next/link"

export default function AdminPage() {
  return (
    <div className="h-full w-full">
      <div className="flex h-full">
        {/* Sidebar con opciones de admin */}
        <div className="w-96 flex flex-col bg-content1 border-r border-divider">
          {/* Header */}
          <div className="p-6 border-b border-divider">
            <h2 className="text-2xl font-bold text-foreground mb-2">Panel de Administrador</h2>
            <p className="text-sm text-default-500">
              Herramientas de administración del sistema
            </p>
          </div>

          {/* Menu */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="space-y-3">
              <Link href="/administrador/colecciones">
                <Card isPressable isHoverable>
                  <CardBody className="p-4">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 flex items-center justify-center bg-primary/10 rounded-xl">
                        <i className="ri-folder-open-line w-6 h-6 text-primary" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-base text-foreground">Gestionar Colecciones</h3>
                        <p className="text-sm text-default-500 mt-1">Crear, editar y eliminar colecciones</p>
                      </div>
                    </div>
                  </CardBody>
                </Card>
              </Link>

              <Link href="/administrador/solicitudes">
                <Card isPressable isHoverable>
                  <CardBody className="p-4">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 flex items-center justify-center bg-warning/10 rounded-xl">
                        <i className="ri-alert-line w-6 h-6 text-warning" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-base text-foreground">Solicitudes de Eliminación</h3>
                        <p className="text-sm text-default-500 mt-1">Revisar y gestionar solicitudes</p>
                      </div>
                    </div>
                  </CardBody>
                </Card>
              </Link>

              <Card isPressable isHoverable>
                <CardBody className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 flex items-center justify-center bg-success/10 rounded-xl">
                      <i className="ri-database-line w-6 h-6 text-success" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-semibold text-base text-foreground">Gestión de Fuentes</h3>
                      <p className="text-sm text-default-500 mt-1">Administrar fuentes de datos</p>
                    </div>
                  </div>
                </CardBody>
              </Card>

              <Card isPressable isHoverable>
                <CardBody className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 flex items-center justify-center bg-secondary/10 rounded-xl">
                      <i className="ri-user-settings-line w-6 h-6 text-secondary" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-semibold text-base text-foreground">Usuarios</h3>
                      <p className="text-sm text-default-500 mt-1">Gestionar usuarios y permisos</p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            </div>
          </div>
        </div>

        {/* Mapa */}
        <div className="flex-1 relative min-w-0">
          <div className="w-full h-full flex items-center justify-center bg-muted">
            <div className="text-center space-y-4">
              <div className="text-6xl">⚙️</div>
              <h3 className="text-xl font-semibold text-foreground">Panel de Administración</h3>
              <p className="text-default-500">
                Selecciona una opción del menú para comenzar a administrar el sistema
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
