#!/bin/bash

# 🧪 Script de testes do API Gateway - iFood Clone
# Testa todas as funcionalidades principais do gateway

set -e

BASE_URL="http://localhost:8080"
GATEWAY_URL="$BASE_URL/api"

echo "🧪 Testes do API Gateway - iFood Clone"
echo "====================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções utilitárias
print_test() {
    echo -e "\n${BLUE}🧪 Teste: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Verificar se o gateway está rodando
check_gateway() {
    print_test "Verificando se API Gateway está rodando"
    
    if curl -f -s "$BASE_URL/api/health" > /dev/null; then
        print_success "API Gateway está rodando"
        return 0
    else
        print_error "API Gateway não está rodando em $BASE_URL"
        echo "Por favor, inicie o gateway com: ./start-gateway.sh"
        exit 1
    fi
}

# Teste 1: Health Check
test_health_check() {
    print_test "Health Check"
    
    response=$(curl -s -w "%{http_code}" "$GATEWAY_URL/health")
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "200" ]; then
        print_success "Health check passou (HTTP $http_code)"
        echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    else
        print_error "Health check falhou (HTTP $http_code)"
    fi
}

# Teste 2: Info endpoint
test_info_endpoint() {
    print_test "Info Endpoint"
    
    response=$(curl -s -w "%{http_code}" "$GATEWAY_URL/info")
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "200" ]; then
        print_success "Info endpoint passou (HTTP $http_code)"
        echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    else
        print_error "Info endpoint falhou (HTTP $http_code)"
    fi
}

# Teste 3: Routes endpoint
test_routes_endpoint() {
    print_test "Routes Endpoint"
    
    response=$(curl -s -w "%{http_code}" "$GATEWAY_URL/routes")
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "200" ]; then
        print_success "Routes endpoint passou (HTTP $http_code)"
        echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    else
        print_error "Routes endpoint falhou (HTTP $http_code)"
    fi
}

# Teste 4: CORS Headers
test_cors_headers() {
    print_test "CORS Headers"
    
    response=$(curl -s -I -X OPTIONS "$GATEWAY_URL/health" \
        -H "Origin: http://localhost:3000" \
        -H "Access-Control-Request-Method: GET")
    
    if echo "$response" | grep -q "Access-Control-Allow-Origin"; then
        print_success "CORS headers estão presentes"
        echo "$response" | grep "Access-Control"
    else
        print_warning "CORS headers não encontrados"
        echo "Response headers:"
        echo "$response"
    fi
}

# Teste 5: Security Headers
test_security_headers() {
    print_test "Security Headers"
    
    response=$(curl -s -I "$GATEWAY_URL/health")
    
    security_headers=(
        "X-Content-Type-Options"
        "X-Frame-Options"
        "X-XSS-Protection"
        "Strict-Transport-Security"
    )
    
    found_headers=0
    for header in "${security_headers[@]}"; do
        if echo "$response" | grep -qi "$header"; then
            print_success "Header encontrado: $header"
            found_headers=$((found_headers + 1))
        else
            print_warning "Header não encontrado: $header"
        fi
    done
    
    if [ $found_headers -gt 2 ]; then
        print_success "Security headers configurados ($found_headers/4)"
    else
        print_error "Poucos security headers encontrados ($found_headers/4)"
    fi
}

# Teste 6: Rate Limiting (simulação)
test_rate_limiting() {
    print_test "Rate Limiting (10 requisições rápidas)"
    
    success_count=0
    rate_limited_count=0
    
    for i in {1..12}; do
        response=$(curl -s -w "%{http_code}" "$GATEWAY_URL/health")
        http_code="${response: -3}"
        
        if [ "$http_code" = "200" ]; then
            success_count=$((success_count + 1))
        elif [ "$http_code" = "429" ]; then
            rate_limited_count=$((rate_limited_count + 1))
        fi
        
        sleep 0.1
    done
    
    echo "Requisições bem-sucedidas: $success_count"
    echo "Requisições limitadas (429): $rate_limited_count"
    
    if [ $rate_limited_count -gt 0 ]; then
        print_success "Rate limiting está funcionando"
    else
        print_warning "Rate limiting pode não estar ativo (ou limite muito alto)"
    fi
}

# Teste 7: Auth endpoints (sem autenticação)
test_auth_endpoints() {
    print_test "Auth Endpoints (rotas públicas)"
    
    # Teste de rota de auth (deve retornar 404 pois o serviço não está rodando)
    response=$(curl -s -w "%{http_code}" "$GATEWAY_URL/auth/login" -X POST \
        -H "Content-Type: application/json" \
        -d '{"email":"test@test.com","password":"123456"}')
    http_code="${response: -3}"
    
    if [ "$http_code" = "503" ] || [ "$http_code" = "404" ]; then
        print_success "Rota de auth está sendo roteada (HTTP $http_code - serviço não disponível)"
    else
        print_warning "Resposta inesperada para auth: HTTP $http_code"
    fi
}

# Teste 8: Protected endpoints (sem token)
test_protected_endpoints() {
    print_test "Endpoints Protegidos (sem token JWT)"
    
    protected_endpoints=(
        "/users/profile"
        "/orders/list"
        "/payments/history"
    )
    
    for endpoint in "${protected_endpoints[@]}"; do
        response=$(curl -s -w "%{http_code}" "$GATEWAY_URL$endpoint")
        http_code="${response: -3}"
        
        if [ "$http_code" = "401" ]; then
            print_success "Endpoint $endpoint está protegido (HTTP 401)"
        elif [ "$http_code" = "503" ]; then
            print_success "Endpoint $endpoint roteado para serviço indisponível (HTTP 503)"
        else
            print_warning "Endpoint $endpoint: HTTP $http_code (esperado 401 ou 503)"
        fi
    done
}

# Teste 9: Actuator endpoints
test_actuator_endpoints() {
    print_test "Actuator Endpoints"
    
    actuator_endpoints=(
        "/actuator/health"
        "/actuator/info"
        "/actuator/metrics"
    )
    
    for endpoint in "${actuator_endpoints[@]}"; do
        response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint")
        http_code="${response: -3}"
        
        if [ "$http_code" = "200" ]; then
            print_success "Actuator $endpoint: HTTP 200"
        else
            print_warning "Actuator $endpoint: HTTP $http_code"
        fi
    done
}

# Teste 10: Gateway routes (via actuator)
test_gateway_routes_actuator() {
    print_test "Gateway Routes (via Actuator)"
    
    response=$(curl -s -w "%{http_code}" "$BASE_URL/actuator/gateway/routes")
    http_code="${response: -3}"
    body="${response%???}"
    
    if [ "$http_code" = "200" ]; then
        print_success "Gateway routes endpoint: HTTP 200"
        echo "Rotas configuradas:"
        echo "$body" | jq '.[].route_id' 2>/dev/null || echo "Não foi possível parsear JSON"
    else
        print_warning "Gateway routes endpoint: HTTP $http_code"
    fi
}

# Executar todos os testes
main() {
    echo "🏁 Iniciando bateria de testes..."
    
    check_gateway
    
    test_health_check
    test_info_endpoint
    test_routes_endpoint
    test_cors_headers
    test_security_headers
    test_rate_limiting
    test_auth_endpoints
    test_protected_endpoints
    test_actuator_endpoints
    test_gateway_routes_actuator
    
    echo ""
    echo "🎯 Testes concluídos!"
    echo ""
    echo "📋 Próximos passos para testes completos:"
    echo "1. Iniciar os microserviços (auth-service, user-service, etc.)"
    echo "2. Testar autenticação com JWT real"
    echo "3. Testar rate limiting com Redis rodando"
    echo "4. Testar circuit breakers com serviços indisponíveis"
    echo ""
    echo "💡 Para ver logs em tempo real:"
    echo "   tail -f api-gateway.log"
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi