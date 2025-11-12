'use client';

import { useEffect, useRef, useState } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

interface LocationPickerMapProps {
  onLocationSelect: (lat: number, lng: number) => void;
  initialLat?: number;
  initialLng?: number;
}

export function LocationPickerMap({ onLocationSelect, initialLat, initialLng }: LocationPickerMapProps) {
  const mapContainer = useRef<HTMLDivElement>(null);
  const map = useRef<mapboxgl.Map | null>(null);
  const marker = useRef<mapboxgl.Marker | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);

  useEffect(() => {
    if (!mapContainer.current) return;

    const token = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;
    if (!token) {
      console.error('Mapbox token not found');
      return;
    }

    // Limpiar mapa existente si hay uno
    if (map.current) {
      map.current.remove();
      map.current = null;
    }
    if (marker.current) {
      marker.current.remove();
      marker.current = null;
    }

    mapboxgl.accessToken = token;

    // Centrar en Argentina por defecto, o en las coordenadas iniciales
    const center: [number, number] = initialLng && initialLat
      ? [initialLng, initialLat]
      : [-63.6167, -38.4161]; // Centro de Argentina

    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v12',
      center,
      zoom: initialLng && initialLat ? 12 : 4,
    });

    // Agregar controles de navegación
    map.current.addControl(new mapboxgl.NavigationControl(), 'top-right');

    // Si hay coordenadas iniciales, agregar un marcador
    if (initialLat && initialLng) {
      marker.current = new mapboxgl.Marker({
        color: '#8B5CF6',
        draggable: true,
      })
        .setLngLat([initialLng, initialLat])
        .addTo(map.current);

      // Actualizar coordenadas al arrastrar el marcador
      marker.current.on('dragend', () => {
        if (marker.current) {
          const lngLat = marker.current.getLngLat();
          onLocationSelect(lngLat.lat, lngLat.lng);
        }
      });
    }

    // Agregar o mover marcador al hacer click en el mapa
    map.current.on('click', (e) => {
      const { lng, lat } = e.lngLat;

      if (marker.current) {
        marker.current.setLngLat([lng, lat]);
      } else {
        marker.current = new mapboxgl.Marker({
          color: '#8B5CF6',
          draggable: true,
        })
          .setLngLat([lng, lat])
          .addTo(map.current!);

        // Actualizar coordenadas al arrastrar el marcador
        marker.current.on('dragend', () => {
          if (marker.current) {
            const lngLat = marker.current.getLngLat();
            onLocationSelect(lngLat.lat, lngLat.lng);
          }
        });
      }

      onLocationSelect(lat, lng);
    });

    map.current.on('load', () => {
      setMapLoaded(true);
    });

    return () => {
      if (marker.current) {
        marker.current.remove();
        marker.current = null;
      }
      if (map.current) {
        map.current.remove();
        map.current = null;
      }
      setMapLoaded(false);
    };
  }, []);

  // Actualizar marcador cuando cambian las coordenadas externas
  useEffect(() => {
    if (!map.current || !mapLoaded) return;

    if (initialLat && initialLng) {
      if (marker.current) {
        marker.current.setLngLat([initialLng, initialLat]);
      } else {
        marker.current = new mapboxgl.Marker({
          color: '#8B5CF6',
          draggable: true,
        })
          .setLngLat([initialLng, initialLat])
          .addTo(map.current);

        // Actualizar coordenadas al arrastrar el marcador
        marker.current.on('dragend', () => {
          if (marker.current) {
            const lngLat = marker.current.getLngLat();
            onLocationSelect(lngLat.lat, lngLat.lng);
          }
        });
      }

      map.current.flyTo({
        center: [initialLng, initialLat],
        zoom: 12,
        duration: 1000,
      });
    }
  }, [initialLat, initialLng, mapLoaded, onLocationSelect]);

  return (
    <div className="space-y-2">
      <div
        ref={mapContainer}
        className="w-full h-[400px] rounded-lg border-2 border-divider overflow-hidden"
      />
      <p className="text-xs text-default-500 flex items-center gap-2">
        <i className="ri-map-pin-line" />
        Haz clic en el mapa para seleccionar una ubicación, o arrastra el marcador
      </p>
    </div>
  );
}
