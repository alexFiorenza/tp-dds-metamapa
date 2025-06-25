# Usar OpenJDK 17 como imagen base
FROM openjdk:17-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR del microservicio
COPY target/fuente-proxy-demo-*.jar app.jar

# Exponer el puerto del servicio
EXPOSE 7004

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"] 