'use client';

import { useState, useEffect } from 'react';
import { Card, CardBody, Input, Textarea, Select, SelectItem, Button, DatePicker } from '@heroui/react';
import { useRouter } from 'next/navigation';
import { parseDateTime, getLocalTimeZone } from '@internationalized/date';
import type { DateValue } from '@internationalized/date';
import { LocationPickerMap } from '@/components/location-picker-map';
import { useUser, useAuth } from '@clerk/nextjs';
import { Switch } from '@heroui/react';

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

export default function AportarHechoPage() {
  const router = useRouter();
  const { user, isLoaded } = useUser();
  const { getToken } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [identificarseComoContribuyente, setIdentificarseComoContribuyente] = useState(false);

  // Form fields
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [categoria, setCategoria] = useState('');
  const [longitud, setLongitud] = useState<number | null>(null);
  const [latitud, setLatitud] = useState<number | null>(null);
  const [fechaAcontecimiento, setFechaAcontecimiento] = useState<DateValue | null>(null);

  const handleLocationSelect = (lat: number, lng: number) => {
    setLatitud(lat);
    setLongitud(lng);
  };
  const [contribuyenteNombre, setContribuyenteNombre] = useState('');
  const [etiquetas, setEtiquetas] = useState('');
  const [archivos, setArchivos] = useState<File[]>([]);

  // Setear nombre del contribuyente desde el usuario logueado
  useEffect(() => {
    if (isLoaded && user) {
      const nombre = user.fullName || user.firstName || user.username || user.emailAddresses[0]?.emailAddress || '';
      setContribuyenteNombre(nombre);
    }
  }, [isLoaded, user]);

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
      if (contribuyenteNombre) {
        formData.append('contribuyenteNombre', contribuyenteNombre);
      }
      if (etiquetas) {
        formData.append('etiquetas', etiquetas);
      }

      // Agregar archivos multimedia
      archivos.forEach(archivo => {
        formData.append('multimedia', archivo);
      });

      const apiUrl = process.env.NEXT_PUBLIC_FUENTE_DINAMICA_URL || 'http://localhost:7002';
      
      // Preparar headers
      const headers: HeadersInit = {};
      
      // Si el usuario quiere identificarse, enviar el token de sesión
      if (identificarseComoContribuyente && user) {
        const token = await getToken();
        console.log('Token obtenido:', token ? 'presente' : 'ausente');
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
          console.log('Header Authorization agregado al request');
        } else {
          console.warn('No se pudo obtener el token de Clerk');
        }
      } else {
        console.log('Usuario no quiere identificarse como contribuyente o no está logueado');
      }
      
      console.log('Enviando request a:', `${apiUrl}/hechos`);
      console.log('Headers:', Object.keys(headers));
      
      const response = await fetch(`${apiUrl}/hechos`, {
        method: 'POST',
        headers,
        body: formData,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Error al aportar el hecho');
      }

      const resultado = await response.json();
      console.log('Hecho aportado:', resultado);

      setSuccess(true);

      // Limpiar formulario
      setTitulo('');
      setDescripcion('');
      setCategoria('');
      setLongitud(null);
      setLatitud(null);
      setFechaAcontecimiento(null);
      setContribuyenteNombre('');
      setEtiquetas('');
      setArchivos([]);
      setIdentificarseComoContribuyente(false);

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

  return (
    <div className="p-6 space-y-6 max-w-4xl mx-auto overflow-y-auto h-full">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Aportar Hecho</h1>
        <p className="text-default-600 mt-2">
          Comparte información sobre eventos relevantes en tu comunidad
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
              <span className="font-medium">¡Hecho aportado exitosamente! Redirigiendo...</span>
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
                granularity="minute"
                hideTimeZone
              />

              {user && (
                <div className="space-y-2">
                  <Switch
                    isSelected={identificarseComoContribuyente}
                    onValueChange={setIdentificarseComoContribuyente}
                    classNames={{
                      base: "w-full",
                      wrapper: "p-0 h-4 overflow-visible",
                      thumb: "w-6 h-6 border-2 shadow-lg",
                    }}
                  >
                    <div className="flex flex-col gap-1">
                      <p className="text-medium">Identificarme como contribuyente</p>
                      <p className="text-tiny text-default-500">
                        Esto te permitirá editar este hecho más adelante (hasta una semana después de crearlo)
                      </p>
                    </div>
                  </Switch>
                </div>
              )}

              <Input
                label="Tu nombre"
                placeholder="Nombre del contribuyente"
                value={contribuyenteNombre}
                onChange={(e) => setContribuyenteNombre(e.target.value)}
                isDisabled={!!user}
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
                {loading ? 'Enviando...' : 'Aportar Hecho'}
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
