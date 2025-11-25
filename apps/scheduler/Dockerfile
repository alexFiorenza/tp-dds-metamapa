# Usar Eclipse Temurin JRE 17 como imagen base (reemplazo oficial de OpenJDK, con soporte ARM64)
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/scheduler-*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]