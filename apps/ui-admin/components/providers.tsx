'use client';

import { useEffect } from 'react';
import { HeroUIProvider } from "@heroui/react";
import { ThemeProvider as NextThemesProvider } from "next-themes";
import { initializeOpenTelemetry } from '@/lib/otel';

export function Providers({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    initializeOpenTelemetry();
  }, []);

  return (
    <NextThemesProvider attribute="class" defaultTheme="system" enableSystem>
      <HeroUIProvider>
        {children}
      </HeroUIProvider>
    </NextThemesProvider>
  );
}
