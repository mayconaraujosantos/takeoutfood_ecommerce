# Runtime-only image for optimized deployment
# Build JAR locally first with: ./mvnw clean package -DskipTests -pl auth-service
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app/logs

# Copy JAR from local build (build JAR first with: ./mvnw clean package -DskipTests -pl auth-service)
COPY auth-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

EXPOSE 8081

# Optimized JVM arguments for Alpine
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
