'use client';

import Link from 'next/link';
import { Button } from '@heroui/react';
import { SignInButton, SignUpButton, SignedIn, SignedOut, UserButton } from '@clerk/nextjs';
import { ThemeSwitcher } from './theme-switcher';
import { useUserRole } from '@/hooks/use-user-role';

export function Header() {
  const { isAdmin } = useUserRole();

  return (
    <header className="flex justify-between items-center px-6 py-4 gap-4 h-16 border-b border-divider bg-background">
      <div className="flex items-center gap-6">
        <Link href="/" className="text-xl font-bold text-foreground">
          MetaMapa
        </Link>
        <SignedIn>
          <nav className="flex items-center gap-4">
            <Link href="/hechos">
              <Button variant="light" size="sm">
                Hechos
              </Button>
            </Link>
            {isAdmin && (
              <>
                <Link href="/administrador">
                  <Button variant="light" size="sm">
                    Dashboard
                  </Button>
                </Link>
                <Link href="/administrador/colecciones">
                  <Button variant="light" size="sm">
                    Colecciones
                  </Button>
                </Link>
                <Link href="/administrador/solicitudes">
                  <Button variant="light" size="sm">
                    Solicitudes
                  </Button>
                </Link>
              </>
            )}
          </nav>
        </SignedIn>
      </div>
      <div className="flex items-center gap-3">
        <ThemeSwitcher />
        <SignedOut>
          <SignInButton mode="modal">
            <Button variant="flat" size="sm">
              Iniciar sesión
            </Button>
          </SignInButton>
          <SignUpButton mode="modal">
            <Button color="primary" size="sm">
              Registrarse
            </Button>
          </SignUpButton>
        </SignedOut>
        <SignedIn>
          <UserButton afterSignOutUrl="/" />
        </SignedIn>
      </div>
    </header>
  );
}
