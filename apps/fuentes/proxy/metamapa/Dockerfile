# Usar Eclipse Temurin JRE 17 como imagen base (reemplazo oficial de OpenJDK, con soporte ARM64)
FROM eclipse-temurin:17-jre

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR del microservicio
COPY target/fuente-proxy-metamapa-*.jar app.jar

# Exponer el puerto del servicio
EXPOSE 7003

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"] 