'use client';

import { Card, CardBody } from '@heroui/react';

export default function EstadisticasPage() {
  const dashboardUrl = process.env.NEXT_PUBLIC_GRAFANA_DASHBOARD_URL || 'http://localhost:3000/goto/ef3pd7hzdxnuod?orgId=1';

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Estadísticas</h1>
        <p className="text-default-600 mt-2">
          Visualiza métricas y análisis de hechos por provincia, categoría y hora
        </p>
      </div>

      {/* Link al Dashboard de Grafana */}
      <Card>
        <CardBody className="p-8 text-center">
          <div className="flex flex-col items-center gap-6">
            <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center">
              <i className="ri-bar-chart-box-line text-5xl text-primary" />
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold text-foreground">
                Dashboard de Estadísticas
              </h2>
              <p className="text-default-600 max-w-md">
                Accede al dashboard interactivo de Grafana con todas las funcionalidades: exportar CSV, descargar PNG, y más.
              </p>
            </div>

            <a
              href={dashboardUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-lg font-medium hover:bg-primary/90 transition-colors"
            >
              <i className="ri-external-link-line text-xl" />
              Abrir Dashboard en Grafana
            </a>

            <div className="flex items-center gap-2 text-sm text-default-500">
              <i className="ri-information-line" />
              <span>Se abrirá en una nueva pestaña</span>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Info adicional */}
      <Card>
        <CardBody>
          <div className="flex items-start gap-3">
            <i className="ri-information-line text-2xl text-primary mt-0.5" />
            <div className="space-y-1">
              <p className="text-sm font-medium text-foreground">
                Actualización automática
              </p>
              <p className="text-sm text-default-600">
                Las estadísticas se actualizan automáticamente cada un periodo de tiempo
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
