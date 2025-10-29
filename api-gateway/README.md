# 🚀 API Gateway - iFood Clone

## 📋 Descrição

O API Gateway é o ponto central de entrada para todos os microserviços do sistema iFood Clone. Ele fornece:

- **Roteamento Inteligente**: Direciona requisições para os serviços apropriados
- **Autenticação & Autorização**: Validação JWT e controle de acesso
- **Rate Limiting**: Proteção contra abuso e overload
- **Circuit Breaker**: Resiliência contra falhas de serviços
- **Logging Centralizado**: Rastreabilidade completa de requisições
- **Monitoramento**: Métricas e health checks
- **Segurança**: Headers de segurança e validação de conteúdo

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│   API Gateway    │───▶│  Microservices  │
│                 │    │                  │    │                 │
│ • Web App       │    │ • Authentication │    │ • Auth Service  │
│ • Mobile App    │    │ • Rate Limiting  │    │ • User Service  │
│ • External APIs │    │ • Load Balancing │    │ • Order Service │
└─────────────────┘    │ • Circuit Breaker│    │ • etc...        │
                       └──────────────────┘    └─────────────────┘
```

## 🛣️ Rotas Disponíveis

### 🔓 **Rotas Públicas** (Sem Autenticação)
```
POST /api/auth/login              - Login de usuário
POST /api/auth/register           - Registro de usuário
GET  /api/restaurants/public/**   - Listagem pública de restaurantes
GET  /api/menus/public/**         - Cardápios públicos
GET  /api/reviews/public/**       - Reviews públicos
```

### 🔐 **Rotas Protegidas** (Requer JWT)
```
GET/POST/PUT    /api/users/**       - Gestão de usuários
GET/POST/PUT    /api/restaurants/** - Gestão de restaurantes
GET/POST/PUT    /api/menus/**       - Gestão de cardápios
GET/POST/PUT    /api/orders/**      - Gestão de pedidos
GET/POST/PUT    /api/payments/**    - Processamento de pagamentos
GET/POST/PUT    /api/deliveries/**  - Rastreamento de entregas
GET/POST/PUT    /api/reviews/**     - Sistema de avaliações
```

### 🔧 **Rotas de Sistema**
```
GET /api/health    - Status do gateway
GET /api/info      - Informações do sistema
GET /api/routes    - Lista de rotas disponíveis
GET /actuator/**   - Endpoints de monitoramento
```

## ⚡ Funcionalidades

### 🛡️ **Segurança**
- **JWT Authentication**: Validação de tokens JWT
- **Security Headers**: Headers de segurança HTTP
- **Content Validation**: Detecção de conteúdo suspeito
- **CORS Configuration**: Configuração flexível de CORS

### 📊 **Rate Limiting**
- **Por IP**: Limite baseado no endereço IP
- **Por Rota**: Limites específicos por endpoint
- **Redis Backend**: Armazenamento distribuído de contadores
- **Configuração Flexível**: Limites ajustáveis por serviço

### 🔄 **Circuit Breaker**
- **Resilience4j**: Implementação robusta de circuit breaker
- **Fallbacks**: Respostas de fallback para serviços indisponíveis
- **Configuração por Serviço**: Thresholds específicos
- **Monitoramento**: Métricas de saúde dos serviços

### 📝 **Logging & Monitoramento**
- **Trace IDs**: Rastreamento de requisições
- **Structured Logging**: Logs estruturados em JSON
- **Prometheus Metrics**: Métricas para monitoramento
- **Health Checks**: Verificações de saúde

## 🔧 Configuração

### Variáveis de Ambiente
```bash
# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here

# Redis Configuration  
REDIS_HOST=localhost
REDIS_PORT=6380
REDIS_PASSWORD=your-redis-password

# Config Server
CONFIG_SERVER_URL=http://localhost:8888

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761
```

### Rate Limits por Serviço
```yaml
Auth Service:     10 req/min   (login/register)
User Service:     20 req/min   (perfil/dados)
Restaurant:       50 req/min   (busca pública)
Menu Service:     100 req/min  (visualização)
Order Service:    15 req/min   (pedidos)
Payment Service:  10 req/min   (pagamentos)
Delivery Service: 25 req/min   (rastreamento)
Review Service:   20 req/min   (avaliações)
```

## 🚀 Como Executar

### Pré-requisitos
1. **Java 21** instalado
2. **Redis** rodando na porta 6380
3. **Config Server** rodando na porta 8888
4. **Service Discovery** rodando na porta 8761

### Via Maven
```bash
cd api-gateway
mvn spring-boot:run
```

### Via Docker
```bash
docker-compose up api-gateway
```

### Verificar Status
```bash
curl http://localhost:8080/api/health
```

## 🔍 Monitoramento

### Health Check
```bash
curl http://localhost:8080/api/health
```

### Métricas Prometheus
```bash
curl http://localhost:8080/actuator/prometheus
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

### Circuit Breaker Status
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

## 🧪 Testes

### Executar Testes
```bash
mvn test
```

### Testar Autenticação
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"123456"}'

# Usar token
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Testar Rate Limiting
```bash
# Múltiplas requisições para testar limite
for i in {1..15}; do
  curl -X GET http://localhost:8080/api/restaurants/public/list
  sleep 1
done
```

## 📊 Logs e Debugging

### Estrutura de Logs
```json
{
  "timestamp": "2024-10-21T21:25:54.123",
  "level": "INFO",
  "traceId": "a1b2c3d4",
  "message": "🔍 [a1b2c3d4] GET /api/users/profile - Headers: {...}",
  "service": "api-gateway"
}
```

### Níveis de Log
- **DEBUG**: Detalhes de requisições e respostas
- **INFO**: Informações gerais de fluxo
- **WARN**: Rate limits e circuit breakers
- **ERROR**: Falhas de sistema

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. **Service Unavailable (503)**
```bash
# Verificar se os serviços estão registrados
curl http://localhost:8761/eureka/apps

# Verificar circuit breakers
curl http://localhost:8080/actuator/circuitbreakers
```

#### 2. **Rate Limit Exceeded (429)**
```bash
# Verificar contadores no Redis
redis-cli -p 6380
> KEYS "rate_limit:*"
> TTL "rate_limit:IP:PATH"
```

#### 3. **JWT Invalid (401)**
```bash
# Verificar configuração JWT
curl http://localhost:8080/api/info
```

#### 4. **CORS Issues**
```bash
# Testar preflight request
curl -X OPTIONS http://localhost:8080/api/users/profile \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization"
```

## 🎯 Próximos Passos

- [ ] Implementar cache de autenticação
- [ ] Adicionar rate limiting por usuário
- [ ] Implementar API versioning
- [ ] Adicionar compressão de resposta
- [ ] Implementar request/response transformation
- [ ] Adicionar WebSocket support

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verificar logs do gateway
2. Consultar health checks
3. Verificar métricas no Prometheus
4. Revisar configurações no Config Server