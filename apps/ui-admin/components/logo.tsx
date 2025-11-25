'use client';

import React from 'react';
import { useTheme } from 'next-themes';

interface LogoProps {
  /**
   * Width/Height of the logo (square)
   * @default 48
   */
  width?: number;
  height?: number;
  /**
   * Additional CSS classes
   */
  className?: string;
  /**
   * Show text next to the logo
   * @default false
   */
  showText?: boolean;
  /**
   * Force a specific variant (overrides theme detection)
   */
  variant?: 'light' | 'dark' | 'auto';
}

export const Logo: React.FC<LogoProps> = ({
  width = 48,
  height = 48,
  className = '',
  showText = false,
  variant = 'auto'
}) => {
  const { resolvedTheme } = useTheme();
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => {
    setMounted(true);
  }, []);

  // Determine the color palette based on variant or theme
  const isDark = variant === 'auto'
    ? (mounted && resolvedTheme === 'dark')
    : variant === 'dark';

  // Color palettes
  const colors = {
    light: {
      line: "#8B5CF6",    // Primary
      dots: "#6328C7",    // Secondary (Darker Purple)
      text: "#3D2B6B",    // Foreground
      subtext: "#6F5C8F"  // Default 600
    },
    dark: {
      line: "#A78BFA",    // Primary (Dark mode)
      dots: "#EEEBF4",    // Foreground (Light)
      text: "#EEEBF4",    // Foreground
      subtext: "#B7A9CB"  // Default 500
    }
  };

  const activePalette = isDark ? colors.dark : colors.light;

  // Show a neutral version during SSR
  if (!mounted) {
    return (
      <div className={`flex items-center gap-3 select-none ${className}`} style={{ width: showText ? 'auto' : width }}>
        <div style={{ width, height }} className="relative flex-shrink-0">
          <svg
            viewBox="0 0 100 100"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="w-full h-full drop-shadow-sm"
          >
            <path
              d="M20 75 V 35 L 50 60 L 80 35 V 75"
              stroke="currentColor"
              strokeWidth="10"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <circle cx="20" cy="35" r="8" fill="currentColor" />
            <circle cx="50" cy="60" r="8" fill="currentColor" />
            <circle cx="80" cy="35" r="8" fill="currentColor" />
          </svg>
        </div>
        {showText && (
          <div className="flex flex-col justify-center">
            <span className="font-bold text-2xl tracking-tighter leading-none">
              MetaMapa
            </span>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className={`flex items-center gap-3 select-none ${className}`} style={{ width: showText ? 'auto' : width }}>
      <div style={{ width, height }} className="relative flex-shrink-0">
        <svg
          viewBox="0 0 100 100"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-full drop-shadow-sm"
        >
          <title>MetaMapa Isotipo</title>
          {/* LÍNEA CONECTORA (El camino/La M) */}
          <path
            d="M20 75 V 35 L 50 60 L 80 35 V 75"
            stroke={activePalette.line}
            strokeWidth="10"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="transition-all duration-300"
          />

          {/* NODOS (Puntos de interés/Datos) */}
          <circle cx="20" cy="35" r="8" fill={activePalette.dots} className="transition-all duration-300" />
          <circle cx="50" cy="60" r="8" fill={activePalette.dots} className="transition-all duration-300" />
          <circle cx="80" cy="35" r="8" fill={activePalette.dots} className="transition-all duration-300" />
        </svg>
      </div>

      {showText && (
        <div className="flex flex-col justify-center">
          <span
            className="font-bold text-2xl tracking-tighter leading-none transition-colors duration-300"
            style={{ color: activePalette.text }}
          >
            MetaMapa
          </span>
        </div>
      )}
    </div>
  );
};
