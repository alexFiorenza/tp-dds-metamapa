'use client';

import { useState, useEffect } from 'react';
import { Card, CardBody, Select, SelectItem } from '@heroui/react';

export default function EstadisticasPage() {
  const [coleccion, setColeccion] = useState('');
  const [categoria, setCategoria] = useState('');
  const [colecciones, setColecciones] = useState<Array<{ handle: string; titulo: string }>>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categorias] = useState([
    'INCENDIO',
    'CONTAMINACION',
    'MANIFESTACION',
    'INUNDACION',
    'FAUNA',
    'ALUD',
  ]);

  const grafanaBaseUrl = process.env.NEXT_PUBLIC_GRAFANA_DASHBOARD_URL || 'http://localhost:3000/public-dashboards/9cb532f8f1494c41ab55b4466bede283';

  // Cargar colecciones disponibles
  useEffect(() => {
    async function fetchColecciones() {
      try {
        setLoading(true);
        setError(null);
        const response = await fetch('/api/colecciones');

        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        console.log('Colecciones cargadas:', data);

        setColecciones(data.datos || []);

        // Seleccionar la primera colección por defecto
        if (data.datos?.length > 0) {
          setColeccion(data.datos[0].handle);
        }
      } catch (error) {
        console.error('Error cargando colecciones:', error);
        setError(error instanceof Error ? error.message : 'Error desconocido');
      } finally {
        setLoading(false);
      }
    }
    fetchColecciones();
  }, []);

  // Construir URL del dashboard con parámetros
  const dashboardUrl = `${grafanaBaseUrl}${coleccion ? `?var-coleccion=${coleccion}` : ''}${categoria ? `&var-categoria=${categoria}` : ''}`;

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Estadísticas</h1>
        <p className="text-default-600 mt-2">
          Visualiza métricas y análisis de hechos por provincia, categoría y hora
        </p>
      </div>

      {/* Filtros */}
      <Card>
        <CardBody>
          {error && (
            <div className="mb-4 p-3 bg-danger-50 text-danger rounded-lg flex items-center gap-2">
              <i className="ri-error-warning-line text-xl" />
              <span className="text-sm">Error cargando colecciones: {error}</span>
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Colección"
              placeholder={loading ? "Cargando colecciones..." : "Selecciona una colección"}
              selectedKeys={coleccion ? [coleccion] : []}
              onChange={(e) => setColeccion(e.target.value)}
              isDisabled={loading}
              classNames={{
                trigger: "h-12",
              }}
            >
              {colecciones.map((col) => (
                <SelectItem key={col.handle} value={col.handle}>
                  {col.titulo}
                </SelectItem>
              ))}
            </Select>

            <Select
              label="Categoría"
              placeholder="Selecciona una categoría"
              selectedKeys={categoria ? [categoria] : []}
              onChange={(e) => setCategoria(e.target.value)}
              classNames={{
                trigger: "h-12",
              }}
            >
              {categorias.map((cat) => (
                <SelectItem key={cat} value={cat}>
                  {cat}
                </SelectItem>
              ))}
            </Select>
          </div>
        </CardBody>
      </Card>

      {/* Dashboard de Grafana embebido */}
      <Card className="flex-1">
        <CardBody className="p-0">
          <iframe
            src={dashboardUrl}
            width="100%"
            height="800"
            frameBorder="0"
            className="rounded-lg"
            title="Dashboard de Estadísticas MetaMapa"
          />
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
                Las estadísticas se actualizan automáticamente cada 10 minutos.
                Los datos mostrados reflejan solo hechos en estado ACTIVO.
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
