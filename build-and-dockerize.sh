#!/bin/bash

echo "🚀 Construyendo proyecto MetaMapa..."

# Compilar todo el proyecto
echo "📦 Compilando módulos..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación. Abortando."
    exit 1
fi

echo "✅ Compilación exitosa!"

# Construir imágenes Docker
echo "🐳 Construyendo imágenes Docker..."

docker build -t metamapa/fuentes-estatica:latest ./apps/fuentes/estatica/
docker build -t metamapa/fuentes-dinamica:latest ./apps/fuentes/dinamica/
docker build -t metamapa/proxy-metamapa:latest ./apps/fuentes/proxy/metamapa/
docker build -t metamapa/proxy-demo:latest ./apps/fuentes/proxy/demo/
docker build -t metamapa/agregador:latest ./apps/agregador/
docker build -t metamapa/metamapa:latest ./apps/metamapa/

echo "✅ Todas las imágenes Docker construidas exitosamente!"
echo ""
echo "📋 Imágenes disponibles:"
echo "  - metamapa/fuentes-estatica:latest"
echo "  - metamapa/fuentes-dinamica:latest"
echo "  - metamapa/proxy-metamapa:latest"
echo "  - metamapa/proxy-demo:latest"
echo "  - metamapa/agregador:latest"
echo "  - metamapa/metamapa:latest"
echo ""
echo "🚀 Para ejecutar todos los servicios y el stack de monitoreo:"
echo "   docker-compose up -d"
echo ""
echo "🛑 Para detener todos los servicios:"
echo "   docker-compose down"
echo ""
echo "📊 Para acceder a Grafana: http://localhost:3000 (usuario: admin, pass: admin por defecto)"
echo "" 