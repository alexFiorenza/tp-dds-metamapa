export interface CustomJwtSessionClaims {
  metadata: {
    role?: string;
  };
}

declare global {
  interface CustomJwtSessionClaims {
    metadata: {
      role?: string;
    };
  }
}
