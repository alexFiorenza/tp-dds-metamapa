#!/bin/bash

echo "🚀 Construyendo proyecto MetaMapa..."
echo ""

# Detener contenedores en ejecución
echo "🛑 Deteniendo contenedores existentes..."
docker compose down

# Eliminar imágenes existentes del proyecto
echo "🗑️  Eliminando imágenes Docker existentes..."
docker rmi -f metamapa/fuentes-estatica:latest 2>/dev/null || true
docker rmi -f metamapa/fuentes-dinamica:latest 2>/dev/null || true
docker rmi -f metamapa/proxy-metamapa:latest 2>/dev/null || true
docker rmi -f metamapa/proxy-demo:latest 2>/dev/null || true
docker rmi -f metamapa/agregador:latest 2>/dev/null || true
docker rmi -f metamapa/metamapa:latest 2>/dev/null || true

echo "✅ Imágenes antiguas eliminadas!"
echo ""

# Compilar todo el proyecto
echo "📦 Compilando módulos con Maven..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación. Abortando."
    exit 1
fi

echo "✅ Compilación exitosa!"
echo ""

# Levantar servicios con Docker Compose
echo "🐳 Levantando servicios con Docker Compose..."
docker compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Error al levantar los servicios. Verifica docker-compose.yml"
    exit 1
fi

echo ""
echo "✅ Todos los servicios están corriendo!"
echo ""
echo "📋 Servicios disponibles:"
echo "  - Fuentes Estáticas:    http://localhost:7001"
echo "  - Fuentes Dinámicas:    http://localhost:7002"
echo "  - Proxy MetaMapa:       http://localhost:7003"
echo "  - Proxy Demo:           http://localhost:7004"
echo "  - Agregador:            http://localhost:7005"
echo "  - MetaMapa:             http://localhost:7006"
echo "  - Prometheus:           http://localhost:9090"
echo "  - Alertmanager:         http://localhost:9093"
echo "  - Grafana:              http://localhost:3000 (admin/admin)"
echo ""
echo "📊 Para ver logs:         docker compose logs -f"
echo "🛑 Para detener todo:     docker compose down"
echo "" 