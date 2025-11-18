'use client';

import { useState, useEffect } from 'react';
import { Card, CardBody, Input, Textarea, Select, SelectItem, Button, DatePicker } from '@heroui/react';
import { useRouter, useParams } from 'next/navigation';
import { parseDateTime, getLocalTimeZone, CalendarDate } from '@internationalized/date';
import type { DateValue } from '@internationalized/date';
import { LocationPickerMap } from '@/components/location-picker-map';
import { useUser, useAuth } from '@clerk/nextjs';
import type { HechoDTO } from '@/types/api';

// Obtener categorías desde variable de entorno
const CATEGORIAS = (process.env.NEXT_PUBLIC_CATEGORIAS || "INCENDIO,CONTAMINACION,MANIFESTACION,INUNDACION,FAUNA,ALUD,OTRO").split(",");

const TIPOS_ARCHIVO_PERMITIDOS = [
  'image/jpeg',
  'image/png',
  'image/gif',
  'image/webp',
  'video/mp4',
  'video/mpeg',
  'video/quicktime',
  'video/webm',
];

const TAMANO_MAXIMO = 10 * 1024 * 1024; // 10MB

export default function EditarHechoPage() {
  const router = useRouter();
  const params = useParams();
  const uuid = params.uuid as string;
  const { user, isLoaded } = useUser();
  const { getToken } = useAuth();
  const [loading, setLoading] = useState(false);
  const [loadingHecho, setLoadingHecho] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // Form fields
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [categoria, setCategoria] = useState('');
  const [longitud, setLongitud] = useState<number | null>(null);
  const [latitud, setLatitud] = useState<number | null>(null);
  const [fechaAcontecimiento, setFechaAcontecimiento] = useState<DateValue | null>(null);
  const [etiquetas, setEtiquetas] = useState('');
  const [archivos, setArchivos] = useState<File[]>([]);
  const [hechoOriginal, setHechoOriginal] = useState<HechoDTO | null>(null);

  const handleLocationSelect = (lat: number, lng: number) => {
    setLatitud(lat);
    setLongitud(lng);
  };

  // Cargar el hecho existente
  useEffect(() => {
    const cargarHecho = async () => {
      try {
        setLoadingHecho(true);
        const response = await fetch(`/api/hechos?pagina=0&tamanioPagina=1000`);

        if (!response.ok) {
          throw new Error('Error al cargar los hechos');
        }

        const data = await response.json();
        const hecho = data.datos.find((h: HechoDTO) => h.uuid === uuid);
        
        if (!hecho) {
          throw new Error('Hecho no encontrado');
        }

        // Verificar que el usuario puede editar
        if (!user || !hecho.contribuyente || user.id !== hecho.contribuyente.userId) {
          throw new Error('No tienes permiso para editar este hecho');
        }

        // Verificar tiempo (menos de una semana)
        if (hecho.fechaCarga) {
          const fechaCarga = new Date(hecho.fechaCarga);
          const ahora = new Date();
          const diasTranscurridos = Math.floor((ahora.getTime() - fechaCarga.getTime()) / (1000 * 60 * 60 * 24));
          
          if (diasTranscurridos >= 7) {
            throw new Error('El hecho no puede ser editado: ha pasado más de una semana desde su creación');
          }
        }

        setHechoOriginal(hecho);
        setTitulo(hecho.titulo);
        setDescripcion(hecho.descripcion);
        setCategoria(hecho.categoria);
        setLongitud(hecho.longitud);
        setLatitud(hecho.latitud);
        setEtiquetas(hecho.etiquetas?.join(', ') || '');
        
        if (hecho.fechaAcontecimiento) {
          const fecha = new Date(hecho.fechaAcontecimiento);
          setFechaAcontecimiento(new CalendarDate(fecha.getFullYear(), fecha.getMonth() + 1, fecha.getDate()));
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error al cargar el hecho');
      } finally {
        setLoadingHecho(false);
      }
    };

    if (uuid && isLoaded) {
      cargarHecho();
    }
  }, [uuid, isLoaded, user]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);

    // Validar tipo y tamaño
    const archivosValidos = files.filter(file => {
      if (!TIPOS_ARCHIVO_PERMITIDOS.includes(file.type)) {
        setError(`Tipo de archivo no permitido: ${file.name}. Solo se permiten imágenes y videos.`);
        return false;
      }
      if (file.size > TAMANO_MAXIMO) {
        setError(`Archivo muy grande: ${file.name}. Máximo 10MB.`);
        return false;
      }
      return true;
    });

    setArchivos(archivosValidos);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      // Validar campos requeridos
      if (!titulo || !descripcion || !categoria || longitud === null || latitud === null) {
        throw new Error('Por favor completa todos los campos requeridos');
      }

      // Crear FormData
      const formData = new FormData();
      formData.append('titulo', titulo);
      formData.append('descripcion', descripcion);
      formData.append('categoria', categoria);
      formData.append('longitud', longitud.toString());
      formData.append('latitud', latitud.toString());

      if (fechaAcontecimiento) {
        const isoString = fechaAcontecimiento.toDate(getLocalTimeZone()).toISOString();
        formData.append('fechaAcontecimiento', isoString);
      }
      if (etiquetas) {
        formData.append('etiquetas', etiquetas);
      }

      // Agregar archivos multimedia (opcional, reemplazan los existentes)
      archivos.forEach(archivo => {
        formData.append('multimedia', archivo);
      });

      // Obtener token de sesión
      const token = await getToken();
      if (!token) {
        throw new Error('No se pudo obtener el token de autenticación');
      }

      const response = await fetch(`/api/hechos/${uuid}`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Error al actualizar el hecho');
      }

      const resultado = await response.json();
      console.log('Hecho actualizado:', resultado);

      setSuccess(true);

      // Redirigir después de 2 segundos
      setTimeout(() => {
        router.push('/hechos');
      }, 2000);

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setLoading(false);
    }
  };

  if (loadingHecho) {
    return (
      <div className="p-6 space-y-6 max-w-4xl mx-auto">
        <div className="text-center py-12">
          <p className="text-default-600">Cargando hecho...</p>
        </div>
      </div>
    );
  }

  if (error && !hechoOriginal) {
    return (
      <div className="p-6 space-y-6 max-w-4xl mx-auto">
        <Card className="bg-danger-50 border border-danger-200">
          <CardBody>
            <div className="flex items-center gap-2 text-danger">
              <i className="ri-error-warning-line text-xl" />
              <span className="font-medium">{error}</span>
            </div>
            <Button
              className="mt-4"
              variant="flat"
              onPress={() => router.push('/hechos')}
            >
              Volver a Hechos
            </Button>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 max-w-4xl mx-auto overflow-y-auto h-full">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Editar Hecho</h1>
        <p className="text-default-600 mt-2">
          Modifica la información del hecho. Solo puedes editar hechos que hayas creado y dentro de una semana.
        </p>
      </div>

      {error && (
        <Card className="bg-danger-50 border border-danger-200">
          <CardBody>
            <div className="flex items-center gap-2 text-danger">
              <i className="ri-error-warning-line text-xl" />
              <span className="font-medium">{error}</span>
            </div>
          </CardBody>
        </Card>
      )}

      {success && (
        <Card className="bg-success-50 border border-success-200">
          <CardBody>
            <div className="flex items-center gap-2 text-success">
              <i className="ri-checkbox-circle-line text-xl" />
              <span className="font-medium">¡Hecho actualizado exitosamente! Redirigiendo...</span>
            </div>
          </CardBody>
        </Card>
      )}

      <form onSubmit={handleSubmit}>
        <Card>
          <CardBody className="gap-6">
            {/* Información básica */}
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-foreground">Información Básica</h2>

              <Input
                label="Título"
                placeholder="Ej: Incendio en zona boscosa"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                isRequired
              />

              <Textarea
                label="Descripción"
                placeholder="Describe el hecho en detalle..."
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                isRequired
                minRows={4}
              />

              <Select
                label="Categoría"
                placeholder="Selecciona una categoría"
                selectedKeys={categoria ? [categoria] : []}
                onChange={(e) => setCategoria(e.target.value)}
                isRequired
              >
                {CATEGORIAS.map((cat) => (
                  <SelectItem key={cat}>
                    {cat}
                  </SelectItem>
                ))}
              </Select>
            </div>

            {/* Ubicación */}
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold text-foreground">Ubicación *</h2>
                {latitud !== null && longitud !== null && (
                  <div className="text-sm text-default-600 flex items-center gap-2">
                    <i className="ri-map-pin-line text-primary" />
                    <span className="font-mono">
                      {latitud.toFixed(6)}, {longitud.toFixed(6)}
                    </span>
                  </div>
                )}
              </div>

              <LocationPickerMap
                onLocationSelect={handleLocationSelect}
                initialLat={latitud || undefined}
                initialLng={longitud || undefined}
              />
            </div>

            {/* Información adicional */}
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-foreground">Información Adicional (Opcional)</h2>

              <DatePicker
                label="Fecha del acontecimiento"
                aria-label="Fecha del acontecimiento"
                value={fechaAcontecimiento}
                onChange={setFechaAcontecimiento}
                granularity="day"
              />

              <Input
                label="Etiquetas"
                placeholder="urgente, peligro, atención (separadas por comas)"
                value={etiquetas}
                onChange={(e) => setEtiquetas(e.target.value)}
                description="Separa las etiquetas con comas"
                classNames={{ input: "text-base" }}
              />
            </div>

            {/* Multimedia */}
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-foreground">Multimedia (Opcional)</h2>
              <p className="text-sm text-default-500">
                Si subes nuevos archivos, reemplazarán los existentes.
              </p>

              <div className="space-y-2">
                <input
                  type="file"
                  multiple
                  accept={TIPOS_ARCHIVO_PERMITIDOS.join(',')}
                  onChange={handleFileChange}
                  className="block w-full text-sm text-default-600
                    file:mr-4 file:py-2 file:px-4
                    file:rounded-lg file:border-0
                    file:text-sm file:font-semibold
                    file:bg-primary file:text-primary-foreground
                    hover:file:bg-primary/90
                    cursor-pointer"
                />
                <p className="text-xs text-default-500">
                  Formatos: JPG, PNG, GIF, WebP, MP4, MPEG, MOV, WebM. Máximo 10MB por archivo.
                </p>
              </div>

              {archivos.length > 0 && (
                <div className="space-y-2">
                  <p className="text-sm font-medium">Archivos seleccionados:</p>
                  <ul className="space-y-1">
                    {archivos.map((file, idx) => (
                      <li key={idx} className="text-sm text-default-600 flex items-center gap-2">
                        <i className={file.type.startsWith('video') ? 'ri-video-line' : 'ri-image-line'} />
                        {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            {/* Botones */}
            <div className="flex gap-4 pt-4">
              <Button
                type="submit"
                color="primary"
                size="lg"
                isLoading={loading}
                className="flex-1"
              >
                {loading ? 'Actualizando...' : 'Actualizar Hecho'}
              </Button>

              <Button
                type="button"
                variant="flat"
                size="lg"
                onPress={() => router.push('/hechos')}
                isDisabled={loading}
              >
                Cancelar
              </Button>
            </div>
          </CardBody>
        </Card>
      </form>
    </div>
  );
}

