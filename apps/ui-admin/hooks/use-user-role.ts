'use client';

import { useUser } from "@clerk/nextjs";

export type UserRole = 'admin' | 'user' | 'guest';

export function useUserRole() {
  const { user, isLoaded, isSignedIn } = useUser();

  if (!isLoaded) {
    return {
      role: 'guest' as UserRole,
      isAdmin: false,
      isUser: false,
      isGuest: true,
      isLoading: true,
    };
  }

  if (!isSignedIn || !user) {
    return {
      role: 'guest' as UserRole,
      isAdmin: false,
      isUser: false,
      isGuest: true,
      isLoading: false,
    };
  }

  const userRole = (user.publicMetadata?.role as UserRole) || 'user';

  return {
    role: userRole,
    isAdmin: userRole === 'admin',
    isUser: userRole === 'user',
    isGuest: userRole === 'guest',
    isLoading: false,
  };
}
