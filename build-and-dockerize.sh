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

# Fuentes Estáticas
echo "🔨 Construyendo imagen para fuentes estáticas..."
docker build -t metamapa/fuentes-estatica:latest ./apps/fuentes/estatica/

# Fuentes Dinámicas
echo "🔨 Construyendo imagen para fuentes dinámicas..."
docker build -t metamapa/fuentes-dinamica:latest ./apps/fuentes/dinamica/

# Proxy MetaMapa
echo "🔨 Construyendo imagen para proxy metamapa..."
docker build -t metamapa/proxy-metamapa:latest ./apps/fuentes/proxy/metamapa/

# Proxy Demo
echo "🔨 Construyendo imagen para proxy demo..."
docker build -t metamapa/proxy-demo:latest ./apps/fuentes/proxy/demo/

echo "✅ Todas las imágenes Docker construidas exitosamente!"
echo ""
echo "📋 Imágenes disponibles:"
echo "  - metamapa/fuentes-estatica:latest"
echo "  - metamapa/fuentes-dinamica:latest"
echo "  - metamapa/proxy-metamapa:latest"
echo "  - metamapa/proxy-demo:latest"
echo ""
echo "🚀 Para ejecutar todos los servicios:"
echo "   docker-compose up -d"
echo ""
echo "🛑 Para detener todos los servicios:"
echo "   docker-compose down" 