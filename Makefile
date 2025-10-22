# ==========================================
# iFood Clone Microservices Management
# ==========================================

# Colors for output
RED=\033[0;31m
GREEN=\033[0;32m
YELLOW=\033[0;33m
BLUE=\033[0;34m
PURPLE=\033[0;35m
CYAN=\033[0;36m
WHITE=\033[0;37m
NC=\033[0m # No Color

# Configuration
JAVA_OPTS=-Xms512m -Xmx1024m -Dspring.profiles.active=dev
MAVEN_OPTS=-DskipTests
DOCKER_COMPOSE_INFRA=docker-compose.infrastructure.yml
DOCKER_COMPOSE_SERVICES=docker-compose.yml

# Services
INFRASTRUCTURE_SERVICES=postgres redis mongodb kafka zookeeper
CORE_SERVICES=config-server service-discovery
API_SERVICES=api-gateway
BUSINESS_SERVICES=auth-service user-service restaurant-service menu-service order-service payment-service delivery-service notification-service review-service

ALL_SERVICES=$(CORE_SERVICES) $(API_SERVICES) $(BUSINESS_SERVICES)

# ==========================================
# Help and Information
# ==========================================

.PHONY: help
help: ## Show this help message
	@echo "$(CYAN)🚀 iFood Clone Microservices Management$(NC)"
	@echo ""
	@echo "$(GREEN)Infrastructure Management:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /infrastructure|infra|postgres|redis|kafka|mongo/ { printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)Service Management:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && !/infrastructure|infra|postgres|redis|kafka|mongo|help|clean/ { printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)Development Tools:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /clean|build|test|logs/ { printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)Available Services:$(NC) $(ALL_SERVICES)"
	@echo "$(BLUE)Infrastructure:$(NC) $(INFRASTRUCTURE_SERVICES)"

# ==========================================
# Infrastructure Management
# ==========================================

.PHONY: infrastructure-up
infrastructure-up: ## Start all infrastructure services (PostgreSQL, Redis, Kafka, MongoDB)
	@echo "$(GREEN)🚀 Starting infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) up -d
	@echo "$(GREEN)✅ Infrastructure services started!$(NC)"
	@make infrastructure-status

.PHONY: infrastructure-down
infrastructure-down: ## Stop all infrastructure services
	@echo "$(RED)🛑 Stopping infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) down
	@echo "$(RED)✅ Infrastructure services stopped!$(NC)"

.PHONY: infrastructure-restart
infrastructure-restart: ## Restart all infrastructure services
	@echo "$(YELLOW)🔄 Restarting infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) restart
	@echo "$(GREEN)✅ Infrastructure services restarted!$(NC)"

.PHONY: infrastructure-status
infrastructure-status: ## Check status of infrastructure services
	@echo "$(CYAN)📊 Infrastructure Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) ps

.PHONY: infrastructure-logs
infrastructure-logs: ## Show logs from infrastructure services
	@echo "$(CYAN)📋 Infrastructure Logs:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) logs -f --tail=100

# ==========================================
# Build Management
# ==========================================

.PHONY: build-all
build-all: ## Build all microservices
	@echo "$(GREEN)🔨 Building all microservices...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "$(BLUE)Building $$service...$(NC)"; \
		cd $$service && mvn clean package $(MAVEN_OPTS) && cd ..; \
	done
	@echo "$(GREEN)✅ All services built successfully!$(NC)"

.PHONY: build-%
build-%: ## Build specific service (e.g., make build-auth-service)
	@echo "$(BLUE)🔨 Building $*...$(NC)"
	@cd $* && mvn clean package $(MAVEN_OPTS)
	@echo "$(GREEN)✅ $* built successfully!$(NC)"

# ==========================================
# Individual Service Management
# ==========================================

.PHONY: start-config-server
start-config-server: ## Start Config Server
	@echo "$(GREEN)🚀 Starting Config Server...$(NC)"
	@cd config-server && java $(JAVA_OPTS) -jar target/config-server-1.0.0.jar &
	@echo "$(GREEN)✅ Config Server started on port 8888$(NC)"

.PHONY: start-service-discovery
start-service-discovery: ## Start Service Discovery (Eureka)
	@echo "$(GREEN)🚀 Starting Service Discovery...$(NC)"
	@cd service-discovery && java $(JAVA_OPTS) -jar target/service-discovery-1.0.0.jar &
	@echo "$(GREEN)✅ Service Discovery started on port 8761$(NC)"

.PHONY: start-api-gateway
start-api-gateway: ## Start API Gateway
	@echo "$(GREEN)🚀 Starting API Gateway...$(NC)"
	@cd api-gateway && java $(JAVA_OPTS) -jar target/api-gateway-1.0.0.jar &
	@echo "$(GREEN)✅ API Gateway started on port 8080$(NC)"

.PHONY: start-auth-service
start-auth-service: ## Start Authentication Service
	@echo "$(GREEN)🚀 Starting Auth Service...$(NC)"
	@cd auth-service && java $(JAVA_OPTS) -jar target/auth-service-1.0.0.jar &
	@echo "$(GREEN)✅ Auth Service started on port 8081$(NC)"

.PHONY: start-user-service
start-user-service: ## Start User Service
	@echo "$(GREEN)🚀 Starting User Service...$(NC)"
	@cd user-service && java $(JAVA_OPTS) -jar target/user-service-1.0.0.jar &
	@echo "$(GREEN)✅ User Service started on port 8082$(NC)"

.PHONY: start-restaurant-service
start-restaurant-service: ## Start Restaurant Service
	@echo "$(GREEN)🚀 Starting Restaurant Service...$(NC)"
	@cd restaurant-service && java $(JAVA_OPTS) -jar target/restaurant-service-1.0.0.jar &
	@echo "$(GREEN)✅ Restaurant Service started on port 8083$(NC)"

.PHONY: start-menu-service
start-menu-service: ## Start Menu Service
	@echo "$(GREEN)🚀 Starting Menu Service...$(NC)"
	@cd menu-service && java $(JAVA_OPTS) -jar target/menu-service-1.0.0.jar &
	@echo "$(GREEN)✅ Menu Service started on port 8084$(NC)"

.PHONY: start-order-service
start-order-service: ## Start Order Service
	@echo "$(GREEN)🚀 Starting Order Service...$(NC)"
	@cd order-service && java $(JAVA_OPTS) -jar target/order-service-1.0.0.jar &
	@echo "$(GREEN)✅ Order Service started on port 8085$(NC)"

.PHONY: start-payment-service
start-payment-service: ## Start Payment Service
	@echo "$(GREEN)🚀 Starting Payment Service...$(NC)"
	@cd payment-service && java $(JAVA_OPTS) -jar target/payment-service-1.0.0.jar &
	@echo "$(GREEN)✅ Payment Service started on port 8086$(NC)"

.PHONY: start-delivery-service
start-delivery-service: ## Start Delivery Service
	@echo "$(GREEN)🚀 Starting Delivery Service...$(NC)"
	@cd delivery-service && java $(JAVA_OPTS) -jar target/delivery-service-1.0.0.jar &
	@echo "$(GREEN)✅ Delivery Service started on port 8088$(NC)"

.PHONY: start-notification-service
start-notification-service: ## Start Notification Service
	@echo "$(GREEN)🚀 Starting Notification Service...$(NC)"
	@cd notification-service && java $(JAVA_OPTS) -jar target/notification-service-1.0.0.jar &
	@echo "$(GREEN)✅ Notification Service started on port 8087$(NC)"

.PHONY: start-review-service
start-review-service: ## Start Review Service
	@echo "$(GREEN)🚀 Starting Review Service...$(NC)"
	@cd review-service && java $(JAVA_OPTS) -jar target/review-service-1.0.0.jar &
	@echo "$(GREEN)✅ Review Service started on port 8089$(NC)"

# ==========================================
# Orchestrated Service Management
# ==========================================

.PHONY: start-core
start-core: ## Start core services (Config Server + Service Discovery)
	@echo "$(PURPLE)🚀 Starting core services...$(NC)"
	@make start-config-server
	@sleep 10
	@make start-service-discovery
	@sleep 15
	@echo "$(GREEN)✅ Core services started!$(NC)"

.PHONY: start-api
start-api: ## Start API layer (Gateway + Auth)
	@echo "$(PURPLE)🚀 Starting API layer...$(NC)"
	@make start-api-gateway
	@sleep 5
	@make start-auth-service
	@sleep 10
	@echo "$(GREEN)✅ API layer started!$(NC)"

.PHONY: start-business
start-business: ## Start business services
	@echo "$(PURPLE)🚀 Starting business services...$(NC)"
	@make start-user-service &
	@sleep 2
	@make start-restaurant-service &
	@sleep 2
	@make start-menu-service &
	@sleep 2
	@make start-order-service &
	@sleep 2
	@make start-payment-service &
	@sleep 2
	@make start-delivery-service &
	@sleep 2
	@make start-notification-service &
	@sleep 2
	@make start-review-service &
	@wait
	@echo "$(GREEN)✅ Business services started!$(NC)"

.PHONY: start-all
start-all: infrastructure-up ## Start all services in correct order
	@echo "$(PURPLE)🚀 Starting complete iFood Clone system...$(NC)"
	@echo "$(YELLOW)⏳ Waiting for infrastructure to be ready...$(NC)"
	@sleep 20
	@make start-core
	@echo "$(YELLOW)⏳ Waiting for core services...$(NC)"
	@sleep 15
	@make start-api
	@echo "$(YELLOW)⏳ Waiting for API layer...$(NC)"
	@sleep 10
	@make start-business
	@echo "$(GREEN)🎉 All services started successfully!$(NC)"
	@make status

# ==========================================
# Service Control
# ==========================================

.PHONY: stop-all
stop-all: ## Stop all running services
	@echo "$(RED)🛑 Stopping all services...$(NC)"
	@pkill -f "java.*jar" || echo "No Java services running"
	@make infrastructure-down
	@echo "$(RED)✅ All services stopped!$(NC)"

.PHONY: restart-all
restart-all: stop-all start-all ## Restart all services

.PHONY: status
status: ## Show status of all services
	@echo "$(CYAN)📊 Service Status:$(NC)"
	@echo ""
	@echo "$(BLUE)Infrastructure:$(NC)"
	@make infrastructure-status
	@echo ""
	@echo "$(BLUE)Java Services:$(NC)"
	@ps aux | grep -E "java.*jar" | grep -v grep || echo "No Java services running"
	@echo ""
	@echo "$(BLUE)Port Status:$(NC)"
	@echo "Config Server (8888): $$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8888/actuator/health 2>/dev/null || echo "DOWN")"
	@echo "Eureka (8761): $$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761 2>/dev/null || echo "DOWN")"
	@echo "API Gateway (8080): $$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "DOWN")"
	@echo "Auth Service (8081): $$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health 2>/dev/null || echo "DOWN")"

# ==========================================
# Development Tools
# ==========================================

.PHONY: logs-all
logs-all: ## Show logs from all services
	@echo "$(CYAN)📋 All Service Logs:$(NC)"
	@tail -f logs/*.log 2>/dev/null || echo "No log files found"

.PHONY: clean-all
clean-all: ## Clean all build artifacts
	@echo "$(YELLOW)🧹 Cleaning all build artifacts...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "Cleaning $$service..."; \
		cd $$service && mvn clean && cd ..; \
	done
	@echo "$(GREEN)✅ All artifacts cleaned!$(NC)"

.PHONY: test-all
test-all: ## Run tests for all services
	@echo "$(BLUE)🧪 Running all tests...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "Testing $$service..."; \
		cd $$service && mvn test && cd ..; \
	done
	@echo "$(GREEN)✅ All tests completed!$(NC)"

# ==========================================
# Quick Development Commands
# ==========================================

.PHONY: dev
dev: infrastructure-up start-core start-auth-service ## Quick development setup (infra + core + auth)
	@echo "$(GREEN)🎯 Development environment ready!$(NC)"
	@echo "$(CYAN)Access points:$(NC)"
	@echo "  - Eureka Dashboard: http://localhost:8761"
	@echo "  - Config Server: http://localhost:8888"
	@echo "  - Auth Service: http://localhost:8081"
	@echo "  - Swagger UI: http://localhost:8081/swagger-ui.html"

.PHONY: quick-start
quick-start: ## Quick start with essential services only
	@echo "$(YELLOW)⚡ Quick starting essential services...$(NC)"
	@make infrastructure-up
	@sleep 15
	@make start-config-server
	@sleep 10
	@make start-service-discovery
	@sleep 10
	@make start-auth-service
	@echo "$(GREEN)⚡ Quick start completed!$(NC)"

# ==========================================
# API Testing
# ==========================================

.PHONY: health-check
health-check: ## Check health of all services
	@echo "$(CYAN)🏥 Health Check:$(NC)"
	@services="config-server:8888 service-discovery:8761 api-gateway:8080 auth-service:8081"; \
	for service in $$services; do \
		name=$$(echo $$service | cut -d: -f1); \
		port=$$(echo $$service | cut -d: -f2); \
		status=$$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$$port/actuator/health 2>/dev/null || echo "DOWN"); \
		if [ "$$status" = "200" ]; then \
			echo "  $(GREEN)✅ $$name ($$port): UP$(NC)"; \
		else \
			echo "  $(RED)❌ $$name ($$port): DOWN$(NC)"; \
		fi; \
	done

.PHONY: create-test-user
create-test-user: ## Create a test user via API
	@echo "$(BLUE)👤 Creating test user...$(NC)"
	@curl -X POST http://localhost:8081/api/auth/register \
		-H "Content-Type: application/json" \
		-d '{"email":"test@ifood.com","password":"Test123!","name":"Test User"}' \
		| jq '.' || echo "Failed to create user"

# ==========================================
# Docker Management
# ==========================================

.PHONY: docker-build-all
docker-build-all: ## Build all Docker images
	@echo "$(PURPLE)🐳 Building all Docker images...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "Building Docker image for $$service..."; \
		cd $$service && docker build -t ifood-$$service:latest . && cd ..; \
	done
	@echo "$(GREEN)✅ All Docker images built!$(NC)"

.PHONY: docker-up
docker-up: ## Start all services using Docker Compose
	@echo "$(PURPLE)🐳 Starting all services with Docker...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d
	@echo "$(GREEN)✅ All services started with Docker!$(NC)"

.PHONY: docker-down
docker-down: ## Stop all Docker services
	@echo "$(RED)🐳 Stopping all Docker services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) down
	@docker compose -f $(DOCKER_COMPOSE_INFRA) down
	@echo "$(RED)✅ All Docker services stopped!$(NC)"

# ==========================================
# Utility Commands
# ==========================================

.PHONY: setup
setup: ## Initial project setup
	@echo "$(CYAN)⚙️ Setting up project...$(NC)"
	@mkdir -p logs
	@chmod +x init-postgres.sh
	@echo "$(GREEN)✅ Project setup completed!$(NC)"

.PHONY: ports
ports: ## Show all service ports
	@echo "$(CYAN)🔌 Service Ports:$(NC)"
	@echo "  Infrastructure:"
	@echo "    PostgreSQL: 5433"
	@echo "    Redis: 6380"
	@echo "    MongoDB: 27018"
	@echo "    Kafka: 9092"
	@echo "    Zookeeper: 2181"
	@echo ""
	@echo "  Core Services:"
	@echo "    Config Server: 8888"
	@echo "    Service Discovery: 8761"
	@echo ""
	@echo "  API Services:"
	@echo "    API Gateway: 8080"
	@echo ""
	@echo "  Business Services:"
	@echo "    Auth Service: 8081"
	@echo "    User Service: 8082"
	@echo "    Restaurant Service: 8083"
	@echo "    Menu Service: 8084"
	@echo "    Order Service: 8085"
	@echo "    Payment Service: 8086"
	@echo "    Notification Service: 8087"
	@echo "    Delivery Service: 8088"
	@echo "    Review Service: 8089"

# ==========================================
# Kubernetes Management
# ==========================================
.PHONY: k8s-setup k8s-deploy k8s-delete k8s-status
k8s-setup: ## Setup Kubernetes cluster (minikube or kind)
	@echo "$(BLUE)🔧 Setting up Kubernetes cluster...$(NC)"
	@./k8s-setup.sh

k8s-deploy: ## Deploy all services to Kubernetes
	@echo "$(BLUE)☸️ Deploying to Kubernetes...$(NC)"
	@kubectl apply -f k8s/ -R
	@echo "$(GREEN)✅ Services deployed to Kubernetes!$(NC)"

k8s-delete: ## Delete all services from Kubernetes
	@echo "$(YELLOW)🗑️ Deleting services from Kubernetes...$(NC)"
	@kubectl delete -f k8s/ -R --ignore-not-found=true
	@echo "$(GREEN)✅ Services deleted from Kubernetes!$(NC)"

k8s-status: ## Check Kubernetes cluster status
	@echo "$(BLUE)📊 Kubernetes Status$(NC)"
	@echo "$(YELLOW)Current Context:$(NC)"
	@kubectl config current-context 2>/dev/null || echo "No active context"
	@echo ""
	@echo "$(YELLOW)Nodes:$(NC)"
	@kubectl get nodes 2>/dev/null || echo "No cluster available"
	@echo ""
	@echo "$(YELLOW)Pods:$(NC)"
	@kubectl get pods --all-namespaces 2>/dev/null || echo "No cluster available"

# Default target
.DEFAULT_GOAL := help