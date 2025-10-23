#!/bin/bash

# 🚀 Script de inicialização do API Gateway - iFood Clone
# Autor: Sistema iFood Clone
# Versão: 1.0.0

set -e

echo "🚀 Iniciando API Gateway - iFood Clone"
echo "======================================"

# Verificar pré-requisitos
echo "🔍 Verificando pré-requisitos..."

# Verificar Java 21
if ! java -version 2>&1 | grep -q "21"; then
    echo "❌ Java 21 é obrigatório. Por favor instale Java 21."
    exit 1
fi
echo "✅ Java 21 detectado"

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não encontrado. Por favor instale o Maven."
    exit 1
fi
echo "✅ Maven detectado"

# Verificar se as portas estão disponíveis
echo "🔍 Verificando portas necessárias..."

check_port() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "⚠️  Porta $port ($service) já está em uso"
        return 1
    else
        echo "✅ Porta $port ($service) disponível"
        return 0
    fi
}

# Portas necessárias
PORTS_OK=true
check_port 8888 "Config Server" || PORTS_OK=false
check_port 8761 "Service Discovery" || PORTS_OK=false
check_port 6380 "Redis" || PORTS_OK=false

if [ "$PORTS_OK" = false ]; then
    echo ""
    echo "⚠️  Algumas portas necessárias estão em uso."
    echo "📋 Para iniciar os serviços de infraestrutura:"
    echo "   docker-compose up config-server service-discovery redis -d"
    echo ""
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Inicialização cancelada"
        exit 1
    fi
fi

# Configurar variáveis de ambiente
echo "🔧 Configurando variáveis de ambiente..."

export JWT_SECRET=${JWT_SECRET:-"mySecretKey123456789012345678901234567890123456789012345678901234567890"}
export REDIS_HOST=${REDIS_HOST:-"localhost"}
export REDIS_PORT=${REDIS_PORT:-"6380"}
export CONFIG_SERVER_URL=${CONFIG_SERVER_URL:-"http://localhost:8888"}
export EUREKA_SERVER_URL=${EUREKA_SERVER_URL:-"http://localhost:8761"}

echo "✅ Variáveis configuradas:"
echo "   JWT_SECRET: ${JWT_SECRET:0:20}..."
echo "   REDIS_HOST: $REDIS_HOST"
echo "   REDIS_PORT: $REDIS_PORT"
echo "   CONFIG_SERVER: $CONFIG_SERVER_URL"
echo "   EUREKA_SERVER: $EUREKA_SERVER_URL"

# Compilar o projeto se necessário
echo "🔨 Verificando compilação..."

if [ ! -d "target/classes" ]; then
    echo "📦 Compilando API Gateway..."
    mvn clean compile -q
    echo "✅ Compilação concluída"
else
    echo "✅ API Gateway já compilado"
fi

# Função para verificar se um serviço está rodando
check_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "🔍 Verificando $service_name..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            echo "✅ $service_name está rodando"
            return 0
        fi
        
        if [ $attempt -eq 1 ]; then
            echo "⏳ Aguardando $service_name iniciar..."
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name não está respondendo após $((max_attempts * 2)) segundos"
    return 1
}

# Verificar serviços de infraestrutura
echo ""
echo "🔍 Verificando serviços de infraestrutura..."

# Verificar Config Server
if ! check_service "$CONFIG_SERVER_URL/actuator/health" "Config Server"; then
    echo ""
    echo "💡 Para iniciar o Config Server:"
    echo "   cd ../config-server && mvn spring-boot:run"
    echo "   ou"
    echo "   docker-compose up config-server -d"
fi

# Verificar Service Discovery
if ! check_service "$EUREKA_SERVER_URL/actuator/health" "Service Discovery"; then
    echo ""
    echo "💡 Para iniciar o Service Discovery:"
    echo "   cd ../service-discovery && mvn spring-boot:run"
    echo "   ou"  
    echo "   docker-compose up service-discovery -d"
fi

# Verificar Redis (simples verificação de porta)
if ! nc -z $REDIS_HOST $REDIS_PORT 2>/dev/null; then
    echo "⚠️  Redis não está respondendo em $REDIS_HOST:$REDIS_PORT"
    echo ""
    echo "💡 Para iniciar o Redis:"
    echo "   docker-compose up redis -d"
else
    echo "✅ Redis está rodando"
fi

echo ""
echo "🚀 Iniciando API Gateway..."
echo "📱 Porta: 8080"
echo "🌐 Health Check: http://localhost:8080/api/health"
echo "📊 Actuator: http://localhost:8080/actuator"
echo "🔍 Rotas: http://localhost:8080/api/routes"
echo ""
echo "⏹️  Para parar, use Ctrl+C"
echo ""

# Verificar se devemos usar profile local
USE_LOCAL_PROFILE=false

if ! check_service "$CONFIG_SERVER_URL/actuator/health" "Config Server" >/dev/null 2>&1; then
    USE_LOCAL_PROFILE=true
fi

if ! check_service "$EUREKA_SERVER_URL/actuator/health" "Service Discovery" >/dev/null 2>&1; then
    USE_LOCAL_PROFILE=true
fi

if [ "$USE_LOCAL_PROFILE" = true ]; then
    echo "🔧 Iniciando em modo LOCAL (sem dependências externas)"
    echo "📋 Profile: local"
    export SPRING_PROFILES_ACTIVE=local
    mvn spring-boot:run -Dspring-boot.run.profiles=local
else
    echo "🌐 Iniciando em modo DISTRIBUÍDO (com config server e eureka)"
    echo "📋 Profile: default"
    mvn spring-boot:run
fi