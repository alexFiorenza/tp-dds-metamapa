'use client';

import { createContext, useContext, useState, type ReactNode } from 'react';
import { Sidebar } from './sidebar';

interface SidebarContextType {
  isCollapsed: boolean;
  setIsCollapsed: (collapsed: boolean) => void;
}

const SidebarContext = createContext<SidebarContextType | undefined>(undefined);

export function useSidebarContext() {
  const context = useContext(SidebarContext);
  if (!context) {
    throw new Error('useSidebarContext must be used within LayoutWrapper');
  }
  return context;
}

export function LayoutWrapper({ children }: { children: ReactNode }) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const marginLeft = isCollapsed ? 80 : 256;

  return (
    <SidebarContext.Provider value={{ isCollapsed, setIsCollapsed }}>
      <Sidebar isCollapsed={isCollapsed} setIsCollapsed={setIsCollapsed} />
      <main 
        className="h-screen overflow-hidden transition-[margin] duration-300 ease-in-out" 
        style={{ marginLeft: `${marginLeft}px` }}
      >
        {children}
      </main>
    </SidebarContext.Provider>
  );
}
