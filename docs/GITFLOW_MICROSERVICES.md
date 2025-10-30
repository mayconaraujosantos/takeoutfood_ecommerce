# 🌊 Git Flow para Microserviços - iFood Clone

## 🎯 Objetivo

Implementar Git Flow adaptado para desenvolvimento de microserviços, onde cada serviço pode ser desenvolvido independentemente, mas mantendo **serviços core** (config-server, service-discovery, api-gateway) disponíveis para todos.

## 📐 Estrutura de Branches

```
main (produção)
├── develop (integração)
├── feature/restaurant-service
├── feature/menu-service  
├── feature/order-service
├── feature/payment-service
├── feature/delivery-service
├── feature/notification-service
├── feature/review-service
├── release/v1.0.0
└── hotfix/critical-fix
```

## 🏗️ Arquitetura de Dependências

### Core Services (Base para todos)
- **config-server** - Configuração centralizada
- **service-discovery** - Registro de serviços (Eureka)
- **api-gateway** - Gateway de API

### Business Services (Desenvolvimento independente)
- **restaurant-service** - Gestão de restaurantes
- **menu-service** - Cardápios e produtos
- **order-service** - Processamento de pedidos
- **payment-service** - Processamento de pagamentos
- **delivery-service** - Gestão de entregas
- **notification-service** - Sistema de notificações
- **review-service** - Avaliações e reviews
- **user-service** - Gestão de usuários

## 🛠️ Como Usar

### 1. Inicializar Git Flow
```bash
# Primeira vez (criar branch develop)
./scripts/gitflow-microservices.sh init
```

### 2. Desenvolver um Microserviço
```bash
# Criar feature branch para restaurant-service
./scripts/gitflow-microservices.sh feature start restaurant-service

# Trabalhar no serviço...
# Os core services estão automaticamente disponíveis

# Fazer commits usando nossa estratégia
./scripts/commit.sh feat restaurant-service "add restaurant registration API"
./scripts/commit.sh feat restaurant-service "implement restaurant search"

# Finalizar feature (merge para develop)
./scripts/gitflow-microservices.sh feature finish restaurant-service
```

### 3. Sincronizar Core Services
```bash
# Quando core services são atualizados, sincronizar com todas features
./scripts/gitflow-microservices.sh core-sync
```

### 4. Criar Release
```bash
# Iniciar release
./scripts/gitflow-microservices.sh release start 1.0.0

# Testar, ajustar e finalizar
./scripts/gitflow-microservices.sh release finish 1.0.0
```

### 5. Verificar Status
```bash
# Ver status do Git Flow
./scripts/gitflow-microservices.sh status

# Listar features ativas
./scripts/gitflow-microservices.sh feature list
```

## 🔄 Fluxo de Trabalho Recomendado

### Cenário 1: Desenvolvimento Paralelo
```bash
# Time 1: Restaurant Service
./scripts/gitflow-microservices.sh feature start restaurant-service
# Desenvolver APIs de restaurante...

# Time 2: Menu Service  
./scripts/gitflow-microservices.sh feature start menu-service
# Desenvolver APIs de cardápio...

# Time 3: Order Service
./scripts/gitflow-microservices.sh feature start order-service
# Desenvolver APIs de pedidos...
```

### Cenário 2: Atualização de Core Service
```bash
# Atualizar config-server no develop
git checkout develop
./scripts/commit.sh feat config-server "add new database configuration"

# Sincronizar para todas features
./scripts/gitflow-microservices.sh core-sync
```

### Cenário 3: Integração e Release
```bash
# Finalizar features prontas
./scripts/gitflow-microservices.sh feature finish restaurant-service
./scripts/gitflow-microservices.sh feature finish menu-service

# Criar release
./scripts/gitflow-microservices.sh release start 1.0.0

# Deploy, teste, ajustes finais...

# Finalizar release
./scripts/gitflow-microservices.sh release finish 1.0.0
```

## 💡 Vantagens desta Abordagem

### ✅ **Desenvolvimento Independente**
- Cada time trabalha em seu microserviço
- Não há conflitos entre services
- Desenvolvimento paralelo eficiente

### ✅ **Core Services Sempre Disponíveis**
- Config Server funciona em todas as branches
- Service Discovery mantém registro
- API Gateway sempre operacional

### ✅ **Integração Controlada**
- Features são integradas quando prontas
- Testes de integração no develop
- Releases coordenadas

### ✅ **Flexibilidade de Deploy**
- Deploy independente de serviços
- Rollback granular
- Versionamento por serviço

## 🔍 Troubleshooting

### Problema: Conflitos de Merge
```bash
# Resolver conflitos manualmente e continuar
git add .
git commit -m "resolve: merge conflicts in feature integration"
```

### Problema: Feature Branch Desatualizada
```bash
# Atualizar feature com develop
git checkout feature/service-name
git merge develop
```

### Problema: Core Service Quebrado
```bash
# Fix rápido no develop
git checkout develop
./scripts/commit.sh fix config-server "resolve configuration issue"

# Sincronizar para features
./scripts/gitflow-microservices.sh core-sync
```

## 📋 Checklist de Desenvolvimento

### Antes de Iniciar Feature
- [ ] `./scripts/gitflow-microservices.sh init` executado
- [ ] Core services funcionando em develop
- [ ] Definição clara do escopo do microserviço

### Durante Desenvolvimento
- [ ] Usar `./scripts/commit.sh` para commits organizados
- [ ] Testar integração com core services
- [ ] Documentar APIs e endpoints
- [ ] Executar testes unitários e integração

### Antes de Finalizar Feature
- [ ] Core services sincronizados
- [ ] Todos os testes passando
- [ ] Documentação atualizada
- [ ] Code review realizado

### Antes de Release
- [ ] Todas as features necessárias finalizadas
- [ ] Testes de integração completos
- [ ] Performance verificada
- [ ] Documentação de deploy atualizada

## 🎖️ Melhores Práticas

1. **Commits Atômicos**: Use sempre `./scripts/commit.sh`
2. **Sync Regular**: Execute `core-sync` regularmente
3. **Testes Contínuos**: Teste integração durante desenvolvimento
4. **Documentação**: Mantenha docs atualizadas em cada feature
5. **Code Review**: Review antes de finish feature

## 🚀 Exemplo Prático

```bash
# 1. Inicializar (uma vez)
./scripts/gitflow-microservices.sh init

# 2. Começar desenvolvimento do Restaurant Service
./scripts/gitflow-microservices.sh feature start restaurant-service

# 3. Desenvolver (commits organizados)
./scripts/commit.sh feat restaurant-service "add Restaurant entity and repository"
./scripts/commit.sh feat restaurant-service "implement restaurant registration API"
./scripts/commit.sh test restaurant-service "add unit tests for restaurant service"

# 4. Sincronizar core services (se houver updates)
./scripts/gitflow-microservices.sh core-sync

# 5. Finalizar quando pronto
./scripts/gitflow-microservices.sh feature finish restaurant-service

# 6. Status e próximos passos
./scripts/gitflow-microservices.sh status
./scripts/gitflow-microservices.sh feature list
```

Esta abordagem garante **desenvolvimento senior** com **isolamento**, **integração controlada** e **dependências gerenciadas** automaticamente! 🎯