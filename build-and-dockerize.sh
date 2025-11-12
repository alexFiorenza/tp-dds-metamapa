#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# ---------- Configurables ----------
BUCKET_PRIVATE="${BUCKET_PRIVATE:-metamapa-data}"
BUCKET_PUBLIC="${BUCKET_PUBLIC:-dinamica-multimedia}"
# -----------------------------------

echo "🚀 Construyendo proyecto MetaMapa..."
echo ""
echo "📦 Compilando módulos Maven (tests omitidos)..."
mvn clean package -DskipTests
echo "✅ Compilación Maven exitosa"
echo ""
echo "📦 Compilando Normalizador (TypeScript)..."
cd apps/normalizador
if [ -d "node_modules" ]; then
  echo "   ✓ node_modules existe, actualizando dependencias..."
  npm install
else
  echo "   → Instalando dependencias..."
  npm install
fi
echo "   → Compilando TypeScript..."
npm run build
echo "✅ Compilación Normalizador exitosa"
cd "$ROOT_DIR"
echo ""

echo "🐳 Limpiando imágenes Docker antiguas..."
# Detener servicios si están corriendo
docker compose down 2>/dev/null || true

# Limpiar imágenes antiguas (opcional, para forzar rebuild completo)
echo "   ♻️ Eliminando imágenes previas de MetaMapa..."
docker images | grep metamapa | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

echo "✅ Limpieza completada"

echo "🔧 Construyendo y levantando stack con docker compose..."
docker compose up -d --build
echo "✅ Stack arriba y construido"

echo "⚙️ Configurando MinIO (buckets y política pública)..."

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

echo "🚀 Para ver logs rápidos:"
echo "   docker compose logs -f --tail=200"
echo ""
echo "📊 Servicios principales:"
echo "   • Fuente Estática:    http://localhost:7001"
echo "   • Fuente Dinámica:    http://localhost:7002"
echo "   • Proxy MetaMapa:     http://localhost:7003"
echo "   • Proxy Demo:         http://localhost:7004"
echo "   • Agregador:          http://localhost:7005"
echo "   • MetaMapa API:       http://localhost:7006"
echo "   • Normalizador:       http://localhost:3005 (health: /health)"
echo ""
echo "📈 Monitoreo y administración:"
echo "   • Grafana:            http://localhost:3000 (admin/admin)"
echo "   • pgAdmin:            http://localhost:5050 (admin@metamapa.com/admin123)"
echo "   • Prometheus:         http://localhost:9090"
echo "   • Alertmanager:       http://localhost:9093"
echo ""
echo "💾 Almacenamiento:"
echo "   • MinIO Console:      http://localhost:9001"
echo "   • CouchDB Fauxton:    http://localhost:5984/_utils (admin/admin123)"
echo "   • PostgreSQL:         localhost:5432 (metamapa/metamapa123)"
echo ""
echo "⏰ Tareas programadas:"
echo "   • Scheduler (Ofelia): Actualiza estadísticas cada 10 minutos"
echo ""
