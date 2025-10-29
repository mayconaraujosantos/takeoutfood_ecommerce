# Estratégia de Commits - iFood Clone Microservices

## Conventional Commits Structure

### Core Infrastructure
```bash
# Config Server
feat(config-server): add environment variable support for Redis configuration
fix(config-server): resolve configuration loading for auth-service
chore(config-server): update Spring Cloud Config to 2023.0.3

# Service Discovery
feat(service-discovery): implement Eureka server with Docker networking
fix(service-discovery): resolve service registration timeout issues
perf(service-discovery): optimize heartbeat intervals

# API Gateway
feat(api-gateway): add Redis session management
fix(api-gateway): resolve connection refused errors with Redis
refactor(api-gateway): modernize spring.data.redis configuration
```

### Business Services
```bash
# Auth Service
feat(auth-service): implement JWT token generation and validation
feat(auth-service): add password reset functionality
fix(auth-service): resolve PostgreSQL connection with environment variables
test(auth-service): add integration tests for authentication flow

# User Service
feat(user-service): implement user registration and profile management
feat(user-service): add user preferences and dietary restrictions
fix(user-service): resolve email validation edge cases

# Restaurant Service
feat(restaurant-service): implement restaurant registration and management
feat(restaurant-service): add restaurant search by location and cuisine
feat(restaurant-service): add operating hours and availability management
perf(restaurant-service): optimize geolocation queries with spatial indexing

# Menu Service
feat(menu-service): implement menu item creation and categorization
feat(menu-service): add menu item availability and pricing
feat(menu-service): implement menu versioning for restaurants

# Order Service
feat(order-service): implement order creation and lifecycle management
feat(order-service): add order status tracking and notifications
feat(order-service): implement order cancellation and refund logic
perf(order-service): optimize order processing with async messaging

# Payment Service
feat(payment-service): integrate Stripe payment processing
feat(payment-service): implement payment method management
feat(payment-service): add payment retry mechanism and failure handling
security(payment-service): implement PCI DSS compliant data handling

# Delivery Service
feat(delivery-service): implement delivery assignment algorithm
feat(delivery-service): add real-time delivery tracking
feat(delivery-service): implement delivery route optimization

# Notification Service
feat(notification-service): implement email notifications with templates
feat(notification-service): add push notifications for mobile apps
feat(notification-service): implement SMS notifications for order updates

# Review Service
feat(review-service): implement restaurant and delivery reviews
feat(review-service): add review moderation and reporting
feat(review-service): implement review analytics and insights
```

### Infrastructure and DevOps
```bash
# Docker & Orchestration
build(docker): add multi-stage Dockerfile for all microservices
build(docker): implement Docker Compose with proper networking
ci(docker): add Docker image build automation in GitHub Actions

# Database
feat(database): implement PostgreSQL schemas for all services
feat(database): add MongoDB collections for menu and reviews
feat(database): implement Redis caching layer configuration

# Monitoring & Observability
feat(monitoring): add Prometheus metrics for all services
feat(monitoring): implement distributed tracing with Jaeger
feat(monitoring): add Grafana dashboards for service monitoring
feat(logging): implement structured logging with Logback and Loki

# Testing
test(integration): add service-to-service integration tests
test(e2e): implement end-to-end order flow testing
test(performance): add load testing for high-traffic scenarios

# Security
security(auth): implement OAuth2 with JWT tokens
security(api-gateway): add rate limiting and request validation
security(database): implement connection encryption and secrets management
```

## Commit Message Examples by Feature

### Single Service Changes
```bash
feat(restaurant-service): add cuisine type filtering and search

Implement advanced restaurant filtering by:
- Cuisine type (Italian, Chinese, Mexican, etc.)
- Price range filtering  
- Rating threshold filtering
- Distance-based sorting

Closes #123
```

### Cross-Service Changes
```bash
feat(order-service,payment-service): implement order payment integration

Add payment processing workflow:
- Order service creates payment intent
- Payment service processes transaction
- Order service receives payment confirmation
- Async notification to user and restaurant

Co-authored-by: TeamMember <team@example.com>
Breaking Change: Payment API v1 deprecated
```

### Bug Fixes
```bash
fix(auth-service): resolve Redis connection timeout in Docker environment

- Update Redis configuration to use container networking
- Add connection pooling and retry mechanism  
- Fix environment variable precedence in Config Server
- Add health check validation for Redis connectivity

Fixes #456
```

### Documentation
```bash
docs(api-gateway): add API documentation for routing rules

- Document service discovery integration
- Add examples for rate limiting configuration
- Include troubleshooting guide for common issues
- Add sequence diagrams for request flow
```

## Branching Strategy Recommendation

### Git Flow for Microservices
```bash
main                    # Production-ready code
├── develop            # Integration branch
├── feature/auth-jwt   # Feature branches per service/feature
├── feature/order-flow # Cross-service features
├── hotfix/auth-redis  # Critical production fixes
└── release/v1.2.0     # Release preparation
```

### Branch Naming Convention
```bash
feature/SERVICE-DESCRIPTION
├── feature/auth-service-jwt-implementation
├── feature/restaurant-service-search
├── feature/order-service-async-processing
├── feature/cross-service-monitoring
└── feature/api-gateway-rate-limiting

bugfix/SERVICE-ISSUE
├── bugfix/auth-service-redis-connection
├── bugfix/order-service-payment-timeout
└── bugfix/api-gateway-routing-rules

hotfix/CRITICAL-ISSUE
├── hotfix/payment-service-security-vulnerability
└── hotfix/database-connection-leak
```

## Atomic Commit Strategy

### Rule: One Logical Change = One Commit
```bash
# ✅ Good - Atomic commits
git commit -m "feat(auth-service): add JWT token generation"
git commit -m "feat(auth-service): add JWT token validation"  
git commit -m "test(auth-service): add JWT integration tests"

# ❌ Bad - Mixed concerns
git commit -m "feat(auth-service): add JWT and fix Redis and update docs"
```

### Service Independence
```bash
# Each service should be committable independently
git commit -m "feat(restaurant-service): implement cuisine filtering"
git commit -m "feat(menu-service): add dietary restriction support"
git commit -m "feat(order-service): integrate with updated menu service"
```

## Release Strategy

### Semantic Versioning per Service
```bash
# Tag format: SERVICE-vMAJOR.MINOR.PATCH
git tag auth-service-v1.0.0
git tag restaurant-service-v1.2.1  
git tag api-gateway-v2.0.0

# Platform release (all services)
git tag platform-v1.0.0
```

### Release Notes Generation
```bash
# Auto-generate from conventional commits
conventional-changelog -p angular -i CHANGELOG.md -s
```

## Tools and Automation

### Commit Message Validation
```bash
# .gitmessage template
# <type>(<scope>): <subject>
# 
# <body>
# 
# <footer>
```

### Pre-commit Hooks
```bash
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/commitizen-tools/commitizen
    rev: v2.40.0
    hooks:
      - id: commitizen
        stages: [commit-msg]
```

### GitHub Actions for Validation
```yaml
# .github/workflows/commit-validation.yml
name: Validate Commits
on: [push, pull_request]
jobs:
  validate-commits:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Validate commit messages
        uses: wagoid/commitlint-github-action@v5
```