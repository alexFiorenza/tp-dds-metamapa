# Usar Eclipse Temurin JRE 17 como imagen base (reemplazo oficial de OpenJDK, con soporte ARM64)
FROM eclipse-temurin:17-jre

# Establecer directorio de trabajo
WORKDIR /app

# Descargar OpenTelemetry Java Agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.10.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
RUN chmod 644 /app/opentelemetry-javaagent.jar

# Copiar el JAR del microservicio
COPY target/metamapa-*.jar app.jar

# Variables de entorno para OpenTelemetry
ENV OTEL_SERVICE_NAME=gestor-metamapa
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
ENV OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
ENV OTEL_PROPAGATORS=tracecontext,baggage
ENV JAVA_TOOL_OPTIONS=-javaagent:/app/opentelemetry-javaagent.jar

# Exponer el puerto del servicio
EXPOSE 7006

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]