# ==========================================
# Etapa 1: Build de la aplicacion con Maven
# ==========================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copiar proyecto y compilar empaquetando JAR
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B -Djacoco.skip=true -Dpitest.skip=true

# ==========================================
# Etapa 2: Imagen Final Ligera de Ejecucion
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copiar artefacto final generado en etapa builder
COPY --from=builder /app/target/order-management-api.jar app.jar

# Variables de entorno y puerto expuesto
ENV PORT=8080
EXPOSE 8080

# Healthcheck usando endpoint Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
