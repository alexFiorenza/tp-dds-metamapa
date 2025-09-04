# Usar OpenJDK 17 como imagen base
FROM openjdk:17-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR del microservicio
COPY target/agregador-*.jar app.jar

# Copiar recursos como archivos separados
COPY src/main/resources/ src/main/resources/

# Exponer el puerto del servicio
EXPOSE 7005

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]