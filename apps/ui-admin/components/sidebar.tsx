'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Logo } from './logo';
import { ThemeSwitcher } from './theme-switcher';
import { SignedIn, SignedOut, SignInButton, UserButton } from '@clerk/nextjs';
import { useUserRole } from '@/hooks/use-user-role';
import { motion, AnimatePresence } from 'motion/react';
import { Button } from '@heroui/react';
import { cn } from '@/lib/utils';

interface NavItemProps {
  href: string;
  icon: string;
  label: string;
  isActive: boolean;
  isCollapsed: boolean;
}

function NavItem({ href, icon, label, isActive, isCollapsed }: NavItemProps) {
  return (
    <Link href={href}>
      <motion.div
        whileHover={{ x: isCollapsed ? 0 : 4 }}
        whileTap={{ scale: 0.95 }}
        className={cn(
          "flex items-center gap-3 px-4 py-3 rounded-lg transition-colors relative group",
          isActive
            ? "bg-primary text-primary-foreground shadow-sm"
            : "text-default-600 hover:bg-default-100",
          isCollapsed && "justify-center px-2"
        )}
        title={isCollapsed ? label : undefined}
      >
        {isActive && !isCollapsed && (
          <motion.div
            layoutId="activeIndicator"
            className="absolute left-0 w-1 h-8 bg-primary rounded-r-full"
            transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
          />
        )}
        <i className={cn(icon, "text-xl shrink-0")} />
        <AnimatePresence>
          {!isCollapsed && (
            <motion.span
              initial={{ opacity: 0, width: 0 }}
              animate={{ opacity: 1, width: "auto" }}
              exit={{ opacity: 0, width: 0 }}
              transition={{ duration: 0.2 }}
              className="font-medium whitespace-nowrap overflow-hidden"
            >
              {label}
            </motion.span>
          )}
        </AnimatePresence>
      </motion.div>
    </Link>
  );
}

interface SidebarProps {
  isCollapsed?: boolean;
  setIsCollapsed?: (collapsed: boolean) => void;
}

export function Sidebar({ isCollapsed: externalIsCollapsed, setIsCollapsed: externalSetIsCollapsed }: SidebarProps = {}) {
  const pathname = usePathname();
  const { isAdmin } = useUserRole();
  const [internalIsCollapsed, setInternalIsCollapsed] = useState(false);

  // Use external state if provided, otherwise use internal state
  const isCollapsed = externalIsCollapsed ?? internalIsCollapsed;
  const setIsCollapsed = externalSetIsCollapsed ?? setInternalIsCollapsed;

  const navItems = [
    { href: '/hechos', icon: 'ri-map-pin-line', label: 'Hechos' },
    { href: '/colecciones', icon: 'ri-folder-line', label: 'Colecciones' },
    { href: '/aportar', icon: 'ri-add-circle-line', label: 'Aportar Hecho' },
  ];

  return (
    <motion.aside
      animate={{ width: isCollapsed ? 80 : 256 }}
      transition={{ duration: 0.3, ease: "easeInOut" }}
      className="h-screen bg-content1 border-r border-divider flex flex-col fixed left-0 top-0 z-50"
      style={{ minWidth: isCollapsed ? 80 : 256 }}
    >
      {/* Logo Section */}
      <div className="border-b border-divider relative">
        <div className="flex items-center justify-between px-4 py-6">
          <Link href="/hechos" className="flex items-center gap-3 overflow-hidden">
            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex items-center gap-3 cursor-pointer"
            >
              <Logo width={48} height={48} showText={!isCollapsed} />
            </motion.div>
          </Link>
        </div>

        {/* Toggle Button - Positioned on the right edge, centered vertically */}
        <div className="absolute top-1/2 -translate-y-1/2 -right-4 z-10">
          <motion.div
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            className="bg-content2 rounded-full p-0.5 shadow-lg"
          >
            <Button
              isIconOnly
              variant="solid"
              color="primary"
              size="sm"
              onPress={() => setIsCollapsed(!isCollapsed)}
              className="rounded-full w-8 h-8 min-w-8 shadow-md"
            >
              <i className={cn("text-base", isCollapsed ? "ri-menu-unfold-2-fill" : "ri-menu-fold-2-fill")} />
            </Button>
          </motion.div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 pt-8 pb-6 space-y-1 overflow-y-auto">
        {navItems.map((item) => (
          <NavItem
            key={item.href}
            href={item.href}
            icon={item.icon}
            label={item.label}
            isActive={pathname?.startsWith(item.href) || false}
            isCollapsed={isCollapsed}
          />
        ))}

        {/* Admin - Solo visible para admins */}
        {isAdmin && (
          <>
            <div className="h-px bg-divider my-4" />
            <NavItem
              href="/administrador"
              icon="ri-admin-line"
              label="Admin"
              isActive={pathname?.startsWith('/administrador') || false}
              isCollapsed={isCollapsed}
            />
          </>
        )}
      </nav>

      {/* User Section */}
      <div className={cn(
        "px-4 py-4 border-t border-divider", 
        isCollapsed 
          ? "flex flex-col items-center gap-3" 
          : "flex flex-row items-center justify-between gap-4"
      )}>
        {/* Theme Switcher */}
        <div className="flex justify-center">
          <ThemeSwitcher />
        </div>
        
        {/* User Profile Section */}
        <div className="flex justify-center">
          <SignedIn>
            <UserButton
              afterSignOutUrl="/"
              appearance={{
                elements: {
                  avatarBox: "w-10 h-10"
                }
              }}
            />
          </SignedIn>
          
          <SignedOut>
            <SignInButton mode="modal">
              <Button
                variant="flat"
                size="sm"
                className={cn(
                  isCollapsed ? "px-2 min-w-0" : "px-4"
                )}
                isIconOnly={isCollapsed}
              >
                <i className="ri-login-box-line text-lg" />
                <AnimatePresence>
                  {!isCollapsed && (
                    <motion.span
                      initial={{ opacity: 0, width: 0 }}
                      animate={{ opacity: 1, width: "auto" }}
                      exit={{ opacity: 0, width: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden whitespace-nowrap"
                    >
                      Iniciar Sesión
                    </motion.span>
                  )}
                </AnimatePresence>
              </Button>
            </SignInButton>
          </SignedOut>
        </div>
      </div>
    </motion.aside>
  );
}

// Export the sidebar widths for use in layout
export const SIDEBAR_WIDTH = 256;
export const SIDEBAR_COLLAPSED_WIDTH = 80;
