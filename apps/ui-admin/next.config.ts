import type { NextConfig } from "next";

const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
})

const nextConfig: NextConfig = {
  /* config options here */
  // Optimizaciones de producción
  reactStrictMode: true,

  // Compresión automática
  compress: true,

  // Optimización de imágenes
  images: {
    formats: ['image/avif', 'image/webp'],
  },

  // Deshabilitar ESLint durante build (opcional)
  eslint: {
    ignoreDuringBuilds: true,
  },
};

export default withBundleAnalyzer(nextConfig);
