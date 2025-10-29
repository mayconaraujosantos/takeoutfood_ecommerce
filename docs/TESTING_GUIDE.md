# 🧪 Guia de Teste - Sistema de Autenticação iFood Clone

## 🚀 **Como Testar a Implementação Completa**

### 📋 **Pré-requisitos**
- Docker e Docker Compose instalados
- Maven Wrapper configurado (`./mvnw`)
- Portas disponíveis: 8080-8089, 5433, 6380, 27018, 9092

## 🔧 **1. Setup do Ambiente**

### **Iniciar Infraestrutura**
```bash
cd /home/mayconaraujo/Documents/ifood_clone

# Iniciar apenas a infraestrutura (bancos, kafka, etc.)
docker-compose up -d postgres mongodb redis kafka zookeeper

# Verificar se os serviços estão rodando
docker-compose ps
```

### **Build dos Serviços (com Maven Wrapper)**
```bash
# Build de todos os serviços
./mvnw clean package -DskipTests

# Ou build individual
cd config-server && ../mvnw clean package -DskipTests && cd ..
cd service-discovery && ../mvnw clean package -DskipTests && cd ..
cd api-gateway && ../mvnw clean package -DskipTests && cd ..
cd auth-service && ../mvnw clean package -DskipTests && cd ..
cd user-service && ../mvnw clean package -DskipTests && cd ..
```

### **Iniciar Serviços Core**
```bash
# Iniciar serviços de infraestrutura primeiro
docker-compose up -d config-server service-discovery

# Aguardar 30 segundos para os serviços subirem
sleep 30

# Iniciar API Gateway e Auth Service
docker-compose up -d api-gateway auth-service

# Aguardar mais 20 segundos
sleep 20

# Iniciar User Service
docker-compose up -d user-service
```

## 🧪 **2. Testes de Autenticação**

### **Verificar Status dos Serviços**
```bash
# Health checks
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Service Discovery  
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # User Service
```

### **Teste 1: Registro de Usuário**
```bash
# Registrar novo usuário
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com", 
    "password": "MinhaSenh@123",
    "role": "CUSTOMER"
  }'

# Resposta esperada: 201 Created com dados do usuário
```

### **Teste 2: Login de Usuário**
```bash
# Login do usuário
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "MinhaSenh@123"
  }'

# Resposta esperada: 200 OK com accessToken e refreshToken
# Salve o accessToken para os próximos testes
```

### **Teste 3: Acesso a Endpoint Protegido**
```bash
# Substitua YOUR_ACCESS_TOKEN pelo token recebido no login
export ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Acessar perfil do usuário
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Resposta esperada: 200 OK com dados do perfil
```

### **Teste 4: Refresh Token**
```bash
# Substitua YOUR_REFRESH_TOKEN pelo refreshToken recebido no login
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'

# Resposta esperada: 200 OK com novo accessToken
```

### **Teste 5: Tentativa de Acesso sem Token**
```bash
# Tentar acessar endpoint protegido sem token
curl -X GET http://localhost:8080/api/users/profile

# Resposta esperada: 401 Unauthorized
```

### **Teste 6: Tentativa com Token Inválido**
```bash
# Tentar com token inválido
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer token_invalido"

# Resposta esperada: 401 Unauthorized
```

## 🔒 **3. Testes de Autorização**

### **Teste 7: Endpoint Admin (deve falhar)**
```bash
# Tentar acessar endpoint admin com usuário comum
curl -X GET http://localhost:8080/api/users/admin/all \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Resposta esperada: 403 Forbidden
```

### **Teste 8: Criar Usuário Admin e Testar**
```bash
# Registrar usuário admin (se permitido)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@example.com",
    "password": "AdminPass@123", 
    "role": "ADMIN"
  }'

# Login do admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass@123"
  }'

# Salvar token admin
export ADMIN_TOKEN="..."

# Testar endpoint admin
curl -X GET http://localhost:8080/api/users/admin/all \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Resposta esperada: 200 OK com acesso liberado
```

## 📊 **4. Verificação de Headers**

### **Teste 9: Verificar Headers Injetados**
```bash
# Usar um serviço que retorne os headers recebidos
# (Implementar endpoint de debug no user-service se necessário)

curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -v

# Verificar se o Gateway está injetando os headers corretos
```

## 🔍 **5. Debugging e Logs**

### **Verificar Logs dos Serviços**
```bash
# Logs do API Gateway
docker-compose logs -f api-gateway

# Logs do Auth Service  
docker-compose logs -f auth-service

# Logs do User Service
docker-compose logs -f user-service

# Logs de todos os serviços
docker-compose logs -f
```

### **Verificar Descoberta de Serviços**
```bash
# Acessar Eureka Dashboard
open http://localhost:8761

# Verificar se todos os serviços estão registrados
```

## ⚡ **6. Testes de Performance**

### **Teste de Carga Básico**
```bash
# Instalar Apache Bench (se não tiver)
# Ubuntu: sudo apt-get install apache2-utils
# macOS: brew install apache2

# Teste de carga no endpoint de login
ab -n 100 -c 10 -p login_data.json -T application/json http://localhost:8080/auth/login

# Arquivo login_data.json:
echo '{"email":"joao@example.com","password":"MinhaSenh@123"}' > login_data.json
```

## 🛠️ **7. Troubleshooting**

### **Problemas Comuns**

1. **Serviço não sobe:**
```bash
# Verificar logs
docker-compose logs nome-do-servico

# Verificar se as portas estão disponíveis
netstat -tulpn | grep :8080
```

2. **Erro de conexão com banco:**
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps postgres

# Testar conexão
docker exec -it ifood_clone_postgres_1 psql -U ifood_user -d ifood_db
```

3. **JWT Token inválido:**
```bash
# Verificar se o secret está consistente entre Gateway e Auth Service
# Verificar logs do API Gateway para erros de validação
```

4. **Service Discovery não funciona:**
```bash
# Verificar se o Eureka está acessível
curl http://localhost:8761/eureka/apps

# Verificar configuração de rede do Docker
docker network ls
```

## ✅ **8. Checklist de Validação**

- [ ] Config Server rodando na porta 8888
- [ ] Service Discovery rodando na porta 8761
- [ ] API Gateway rodando na porta 8080
- [ ] Auth Service rodando na porta 8081  
- [ ] User Service rodando na porta 8082
- [ ] PostgreSQL acessível na porta 5433
- [ ] Redis acessível na porta 6380
- [ ] Registro de usuário funcionando
- [ ] Login retornando access e refresh tokens
- [ ] Endpoints protegidos bloqueando acesso sem token
- [ ] Endpoints protegidos liberando acesso com token válido
- [ ] Refresh token funcionando
- [ ] Autorização por roles funcionando
- [ ] Headers sendo injetados corretamente
- [ ] Logs mostrando atividade esperada

## 🎉 **Resultado Esperado**

Se todos os testes passarem, você terá:
- ✅ Sistema de autenticação JWT completo
- ✅ API Gateway com validação automática
- ✅ Business services com autorização
- ✅ Refresh tokens funcionais
- ✅ Arquitetura de microsserviços segura

**🚀 Sua implementação está funcionando perfeitamente!**