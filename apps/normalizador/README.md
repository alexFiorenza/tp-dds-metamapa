# MetaMapa Normalizador

Servicio de normalización de hechos para el sistema MetaMapa. Implementado como Railway Function ultra-liviana en TypeScript.

## 📋 Descripción

El normalizador procesa hechos de diferentes fuentes y los estandariza aplicando transformaciones y validaciones, garantizando consistencia en los datos antes de la persistencia.

## 🏗️ Arquitectura

### Pipeline de Normalización

El normalizador ejecuta 5 normalizadores en secuencia:

1. **TextoNormalizador** (OPCIONAL)
   - Elimina espacios extra
   - Normaliza saltos de línea
   - Limpia formato general

2. **CategoriaNormalizador** (ADVERTENCIA)
   - Categorías configurables vía `src/mapeos/categorias.json`
   - Mapeo exacto (case-insensitive)
   - Mapeo por regex
   - Fuzzy matching (Levenshtein)
   - Fallback a "OTRO"

3. **FechaNormalizador** (ADVERTENCIA)
   - Convierte a formato ISO-8601
   - Elimina timezone
   - Valida formatos

4. **CoordenadasNormalizador** (CRÍTICO)
   - Valida rangos: lat [-90, 90], lng [-180, 180]
   - Redondea a 6 decimales (~11cm precisión)
   - Detecta coordenadas sospechosas (0,0)

5. **EtiquetasNormalizador** (OPCIONAL)
   - Convierte a lowercase
   - Elimina duplicados
   - Ordena alfabéticamente

### Niveles de Validación

- **CRÍTICO**: Error rechaza el hecho completamente
- **ADVERTENCIA**: Error genera advertencia, usa valor por defecto
- **OPCIONAL**: Error se ignora silenciosamente

## 🚀 Uso Local

### Instalación

```bash
cd apps/normalizador
npm install
```

### Desarrollo

```bash
npm run dev  # Hot reload con tsx
```

### Build

```bash
npm run build  # Compila TypeScript a dist/
```

### Producción

```bash
npm start  # Ejecuta dist/index.js
```

## 📡 API

### Health Check

```bash
GET /health

Response:
{
  "status": "ok",
  "service": "metamapa-normalizador",
  "version": "1.0.0",
  "uptime": 123.45,
  "timestamp": "2025-01-11T..."
}
```

### Normalizar Hechos

```bash
POST /normalizar

Request:
{
  "hechos": [
    {
      "uuid": "abc-123",
      "titulo": "  incendio forestal  ",
      "descripcion": "Fuego en zona boscosa",
      "categoria": "fuego",
      "origen": "test",
      "fechaAcontecimiento": "2025-11-19T04:00:00.000Z",
      "fechaCarga": "2025-11-19T05:00:00.000Z",
      "longitud": -58.381592847384,
      "latitud": -34.603722847384,
      "estado": "ACTIVO",
      "etiquetas": ["Urgente", "urgente", "PELIGRO"]
    }
  ],
  "config": {
    "normalizarCategorias": true,
    "usarFuzzyMatching": true,
    "umbralSimilitud": 0.85,
    "rechazarCoordenadasInvalidas": true
  }
}

Response:
{
  "hechosNormalizados": [
    {
      "uuid": "abc-123",
      "titulo": "incendio forestal",
      "descripcion": "Fuego en zona boscosa",
      "categoria": "INCENDIO",
      "origen": "test",
      "fechaAcontecimiento": "2025-11-19T04:00:00",
      "fechaCarga": "2025-11-19T05:00:00",
      "longitud": -58.381593,
      "latitud": -34.603723,
      "estado": "ACTIVO",
      "etiquetas": ["peligro", "urgente"]
    }
  ],
  "hechosRechazados": [],
  "estadisticas": {
    "totalProcesados": 1,
    "totalNormalizados": 1,
    "totalRechazados": 0,
    "cambiosPorTipo": {
      "titulo": 1,
      "categoria": 1,
      "fechaAcontecimiento": 1,
      "fechaCarga": 1,
      "longitud": 1,
      "latitud": 1,
      "etiquetas": 1
    },
    "tiempoEjecucion": 15
  }
}
```

## 🧪 Testing

```bash
# Test básico con curl
curl -X POST http://localhost:3000/normalizar \
  -H "Content-Type: application/json" \
  -d '{
    "hechos": [{
      "uuid": "test-1",
      "titulo": "  Incendio  ",
      "descripcion": "Fuego",
      "categoria": "fuego",
      "origen": "test",
      "fechaAcontecimiento": "2025-11-19",
      "fechaCarga": "2025-11-19",
      "longitud": -58.38,
      "latitud": -34.60,
      "estado": "ACTIVO",
      "etiquetas": ["Test"]
    }]
  }'
```

## 📊 Configuración

### Variables de Entorno

- `PORT`: Puerto del servidor (default: 3000)
- `NODE_ENV`: Entorno (development/production)

### Configuración de Categorías

Las categorías válidas y sus mapeos se configuran en `src/mapeos/categorias.json`:

```json
{
  "categoriasValidas": [
    "INCENDIO",
    "CONTAMINACION",
    "MANIFESTACION",
    "INUNDACION",
    "FAUNA",
    "ALUD"
  ],
  "exactos": {
    "fuego": "INCENDIO",
    "protesta": "MANIFESTACION",
    ...
  },
  "regex": {
    ".*(incendio|fuego).*": "INCENDIO",
    ".*(manifestación|protesta).*": "MANIFESTACION",
    ...
  }
}
```

**Agregar nuevas categorías:**

1. Agregar la categoría a `categoriasValidas`
2. Agregar mapeos exactos en `exactos` (case-insensitive)
3. (Opcional) Agregar patrones regex en `regex`
4. Recompilar: `npm run build`
5. Reiniciar servicio

**Ejemplo:**
```bash
# Editar src/mapeos/categorias.json
vim src/mapeos/categorias.json

# Recompilar
npm run build

# El JSON se copia automáticamente a dist/mapeos/
```

### Configuración del Pipeline

```typescript
interface ConfiguracionNormalizacion {
  normalizarCategorias?: boolean        // default: true
  normalizarFechas?: boolean            // default: true
  normalizarCoordenadas?: boolean       // default: true
  normalizarTexto?: boolean             // default: true
  normalizarEtiquetas?: boolean         // default: true
  usarFuzzyMatching?: boolean           // default: true
  umbralSimilitud?: number              // default: 0.85
  rechazarCoordenadasInvalidas?: boolean // default: true
}
```

## 📁 Estructura

```
src/
├── index.ts                  # HTTP handler principal
├── pipeline.ts               # Orquestador del pipeline
├── types.ts                  # Tipos TypeScript
├── normalizadores/
│   ├── categoria.ts         # Normalización de categorías
│   ├── fecha.ts             # Normalización de fechas
│   ├── coordenadas.ts       # Validación de coordenadas
│   ├── texto.ts             # Limpieza de texto
│   └── etiquetas.ts         # Normalización de etiquetas
├── mapeos/
│   └── categorias.json      # Mapeos de categorías
└── utils/
    └── similitud.ts         # Algoritmo Levenshtein
```

## 🐳 Docker

### Build y Run Local

```bash
# Build imagen
docker build -t metamapa-normalizador .

# Run contenedor
docker run -p 3005:3000 \
  -e NODE_ENV=production \
  -e PORT=3000 \
  metamapa-normalizador
```

### Docker Compose

El normalizador ya está integrado en el `docker-compose.yml` principal:

```bash
# Levantar todo el stack (desde raíz del proyecto)
cd ../..
docker-compose up -d

# Levantar solo normalizador
docker-compose up -d normalizador

# Ver logs del normalizador
docker-compose logs -f normalizador

# Verificar health
curl http://localhost:3005/health
```

**Configuración en docker-compose.yml:**
- Puerto: `3005:3000` (host:container)
- Network: `metamapa-network`
- Health check: `GET /health` cada 30s
- Variables de entorno:
  - `NODE_ENV=production`
  - `PORT=3000`

### Integración con Agregador

El agregador se conecta automáticamente al normalizador:
- URL interna: `http://normalizador:3000`
- Variable de entorno: `NORMALIZADOR_ENABLED=true` (configurable en `.env`)

Para deshabilitar temporalmente:
```bash
# En .env
NORMALIZADOR_ENABLED=false

# O al levantar el stack
NORMALIZADOR_ENABLED=false docker-compose up -d agregador
```

## 🚀 Deployment

### Railway

1. **Crear servicio desde Dockerfile:**
   ```bash
   railway up
   ```

2. **Variables de entorno:**
   ```
   NODE_ENV=production
   PORT=3000
   ```

3. **Configurar en Agregador:**
   ```
   NORMALIZADOR_URL=https://normalizador-production.railway.app
   NORMALIZADOR_ENABLED=true
   ```
