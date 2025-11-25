# Usar Eclipse Temurin JRE 17 como imagen base (reemplazo oficial de OpenJDK, con soporte ARM64)
FROM eclipse-temurin:17-jre

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR del microservicio
COPY target/fuente-dinamica-*.jar app.jar

# Copiar recursos como archivos separados
COPY src/main/resources/ src/main/resources/

# Exponer el puerto del servicio
EXPOSE 7002

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"] 