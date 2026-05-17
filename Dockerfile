# =============================================
# STAGE 1: Build
# =============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar solo el pom.xml primero para cachear dependencias
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar fuentes y compilar
COPY src/ src/
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# =============================================
# STAGE 2: Runtime (imagen mínima)
# =============================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Usuario no root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar JAR desde build stage
COPY --from=builder /app/target/*.jar app.jar

# Permisos de solo lectura
RUN chown appuser:appgroup app.jar && chmod 400 app.jar

USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

# JVM optimizada para containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+OptimizeStringConcat", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]