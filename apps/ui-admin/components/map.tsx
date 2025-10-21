'use client'

import { useState, useCallback, useMemo } from 'react'
import Map, {
  Marker,
  NavigationControl,
  GeolocateControl,
  FullscreenControl,
  ScaleControl,
  Popup,
  MapProps as ReactMapGLProps
} from 'react-map-gl/mapbox'
import 'mapbox-gl/dist/mapbox-gl.css'
import type { HechoDTO } from '@/types/api'

interface MapComponentProps extends Partial<ReactMapGLProps> {
  /**
   * Array de hechos para mostrar como marcadores en el mapa
   */
  hechos?: HechoDTO[]
  /**
   * Initial longitude for map center
   * @default -58.3816 (Buenos Aires)
   */
  initialLongitude?: number
  /**
   * Initial latitude for map center
   * @default -34.6037 (Buenos Aires)
   */
  initialLatitude?: number
  /**
   * Initial zoom level
   * @default 10
   */
  initialZoom?: number
  /**
   * Map container height
   * @default '100%'
   */
  height?: string | number
  /**
   * Map container width
   * @default '100%'
   */
  width?: string | number
  /**
   * Enable navigation controls
   * @default true
   */
  showControls?: boolean
  /**
   * Enable geolocation control
   * @default true
   */
  showGeolocate?: boolean
  /**
   * Enable fullscreen control
   * @default true
   */
  showFullscreen?: boolean
  /**
   * Enable scale control
   * @default true
   */
  showScale?: boolean
  /**
   * Map style URL
   * @default 'mapbox://styles/mapbox/streets-v12'
   */
  mapStyle?: string
}

export function MapComponent({
  hechos = [],
  initialLongitude = -58.3816,
  initialLatitude = -34.6037,
  initialZoom = 10,
  height = '100%',
  width = '100%',
  showControls = true,
  showGeolocate = true,
  showFullscreen = true,
  showScale = true,
  mapStyle = 'mapbox://styles/mapbox/streets-v12',
  ...mapProps
}: MapComponentProps) {
  const [selectedHecho, setSelectedHecho] = useState<HechoDTO | null>(null)

  // Calcular el centro y zoom basado en los hechos si existen
  const { center, zoom } = useMemo(() => {
    if (hechos.length === 0) {
      return {
        center: { longitude: initialLongitude, latitude: initialLatitude },
        zoom: initialZoom
      }
    }

    // Calcular bounds de todos los hechos
    const lngs = hechos.map(h => h.longitud)
    const lats = hechos.map(h => h.latitud)

    const minLng = Math.min(...lngs)
    const maxLng = Math.max(...lngs)
    const minLat = Math.min(...lats)
    const maxLat = Math.max(...lats)

    // Calcular centro
    const centerLng = (minLng + maxLng) / 2
    const centerLat = (minLat + maxLat) / 2

    // Calcular zoom aproximado basado en la extensión
    const lngDiff = maxLng - minLng
    const latDiff = maxLat - minLat
    const maxDiff = Math.max(lngDiff, latDiff)

    let calculatedZoom = 10
    if (maxDiff > 0) {
      calculatedZoom = Math.floor(Math.log2(360 / maxDiff)) - 1
      calculatedZoom = Math.max(2, Math.min(15, calculatedZoom))
    }

    return {
      center: { longitude: centerLng, latitude: centerLat },
      zoom: hechos.length === 1 ? 14 : calculatedZoom
    }
  }, [hechos, initialLongitude, initialLatitude, initialZoom])

  const [viewState, setViewState] = useState({
    longitude: center.longitude,
    latitude: center.latitude,
    zoom: zoom
  })

  const mapboxToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN

  if (!mapboxToken) {
    return (
      <div
        className="w-full h-full bg-muted flex items-center justify-center border border-destructive"
        style={{ height, width }}
      >
        <div className="text-center space-y-2 p-4">
          <div className="text-4xl">⚠️</div>
          <p className="text-destructive font-semibold">Mapbox Token Missing</p>
          <p className="text-sm text-muted-foreground">
            Please add NEXT_PUBLIC_MAPBOX_TOKEN to your .env.local file
          </p>
        </div>
      </div>
    )
  }

  return (
    <div style={{ height, width }}>
      <Map
        {...viewState}
        {...mapProps}
        onMove={(evt) => setViewState(evt.viewState)}
        mapboxAccessToken={mapboxToken}
        mapStyle={mapStyle}
        style={{ width: '100%', height: '100%' }}
      >
        {/* Marcadores de hechos */}
        {hechos.map((hecho) => (
          <Marker
            key={hecho.uuid}
            longitude={hecho.longitud}
            latitude={hecho.latitud}
            anchor="bottom"
            onClick={(e) => {
              e.originalEvent.stopPropagation()
              setSelectedHecho(hecho)
            }}
          >
            <div
              className="cursor-pointer transition-transform hover:scale-110"
              style={{
                width: '30px',
                height: '30px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <svg
                width="30"
                height="30"
                viewBox="0 0 24 24"
                fill="#ef4444"
                stroke="#dc2626"
                strokeWidth="2"
                style={{ filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.3))' }}
              >
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" />
              </svg>
            </div>
          </Marker>
        ))}

        {/* Popup al seleccionar un hecho */}
        {selectedHecho && (
          <Popup
            longitude={selectedHecho.longitud}
            latitude={selectedHecho.latitud}
            anchor="bottom"
            onClose={() => setSelectedHecho(null)}
            closeButton={true}
            closeOnClick={false}
            maxWidth="300px"
          >
            <div className="p-2">
              <h3 className="font-semibold text-sm mb-1">{selectedHecho.titulo}</h3>
              <p className="text-xs text-muted-foreground mb-2 line-clamp-3">
                {selectedHecho.descripcion}
              </p>
              <div className="flex flex-wrap gap-1 mb-2">
                <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700">
                  {selectedHecho.categoria}
                </span>
              </div>
              <p className="text-xs text-muted-foreground">
                {new Date(selectedHecho.fechaAcontecimiento).toLocaleDateString('es-AR')}
              </p>
            </div>
          </Popup>
        )}

        {/* Controles */}
        {showControls && (
          <NavigationControl position="top-right" />
        )}
        {showGeolocate && (
          <GeolocateControl position="top-right" />
        )}
        {showFullscreen && (
          <FullscreenControl position="top-right" />
        )}
        {showScale && (
          <ScaleControl />
        )}
      </Map>
    </div>
  )
}

// Export types for consumers
export type { MapComponentProps }
export { Marker, Map as ReactMapGL }
