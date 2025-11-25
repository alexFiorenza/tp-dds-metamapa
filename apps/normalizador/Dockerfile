# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar package files
COPY package*.json ./
COPY tsconfig.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY src/ ./src/

# Compilar TypeScript
RUN npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

# Copiar solo archivos necesarios para producción
COPY package*.json ./
COPY --from=builder /app/dist ./dist

# Instalar solo dependencias de producción (aunque no hay runtime deps)
RUN npm ci --omit=dev

# Variables de entorno
ENV NODE_ENV=production
ENV PORT=3000

# Exponer puerto
EXPOSE 3000

# Usuario no-root para seguridad
USER node

# Comando de inicio
CMD ["node", "dist/index.js"]
