#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# ---------- Configurables ----------
BUCKET_PRIVATE="${BUCKET_PRIVATE:-metamapa-data}"
BUCKET_PUBLIC="${BUCKET_PUBLIC:-dinamica-multimedia}"
# -----------------------------------

echo "🚀 Construyendo proyecto MetaMapa..."
echo "📦 Compilando módulos Maven (tests omitidos)..."
mvn clean package -DskipTests
echo "✅ Compilación exitosa"

echo "🐳 Construyendo imágenes Docker..."
IMAGES=(
  "metamapa/fuentes-estatica:latest"
  "metamapa/fuentes-dinamica:latest"
  "metamapa/proxy-metamapa:latest"
  "metamapa/proxy-demo:latest"
  "metamapa/agregador:latest"
  "metamapa/normalizador:latest"
  "metamapa/scheduler:latest"
  "metamapa/metamapa:latest"
)

# Limpieza opcional de tags previos (no falla si no existen)
for IMAGE in "${IMAGES[@]}"; do
  if docker image inspect "$IMAGE" >/dev/null 2>&1; then
    echo "   ♻️ Eliminando imagen previa: $IMAGE"
    docker image rm -f "$IMAGE" >/dev/null 2>&1 || true
  fi
done

# Builds
docker build -t metamapa/fuentes-estatica:latest     ./apps/fuentes/estatica/
docker build -t metamapa/fuentes-dinamica:latest     ./apps/fuentes/dinamica/
docker build -t metamapa/proxy-metamapa:latest       ./apps/fuentes/proxy/metamapa/
docker build -t metamapa/proxy-demo:latest           ./apps/fuentes/proxy/demo/
docker build -t metamapa/agregador:latest            ./apps/agregador/
docker build -t metamapa/normalizador:latest         ./apps/normalizador/
docker build -t metamapa/scheduler:latest            ./apps/scheduler/
docker build -t metamapa/metamapa:latest             ./apps/metamapa/

echo "✅ Todas las imágenes Docker construidas"

echo "🔧 Levantando TODO el stack con docker compose..."
docker compose up -d
echo "✅ Stack arriba"

echo "⚙️ Configurando MinIO (buckets y política pública)..."
# Notas:
# - Ejecutamos *dentro* del contenedor de MinIO (servicio: 'minio')
# - Usamos creds nuevas si están, o legacy si no.
# - Retry corto para evitar carreras sin complicar el script.

docker compose exec -T minio sh -c "
  set -eu

  MINIO_USER=\${MINIO_ROOT_USER:-\${MINIO_ACCESS_KEY}}
  MINIO_PASS=\${MINIO_ROOT_PASSWORD:-\${MINIO_SECRET_KEY}}

  # Retry helper (máx ~20s)
  try() { n=0; until \"\$@\"; do n=\$((n+1)); [ \$n -ge 10 ] && return 1; sleep 2; done; }

  # Alias a MinIO local del contenedor
  try mc alias set minio http://127.0.0.1:9000 \"\$MINIO_USER\" \"\$MINIO_PASS\"

  # Crear buckets (idempotente)
  mc mb --ignore-existing minio/${BUCKET_PRIVATE}
  mc mb --ignore-existing minio/${BUCKET_PUBLIC}

  # Hacer público (solo GetObject) el bucket público
  mc anonymous set download minio/${BUCKET_PUBLIC}

  # Verificar política aplicada (no detiene el script si falla el listado)
  mc anonymous list minio/${BUCKET_PUBLIC} || true

  echo '→ Buckets:'
  mc ls minio/ || true
"

echo ""
echo "✅ MinIO configurado:"
echo "   • Bucket privado: ${BUCKET_PRIVATE}"
echo "   • Bucket público: ${BUCKET_PUBLIC}"
echo "   • URL (path-style): http://localhost:9000/${BUCKET_PUBLIC}/<ruta/archivo>"
echo ""

echo "📋 Imágenes disponibles:"
printf '  - %s\n' "${IMAGES[@]}"

echo ""
echo "🚀 Para ver logs rápidos:"
echo "   docker compose logs -f --tail=200"
echo ""
echo "📊 Grafana: http://localhost:3000 (usuario: \${GRAFANA_USER:-admin}, pass: \${GRAFANA_PASSWORD:-admin})"
