import { auth } from '@clerk/nextjs/server';

/**
 * Obtiene el token de sesión de Clerk para usar en llamadas a la API
 *
 * Este helper debe usarse en Server Components o Server Actions.
 * Para Client Components, usa el hook useAuth() de Clerk.
 *
 * @returns El token de sesión JWT o null si no hay sesión
 */
export async function getAuthToken(): Promise<string | null> {
  try {
    const { getToken, userId } = await auth();
    console.log('[auth-helpers] userId:', userId);

    if (!userId) {
      console.warn('[auth-helpers] No hay userId, usuario no autenticado');
      return null;
    }

    const token = await getToken();
    console.log('[auth-helpers] Token obtenido:', token ? `${token.substring(0, 20)}...` : 'null');

    return token;
  } catch (error) {
    console.error('[auth-helpers] Error al obtener token de autenticación:', error);
    return null;
  }
}
