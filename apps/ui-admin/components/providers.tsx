'use client';

import { HeroUIProvider } from "@heroui/react";
import { ThemeProvider as NextThemesProvider, useTheme } from "next-themes";
import { useEffect, useState } from "react";
import heroTheme from "@/hero";

function HeroUIProviderWithTheme({ children }: { children: React.ReactNode }) {
  const { theme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return <HeroUIProvider>{children}</HeroUIProvider>;
  }

  return (
    <HeroUIProvider theme={heroTheme}>
      {children}
    </HeroUIProvider>
  );
}

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <NextThemesProvider attribute="class" defaultTheme="system" enableSystem>
      <HeroUIProviderWithTheme>
        {children}
      </HeroUIProviderWithTheme>
    </NextThemesProvider>
  );
}
