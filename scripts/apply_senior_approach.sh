#!/bin/bash

# Script para aplicar abordagem senior: eliminar pom.docker.xml e otimizar Dockerfiles
# Autor: GitHub Copilot - Outubro 2025

echo "🚀 Iniciando aplicação da abordagem senior para todos os microserviços..."

# Lista dos serviços (exceto config-server que já foi processado)
SERVICES=(
    "service-discovery"
    "api-gateway" 
    "auth-service"
    "user-service"
    "restaurant-service"
    "menu-service"
    "order-service"
    "payment-service"
    "notification-service"
    "delivery-service"
    "review-service"
)

# Mapeamento de portas para cada serviço
declare -A SERVICE_PORTS=(
    ["service-discovery"]="8761"
    ["api-gateway"]="8080"
    ["auth-service"]="8081"
    ["user-service"]="8082"
    ["restaurant-service"]="8083"
    ["menu-service"]="8084"
    ["order-service"]="8085"
    ["payment-service"]="8086"
    ["notification-service"]="8087"
    ["delivery-service"]="8088"
    ["review-service"]="8089"
)

for SERVICE in "${SERVICES[@]}"; do
    echo "📦 Processando $SERVICE..."
    
    # 1. Remover pom.docker.xml se existir
    if [ -f "$SERVICE/pom.docker.xml" ]; then
        echo "  ❌ Removendo pom.docker.xml duplicado"
        rm "$SERVICE/pom.docker.xml"
    fi
    
    # 2. Criar Dockerfile otimizado
    PORT=${SERVICE_PORTS[$SERVICE]}
    echo "  🐳 Criando Dockerfile otimizado (porta $PORT)"
    
    cat > "$SERVICE/Dockerfile" << EOF
# Runtime-only image for optimized deployment
# Build JAR locally first with: ./mvnw clean package -DskipTests -pl $SERVICE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \\
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from local build (build JAR first with: ./mvnw clean package -DskipTests -pl $SERVICE)
COPY $SERVICE/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \\
    CMD wget --no-verbose --tries=1 --spider http://localhost:$PORT/actuator/health || exit 1

EXPOSE $PORT

# Optimized JVM arguments for Alpine
ENTRYPOINT ["java", \\
    "-XX:+UseContainerSupport", \\
    "-XX:MaxRAMPercentage=75.0", \\
    "-XX:+UseG1GC", \\
    "-XX:+UseStringDeduplication", \\
    "-Djava.security.egd=file:/dev/./urandom", \\
    "-jar", \\
    "app.jar"]
EOF
    
    echo "  ✅ $SERVICE processado com sucesso!"
done

echo ""
echo "🎉 Abordagem senior aplicada com sucesso em todos os serviços!"
echo ""
echo "📋 Resumo das melhorias:"
echo "  • Eliminados $(find . -name 'pom.docker.xml' | wc -l | xargs) arquivos pom.docker.xml duplicados"
echo "  • Criados Dockerfiles otimizados para ${#SERVICES[@]} serviços"
echo "  • Configuração de single source of truth implementada"
echo "  • Build process simplificado"
echo ""
echo "🔨 Para construir todos os JARs:"
echo "  ./mvnw clean package -DskipTests"
echo ""
echo "🐳 Para construir imagem Docker de um serviço específico:"
echo "  docker build -f <service>/Dockerfile -t ifood-<service>:latest ."