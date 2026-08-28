# Multi-stage build: compiles the module inside the image, no local pre-build required.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

COPY . .

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && \
    ./mvnw clean package spring-boot:repackage -DskipTests -pl delivery-service -am

# Runtime-only image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=build /build/delivery-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8088/actuator/health || exit 1

EXPOSE 8088

# Optimized JVM arguments for Alpine
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
