#!/bin/bash

# 🧪 Teste rápido do API Gateway em modo local
# Este script testa o gateway sem dependências externas

echo "🧪 Testando API Gateway - Modo Local"
echo "=================================="

GATEWAY_URL="http://localhost:8080"
MAX_WAIT=30
WAIT_COUNT=0

# Função para aguardar o gateway iniciar
wait_for_gateway() {
    echo "⏳ Aguardando gateway iniciar..."
    
    while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
        if curl -f -s "$GATEWAY_URL/api/health" > /dev/null 2>&1; then
            echo "✅ Gateway está rodando!"
            return 0
        fi
        
        sleep 2
        WAIT_COUNT=$((WAIT_COUNT + 1))
        echo -n "."
    done
    
    echo "❌ Gateway não respondeu após $((MAX_WAIT * 2)) segundos"
    return 1
}

# Executar testes apenas se o gateway estiver rodando
if curl -f -s "$GATEWAY_URL/api/health" > /dev/null 2>&1; then
    echo "✅ Gateway já está rodando"
elif wait_for_gateway; then
    echo "✅ Gateway iniciado com sucesso"
else
    echo "❌ Não foi possível conectar ao gateway"
    echo "💡 Execute: ./start-gateway.sh"
    exit 1
fi

echo ""
echo "🔍 Executando testes básicos..."

# Teste 1: Health Check
echo -n "🔍 Health Check: "
if curl -f -s "$GATEWAY_URL/api/health" | grep -q '"status":"UP"'; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 2: Info endpoint
echo -n "🔍 Info endpoint: "
if curl -f -s "$GATEWAY_URL/api/info" | grep -q 'api-gateway'; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 3: Routes endpoint
echo -n "🔍 Routes endpoint: "
if curl -f -s "$GATEWAY_URL/api/routes" > /dev/null 2>&1; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 4: Actuator health
echo -n "🔍 Actuator health: "
if curl -f -s "$GATEWAY_URL/actuator/health" | grep -q '"status":"UP"'; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 5: Gateway routes (actuator)
echo -n "🔍 Gateway routes: "
if curl -f -s "$GATEWAY_URL/actuator/gateway/routes" > /dev/null 2>&1; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 6: CORS headers
echo -n "🔍 CORS headers: "
CORS_RESPONSE=$(curl -s -I -X OPTIONS "$GATEWAY_URL/api/health" \
    -H "Access-Control-Request-Method: GET" \
    -H "Access-Control-Request-Headers: Authorization")

if echo "$CORS_RESPONSE" | grep -qi "access-control-allow"; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

# Teste 7: Security headers
echo -n "🔍 Security headers: "
SECURITY_HEADERS=$(curl -s -I "$GATEWAY_URL/api/health")

if echo "$SECURITY_HEADERS" | grep -qi "x-content-type-options"; then
    echo "✅ PASSOU"
else
    echo "❌ FALHOU"
fi

echo ""
echo "📊 Informações do Gateway:"
echo "========================="

# Mostrar info do gateway
echo "📱 URL: $GATEWAY_URL"
echo "🔍 Health: $GATEWAY_URL/api/health"
echo "📊 Metrics: $GATEWAY_URL/actuator/metrics"
echo "🛣️ Routes: $GATEWAY_URL/actuator/gateway/routes"

echo ""
echo "📋 Exemplo de uso:"
echo "=================="
echo "# Health check"
echo "curl $GATEWAY_URL/api/health"
echo ""
echo "# Ver rotas disponíveis"
echo "curl $GATEWAY_URL/api/routes"
echo ""
echo "# Métricas do sistema"
echo "curl $GATEWAY_URL/actuator/metrics"

echo ""
echo "🎉 Testes concluídos! Gateway funcionando em modo local."