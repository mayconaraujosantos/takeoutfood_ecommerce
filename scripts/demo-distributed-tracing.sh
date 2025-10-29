#!/bin/bash

# Script de demonstração do Distributed Tracing Avançado
# com Custom Spans, Correlation IDs e Métricas Customizadas

echo "🚀 DEMONSTRAÇÃO: Distributed Tracing Avançado com Spring Boot + OpenTelemetry"
echo "================================================================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 FUNCIONALIDADES IMPLEMENTADAS:${NC}"
echo "✅ Custom Spans com @NewSpan e @SpanTag"
echo "✅ Correlation IDs (X-Correlation-ID) com propagação automática"
echo "✅ Distributed Tracing entre múltiplos serviços"
echo "✅ Métricas customizadas (Counters, Timers, Gauges)"
echo "✅ Alertas baseados em latência"
echo "✅ Integração com Jaeger UI"
echo ""

echo -e "${YELLOW}🔧 ARQUITETURA:${NC}"
echo "• Auth Service (8081) - Serviço principal com tracing e métricas"
echo "• User Service (8082) - Serviço secundário para distributed tracing"
echo "• Jaeger (16686) - UI para visualização de traces"
echo "• OpenTelemetry OTLP (4318) - Exportação de traces"
echo ""

echo -e "${GREEN}📊 MÉTRICAS CUSTOMIZADAS IMPLEMENTADAS:${NC}"
echo "• auth.validation.success - Counter de validações bem-sucedidas"
echo "• auth.validation.error - Counter de validações com erro"
echo "• auth.business.operation.duration - Timer de operações de negócio"
echo "• auth.active.connections - Gauge de conexões ativas"
echo "• Alertas automáticos para latência > 500ms e > 1000ms"
echo ""

echo -e "${BLUE}🎯 DEMONSTRAÇÃO DE CUSTOM SPANS:${NC}"
echo "Cada endpoint utiliza anotações @NewSpan e @SpanTag:"
echo "• @NewSpan('operation-name') - Cria spans customizados"
echo "• @SpanTag('key') - Adiciona tags aos spans para filtering/searching"
echo "• Correlation IDs automáticos em todos os requests"
echo ""

echo -e "${GREEN}📡 ENDPOINTS PARA TESTE:${NC}"
echo ""
echo -e "${YELLOW}Auth Service (8081):${NC}"
echo "GET /actuator/health - Health check"
echo "GET /actuator/metrics - Métricas Micrometer"
echo "POST /api/auth/register - Registro com tracing"
echo "POST /api/auth/login - Login com tracing"
echo "GET /api/tracing/validate - Validação com custom spans"
echo "POST /api/tracing/metrics/simulate-load - Simular alta carga"
echo "POST /api/tracing/metrics/simulate-errors - Simular erros"
echo ""

echo -e "${YELLOW}User Service (8082):${NC}"
echo "GET /api/users/health - Health check"
echo "GET /api/users/{userId} - Buscar usuário com tracing"
echo "POST /api/users/validate-with-auth - Distributed tracing (user->auth)"
echo "POST /api/users/distributed-trace-test - Teste completo de tracing"
echo ""

echo -e "${BLUE}🔍 CORRELATION ID FLOW:${NC}"
echo "1. Request recebido sem Correlation ID"
echo "2. CorrelationIdInterceptor gera UUID único"
echo "3. Correlation ID adicionado ao MDC para logs"
echo "4. Correlation ID propagado em chamadas HTTP"
echo "5. Correlation ID incluído em spans OpenTelemetry"
echo "6. Logs estruturados com padrão: [traceId,spanId] [correlationId,requestId]"
echo ""

echo -e "${GREEN}📈 EXEMPLO DE TESTE COMPLETO:${NC}"
echo ""

CORRELATION_ID="demo-$(date +%s)"
echo -e "${YELLOW}Gerando Correlation ID para demo: ${CORRELATION_ID}${NC}"
echo ""

echo -e "${BLUE}1. Testando Auth Service Health:${NC}"
curl -s http://localhost:8081/actuator/health | jq . || echo "❌ Auth Service não está rodando"
echo ""

echo -e "${BLUE}2. Testando User Service Health:${NC}"
# Tentar iniciar user-service se não estiver rodando
if ! curl -s http://localhost:8082/api/users/health >/dev/null 2>&1; then
    echo "⚠️  User Service não está rodando. Iniciando..."
    echo "Execute: java -jar user-service/target/user-service-1.0.0.jar --server.port=8082 --spring.profiles.active=local"
else
    echo "✅ User Service está rodando"
fi
echo ""

echo -e "${GREEN}3. Exemplo de Registro com Tracing:${NC}"
echo "curl -X POST http://localhost:8081/api/auth/register \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'X-Correlation-ID: ${CORRELATION_ID}' \\"
echo "  -d '{\"name\":\"Demo User\",\"email\":\"demo@tracing.com\",\"password\":\"demo123\"}'"
echo ""

echo -e "${GREEN}4. Exemplo de Simulação de Métricas:${NC}"
echo "curl -X POST http://localhost:8081/api/tracing/metrics/simulate-load \\"
echo "  -H 'X-Correlation-ID: ${CORRELATION_ID}'"
echo ""

echo -e "${GREEN}5. Exemplo de Distributed Tracing:${NC}"
echo "curl -X POST http://localhost:8082/api/users/distributed-trace-test \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'X-Correlation-ID: ${CORRELATION_ID}' \\"
echo "  -d 'scenario=chain'"
echo ""

echo -e "${BLUE}🎛️  VISUALIZAÇÃO NO JAEGER:${NC}"
echo "1. Acesse: http://localhost:16686"
echo "2. Service: auth-service ou user-service"
echo "3. Busque por Correlation ID: ${CORRELATION_ID}"
echo "4. Visualize spans customizados e propagação entre serviços"
echo ""

echo -e "${YELLOW}🔧 ESTRUTURA DE LOGS:${NC}"
echo "Pattern: [traceId,spanId] [correlationId,requestId] Logger - Message"
echo "Exemplo: [abc123,def456] [${CORRELATION_ID},req-789] AuthController - Processing login"
echo ""

echo -e "${RED}⚠️  REQUISITOS PARA EXECUÇÃO COMPLETA:${NC}"
echo "1. Jaeger rodando na porta 16686 (UI) e 4318 (OTLP)"
echo "2. Auth Service rodando na porta 8081"
echo "3. User Service rodando na porta 8082 (modo local com H2)"
echo "4. Para ambiente completo: PostgreSQL, Kafka e Service Discovery"
echo ""

echo -e "${GREEN}✨ FEATURES AVANÇADAS IMPLEMENTADAS:${NC}"
echo "• Interceptor automático de Correlation ID"
echo "• Spans customizados com anotações declarativas"
echo "• Métricas de negócio (validações, latência, conexões)"
echo "• Alertas automáticos baseados em latência"
echo "• Tracing distribuído com propagação de contexto"
echo "• Logs estruturados com trace correlation"
echo "• RestTemplate instrumentado automaticamente"
echo ""

echo -e "${BLUE}🚀 Para executar os testes, use os comandos curl acima!${NC}"
echo "================================================================================"