'use client'

import dynamic from 'next/dynamic'
import { MapPlaceholder } from './map-placeholder'
import type { MapComponentProps } from './map'
import type { HechoDTO } from '@/types/api'

// Dynamically import the map component with SSR disabled
// This ensures that Mapbox GL only loads on the client side
const MapComponent = dynamic(
  () => import('./map').then((mod) => mod.MapComponent),
  {
    ssr: false,
    loading: () => <MapPlaceholder />
  }
)

/**
 * MapWrapper component that handles SSR by dynamically loading the Mapbox map
 * only on the client side. Use this component in your pages/components.
 *
 * @example
 * ```tsx
 * import { MapWrapper } from '@/components/map-wrapper'
 *
 * export default function Page() {
 *   return (
 *     <div className="h-screen">
 *       <MapWrapper
 *         hechos={hechos}
 *         initialLatitude={-34.6037}
 *         initialLongitude={-58.3816}
 *         initialZoom={12}
 *       />
 *     </div>
 *   )
 * }
 * ```
 */
export function MapWrapper(props: MapComponentProps) {
  return <MapComponent {...props} />
}

export type { MapComponentProps, HechoDTO }
