# Guia Prático - Conventional Commits para iFood Clone

## 🚀 Setup Inicial

### 1. Instalar Commitizen (Opcional)
```bash
pip install commitizen
```

### 2. Usar o Script Helper
```bash
./scripts/commit.sh <type> <scope> <description> [body]
```

## 📋 Fluxo de Trabalho Recomendado

### 1. **Trabalho Atual - Finalizar Infraestrutura**

```bash
# Commitar as correções do Config Server
git add config-server/src/main/resources/config/auth-service.yml
./scripts/commit.sh fix config-server "resolve environment variable support for Redis and PostgreSQL"

# Commitar a documentação da estratégia  
git add docs/COMMIT_STRATEGY.md scripts/commit.sh pyproject.toml
./scripts/commit.sh docs ci "add conventional commits strategy and automation tools"
```

### 2. **Próximos Commits por Service**

#### Config Server
```bash
# Adicionar configurações para novos services
git add config-server/src/main/resources/config/restaurant-service.yml
./scripts/commit.sh feat config-server "add configuration for restaurant-service"

git add config-server/src/main/resources/config/menu-service.yml  
./scripts/commit.sh feat config-server "add configuration for menu-service"
```

#### Restaurant Service
```bash
# Implementação básica
git add restaurant-service/src/main/java/com/ifoodclone/restaurant/
./scripts/commit.sh feat restaurant-service "implement basic restaurant entity and repository"

# API endpoints
git add restaurant-service/src/main/java/com/ifoodclone/restaurant/controller/
./scripts/commit.sh feat restaurant-service "add REST endpoints for restaurant management"

# Business logic
git add restaurant-service/src/main/java/com/ifoodclone/restaurant/service/
./scripts/commit.sh feat restaurant-service "add restaurant search and filtering logic"
```

#### Menu Service  
```bash
# Core functionality
git add menu-service/src/main/java/com/ifoodclone/menu/
./scripts/commit.sh feat menu-service "implement menu item management"

# Integration
git add menu-service/src/main/java/com/ifoodclone/menu/integration/
./scripts/commit.sh feat menu-service "add integration with restaurant-service"
```

#### Order Service
```bash
# Order lifecycle
git add order-service/src/main/java/com/ifoodclone/order/
./scripts/commit.sh feat order-service "implement order creation and lifecycle management"

# Payment integration
git add order-service/src/main/java/com/ifoodclone/order/payment/
./scripts/commit.sh feat order-service "integrate with payment-service for order processing"
```

### 3. **Commits Cross-Service**

```bash
# Quando uma mudança afeta múltiplos services
git add api-gateway/ auth-service/ user-service/
./scripts/commit.sh feat api-gateway,auth-service,user-service "implement unified authentication flow"

# Docker e infraestrutura
git add docker-compose.yml Dockerfile*
./scripts/commit.sh build docker "optimize container startup and networking configuration"
```

### 4. **Testing e Quality Assurance**

```bash
# Testes unitários
git add auth-service/src/test/
./scripts/commit.sh test auth-service "add unit tests for JWT token validation"

# Testes de integração  
git add auth-service/src/test/java/integration/
./scripts/commit.sh test auth-service "add integration tests for Redis and PostgreSQL connectivity"

# Performance tests
git add restaurant-service/src/test/java/performance/
./scripts/commit.sh perf restaurant-service "add load testing for search endpoints"
```

### 5. **Bug Fixes**

```bash
# Correções específicas
git add auth-service/src/main/java/com/ifoodclone/auth/config/
./scripts/commit.sh fix auth-service "resolve Redis connection timeout in Docker environment"

# Security fixes
git add payment-service/src/main/java/com/ifoodclone/payment/security/
./scripts/commit.sh security payment-service "implement PCI DSS compliant data encryption"
```

### 6. **Documentation**

```bash
# API Documentation
git add restaurant-service/src/main/resources/static/swagger/
./scripts/commit.sh docs restaurant-service "add OpenAPI 3.0 documentation for all endpoints"

# README updates
git add README.md
./scripts/commit.sh docs project "update setup instructions and architecture documentation"
```

## 🎯 Estratégia de Branches

### Feature Development
```bash
# Criar branch para nova feature
git checkout -b feature/restaurant-service-search-optimization

# Trabalhar na feature com commits atômicos
./scripts/commit.sh feat restaurant-service "add geospatial indexing for location search"
./scripts/commit.sh perf restaurant-service "optimize database queries with proper indexing" 
./scripts/commit.sh test restaurant-service "add performance tests for location search"

# Merge para develop
git checkout develop
git merge feature/restaurant-service-search-optimization --no-ff
```

### Hotfixes
```bash
# Criar branch de hotfix
git checkout -b hotfix/auth-service-security-vulnerability

# Fix crítico
./scripts/commit.sh security auth-service "patch JWT token validation vulnerability"
./scripts/commit.sh test auth-service "add regression tests for JWT security fix"

# Merge para main e develop
git checkout main
git merge hotfix/auth-service-security-vulnerability
git checkout develop  
git merge hotfix/auth-service-security-vulnerability
```

## 📊 Tracking e Analytics

### Commit Analytics
```bash
# Commits por service
git log --oneline | grep -E "\((auth-service|user-service|restaurant-service)\):" | wc -l

# Commits por tipo
git log --oneline | grep -E "^feat\(" | wc -l
git log --oneline | grep -E "^fix\(" | wc -l

# Commits recentes por scope
git log --oneline --since="1 week ago" | grep "(restaurant-service)"
```

### Release Notes Generation
```bash
# Gerar changelog automático
conventional-changelog -p angular -i CHANGELOG.md -s

# Commits desde última release
git log v1.0.0..HEAD --oneline --pretty=format:"%s"
```

## 🔧 Automação e Validação

### Pre-commit Hook
```bash
# .git/hooks/commit-msg
#!/bin/sh
# Validate commit message format
commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|security)\([a-z-]+\): .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "Invalid commit message format!" >&2
    echo "Use: <type>(<scope>): <description>" >&2
    exit 1
fi
```

### GitHub Actions
```yaml
# .github/workflows/commit-validation.yml
name: Validate Commits
on: [push, pull_request]
jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0
      - name: Validate commit messages
        run: |
          git log --pretty=format:"%s" origin/main..HEAD | while read msg; do
            if ! echo "$msg" | grep -qE '^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|security)\([a-z-]+\): .{1,50}'; then
              echo "Invalid commit: $msg"
              exit 1
            fi
          done
```

## 📈 Métricas e KPIs

### Qualidade dos Commits
- **Atomicidade**: Um commit = uma mudança lógica
- **Mensagens Descritivas**: Commits auto-documentados
- **Rastreabilidade**: Fácil identificação de mudanças por service
- **Reversibilidade**: Commits que podem ser revertidos independentemente

### Produtividade
- **Commits por Sprint**: Tracking de velocity por service
- **Bug Fix Ratio**: Proporção de fixes vs features
- **Documentation Coverage**: Commits de docs vs código

Essa estratégia garante **rastreabilidade**, **manutenibilidade** e **colaboração eficiente** em um ambiente de microserviços!