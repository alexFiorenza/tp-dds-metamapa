import { ImageResponse } from 'next/og'

// Route segment config
export const runtime = 'edge'

// Image metadata
export const size = {
  width: 180,
  height: 180,
}
export const contentType = 'image/png'

// Image generation
export default function AppleIcon() {
  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#8B5CF6',
          borderRadius: '36px',
        }}
      >
        <svg
          width="120"
          height="120"
          viewBox="0 0 100 100"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          {/* LÍNEA CONECTORA (El camino/La M) */}
          <path
            d="M20 75 V 35 L 50 60 L 80 35 V 75"
            stroke="#FFFFFF"
            strokeWidth="10"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* NODOS (Puntos de interés/Datos) */}
          <circle cx="20" cy="35" r="8" fill="#FFFFFF" />
          <circle cx="50" cy="60" r="8" fill="#FFFFFF" />
          <circle cx="80" cy="35" r="8" fill="#FFFFFF" />
        </svg>
      </div>
    ),
    {
      ...size,
    }
  )
}
