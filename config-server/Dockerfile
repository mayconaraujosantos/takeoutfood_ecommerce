# Runtime-only image for optimized deployment
# Build JAR locally first with: ./mvnw clean package -DskipTests -pl config-server
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="iFood Clone Team"
LABEL service="config-server"
LABEL version="1.0"

# Install wget for healthcheck and create non-root user
RUN apk add --no-cache wget && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create app directory
WORKDIR /app

# Copy JAR from local build (build JAR first with: ./mvnw clean package -DskipTests -pl config-server)
COPY config-server/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

EXPOSE 8888

# Optimized JVM arguments for Alpine
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
