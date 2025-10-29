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
DOCKER_COMPOSE_OBSERVABILITY=docker-compose.observability.yml
DOCKER_COMPOSE_SERVICES=docker-compose.yml

# Services
INFRASTRUCTURE_SERVICES=postgres redis mongodb kafka zookeeper
CORE_SERVICES=config-server service-discovery
API_SERVICES=api-gateway
BUSINESS_SERVICES=auth-service user-service restaurant-service menu-service order-service payment-service delivery-service notification-service review-service

# Service Groups for organized startup
INFRA_CORE_SERVICES=config-server service-discovery api-gateway
BUSINESS_CORE_SERVICES=auth-service user-service
BUSINESS_EXTENDED_SERVICES=restaurant-service menu-service order-service payment-service delivery-service notification-service review-service

ALL_SERVICES=$(CORE_SERVICES) $(API_SERVICES) $(BUSINESS_SERVICES)

# ==========================================
# Help and Information
# ==========================================

.PHONY: help
help: ## Show this help message
	@echo "$(CYAN)🚀 iFood Clone Microservices Management$(NC)"
	@echo ""
	@echo "$(GREEN)🏗️  Infrastructure Management:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /infra-|obs-/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)🎯 Organized Service Startup:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /start-|stop-|restart-|health-check/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)🔧 Service Management:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /services-|up-all|down-all|status-all|logs-/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)🔨 Build Tools:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /build-|create-|fix-|docker-build/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(GREEN)🐳 Container Management:$(NC)"
	@awk '/^[a-zA-Z_-]+.*:.*##/ && /docker-/ && !/docker-build/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, substr($$0, index($$0, "## ") + 3) }' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(BLUE)📋 Service Groups:$(NC)"
	@echo "  $(CYAN)Infrastructure Core:$(NC) $(INFRA_CORE_SERVICES)"
	@echo "  $(CYAN)Business Core:$(NC)      $(BUSINESS_CORE_SERVICES)"
	@echo "  $(CYAN)Business Extended:$(NC)  $(BUSINESS_EXTENDED_SERVICES)"
	@echo "  $(CYAN)External Infra:$(NC)     $(INFRASTRUCTURE_SERVICES)"
	@echo ""
	@echo "$(PURPLE)💡 Quick Start Examples:$(NC)"
	@echo "  $(YELLOW)make start-sequential$(NC)         # Start everything in proper order"
	@echo "  $(YELLOW)make start-infra-services$(NC)     # Start only Config, Discovery, Gateway"
	@echo "  $(YELLOW)make logs-auth-service$(NC)        # Follow logs for auth service"
	@echo "  $(YELLOW)make restart-service-user-service$(NC) # Restart specific service"
	@echo ""
	@echo "$(GREEN)🚀 Senior Build Approach:$(NC)"
	@echo "  $(YELLOW)make docker-build-all$(NC)         # Build all services with optimized approach"
	@echo "  $(YELLOW)make docker-build-service SERVICE=config-server$(NC) # Build specific service"
	@echo "  $(CYAN)✨ Benefits: Single source of truth, no pom.docker.xml duplication$(NC)"
	@echo ""
	@echo "$(GREEN)🐳 Container Operations:$(NC)"
	@echo "  $(YELLOW)make docker-full-cycle SERVICE=config-server$(NC) # Build & run complete cycle"
	@echo "  $(YELLOW)make docker-run-service SERVICE=auth-service$(NC)  # Run specific container"
	@echo "  $(YELLOW)make docker-logs-service SERVICE=user-service$(NC) # Follow container logs"
	@echo "  $(YELLOW)make docker-status-all$(NC)        # Show all container status"
	@echo "  $(YELLOW)make docker-start-core$(NC)        # Start Config + Discovery + Gateway"
	@echo "  $(YELLOW)make docker-stop-all$(NC)          # Stop all service containers"

# ==========================================
# Infrastructure Management (docker-compose.infrastructure.yml)
# ==========================================

.PHONY: infra-up
infra-up: ## Start infrastructure services (PostgreSQL, Redis, Kafka, MongoDB)
	@echo "$(GREEN)🚀 Starting infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) up -d
	@echo "$(GREEN)✅ Infrastructure services started!$(NC)"

.PHONY: infra-down
infra-down: ## Stop infrastructure services
	@echo "$(RED)🛑 Stopping infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) down
	@echo "$(RED)✅ Infrastructure services stopped!$(NC)"

.PHONY: infra-status
infra-status: ## Check status of infrastructure services
	@echo "$(CYAN)📊 Infrastructure Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) ps

.PHONY: infra-logs
infra-logs: ## Show logs from infrastructure services
	@echo "$(CYAN)📋 Infrastructure Logs:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_INFRA) logs -f --tail=100

# ==========================================
# Services Management (docker-compose.yml)
# ==========================================

.PHONY: services-up
services-up: ## Start services from docker-compose.yml
	@echo "$(GREEN)� Starting services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d
	@echo "$(GREEN)✅ Services started!$(NC)"

.PHONY: services-down
services-down: ## Stop services from docker-compose.yml
	@echo "$(RED)🛑 Stopping services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) down
	@echo "$(RED)✅ Services stopped!$(NC)"

.PHONY: services-status
services-status: ## Check status of services
	@echo "$(CYAN)📊 Services Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) ps

.PHONY: services-logs
services-logs: ## Show logs from services
	@echo "$(CYAN)📋 Services Logs:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) logs -f --tail=100

.PHONY: services-build
services-build: ## Build services Docker images
	@echo "$(GREEN)� Building services Docker images...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) build
	@echo "$(GREEN)✅ Services built!$(NC)"

.PHONY: services-build-up
services-build-up: ## Build and start services
	@echo "$(GREEN)� Building and starting services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up --build -d
	@echo "$(GREEN)✅ Services built and started!$(NC)"

# ==========================================
# Senior Build Approach - Optimized Docker Builds
# ==========================================

.PHONY: build-all-jars
build-all-jars: ## Build all JAR files using optimized approach
	@echo "$(GREEN)🔨 Building all JAR files...$(NC)"
	@./mvnw clean package spring-boot:repackage $(MAVEN_OPTS)
	@echo "$(GREEN)✅ All JARs built successfully!$(NC)"

.PHONY: docker-build-all
docker-build-all: build-all-jars ## Build all Docker images using optimized approach
	@echo "$(GREEN)🐳 Building all Docker images...$(NC)"
	@for service in $(CORE_SERVICES) $(API_SERVICES) $(BUSINESS_SERVICES); do \
		echo "$(CYAN)Building $$service...$(NC)"; \
		docker build -f $$service/Dockerfile -t ifood-$$service:latest . || exit 1; \
	done
	@echo "$(GREEN)✅ All Docker images built successfully!$(NC)"

.PHONY: docker-build-service
docker-build-service: ## Build specific service (usage: make docker-build-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-build-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)🔨 Building JAR for $(SERVICE)...$(NC)"
	@./mvnw clean package spring-boot:repackage $(MAVEN_OPTS) -pl $(SERVICE)
	@echo "$(GREEN)🐳 Building Docker image for $(SERVICE)...$(NC)"
	@docker build -f $(SERVICE)/Dockerfile -t ifood-$(SERVICE):latest .
	@echo "$(GREEN)✅ $(SERVICE) built successfully!$(NC)"

# ==========================================
# Container Management - Senior Approach
# ==========================================

# Service port mapping
CONFIG_SERVER_PORT=8888
SERVICE_DISCOVERY_PORT=8761
API_GATEWAY_PORT=8080
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8082
RESTAURANT_SERVICE_PORT=8083
MENU_SERVICE_PORT=8084
ORDER_SERVICE_PORT=8085
PAYMENT_SERVICE_PORT=8086
NOTIFICATION_SERVICE_PORT=8087
DELIVERY_SERVICE_PORT=8088
REVIEW_SERVICE_PORT=8089

.PHONY: docker-run-service
docker-run-service: ## Run specific service container (usage: make docker-run-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-run-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)🚀 Starting $(SERVICE) container...$(NC)"
	@make docker-run-$(SERVICE)
	@echo "$(GREEN)✅ $(SERVICE) container started!$(NC)"

.PHONY: docker-run-config-server
docker-run-config-server:
	@docker stop ifood-config-server 2>/dev/null || true
	@docker rm ifood-config-server 2>/dev/null || true
	@docker network create ifood-network 2>/dev/null || true
	@docker run -d --name ifood-config-server --network ifood-network \
		-p $(CONFIG_SERVER_PORT):$(CONFIG_SERVER_PORT) \
		ifood-config-server:latest

.PHONY: docker-run-service-discovery
docker-run-service-discovery:
	@docker stop ifood-service-discovery 2>/dev/null || true
	@docker rm ifood-service-discovery 2>/dev/null || true
	@docker network create ifood-network 2>/dev/null || true
	@docker run -d --name ifood-service-discovery --network ifood-network \
		-p $(SERVICE_DISCOVERY_PORT):$(SERVICE_DISCOVERY_PORT) \
		-e CONFIG_SERVER_URL=http://ifood-config-server:8888 \
		ifood-service-discovery:latest

.PHONY: docker-run-api-gateway
docker-run-api-gateway:
	@docker stop ifood-api-gateway 2>/dev/null || true
	@docker rm ifood-api-gateway 2>/dev/null || true
	@docker network create ifood-network 2>/dev/null || true
	@docker run -d --name ifood-api-gateway --network ifood-network \
		-p $(API_GATEWAY_PORT):$(API_GATEWAY_PORT) \
		-e CONFIG_SERVER_URL=http://ifood-config-server:8888 \
		-e EUREKA_SERVER_URL=http://ifood-service-discovery:8761 \
		-e REDIS_HOST=ifood-redis \
		-e REDIS_PORT=6379 \
		-e REDIS_PASSWORD=ifood_redis_pass \
		ifood-api-gateway:latest

.PHONY: docker-run-auth-service
docker-run-auth-service:
	@docker stop ifood-auth-service 2>/dev/null || true
	@docker rm ifood-auth-service 2>/dev/null || true
	@docker network create ifood-network 2>/dev/null || true
	@docker run -d --name ifood-auth-service --network ifood-network \
		-p $(AUTH_SERVICE_PORT):$(AUTH_SERVICE_PORT) \
		-e CONFIG_SERVER_URL=http://ifood-config-server:8888 \
		-e EUREKA_SERVER_URL=http://ifood-service-discovery:8761 \
		-e DB_HOST=ifood-postgres \
		-e DB_PORT=5432 \
		-e DB_NAME=ifood_db \
		-e DB_USER=ifood_user \
		-e DB_PASSWORD=ifood_pass \
		-e REDIS_HOST=ifood-redis \
		-e REDIS_PORT=6379 \
		-e REDIS_PASSWORD=ifood_redis_pass \
		ifood-auth-service:latest

.PHONY: docker-run-user-service
docker-run-user-service:
	@docker stop ifood-user-service 2>/dev/null || true
	@docker rm ifood-user-service 2>/dev/null || true
	@docker network create ifood-network 2>/dev/null || true
	@docker run -d --name ifood-user-service --network ifood-network \
		-p $(USER_SERVICE_PORT):$(USER_SERVICE_PORT) \
		-e CONFIG_SERVER_URL=http://ifood-config-server:8888 \
		-e EUREKA_SERVER_URL=http://ifood-service-discovery:8761 \
		-e DB_HOST=ifood-postgres \
		-e DB_PORT=5432 \
		-e DB_NAME=ifood_db \
		-e DB_USER=ifood_user \
		-e DB_PASSWORD=ifood_pass \
		-e REDIS_HOST=ifood-redis \
		-e REDIS_PORT=6379 \
		-e REDIS_PASSWORD=ifood_redis_pass \
		ifood-user-service:latest

.PHONY: docker-run-restaurant-service
docker-run-restaurant-service:
	@docker stop ifood-restaurant-service 2>/dev/null || true
	@docker rm ifood-restaurant-service 2>/dev/null || true
	@docker run -d --name ifood-restaurant-service -p $(RESTAURANT_SERVICE_PORT):$(RESTAURANT_SERVICE_PORT) ifood-restaurant-service:latest

.PHONY: docker-run-menu-service
docker-run-menu-service:
	@docker stop ifood-menu-service 2>/dev/null || true
	@docker rm ifood-menu-service 2>/dev/null || true
	@docker run -d --name ifood-menu-service -p $(MENU_SERVICE_PORT):$(MENU_SERVICE_PORT) ifood-menu-service:latest

.PHONY: docker-run-order-service
docker-run-order-service:
	@docker stop ifood-order-service 2>/dev/null || true
	@docker rm ifood-order-service 2>/dev/null || true
	@docker run -d --name ifood-order-service -p $(ORDER_SERVICE_PORT):$(ORDER_SERVICE_PORT) ifood-order-service:latest

.PHONY: docker-run-payment-service
docker-run-payment-service:
	@docker stop ifood-payment-service 2>/dev/null || true
	@docker rm ifood-payment-service 2>/dev/null || true
	@docker run -d --name ifood-payment-service -p $(PAYMENT_SERVICE_PORT):$(PAYMENT_SERVICE_PORT) ifood-payment-service:latest

.PHONY: docker-run-notification-service
docker-run-notification-service:
	@docker stop ifood-notification-service 2>/dev/null || true
	@docker rm ifood-notification-service 2>/dev/null || true
	@docker run -d --name ifood-notification-service -p $(NOTIFICATION_SERVICE_PORT):$(NOTIFICATION_SERVICE_PORT) ifood-notification-service:latest

.PHONY: docker-run-delivery-service
docker-run-delivery-service:
	@docker stop ifood-delivery-service 2>/dev/null || true
	@docker rm ifood-delivery-service 2>/dev/null || true
	@docker run -d --name ifood-delivery-service -p $(DELIVERY_SERVICE_PORT):$(DELIVERY_SERVICE_PORT) ifood-delivery-service:latest

.PHONY: docker-run-review-service
docker-run-review-service:
	@docker stop ifood-review-service 2>/dev/null || true
	@docker rm ifood-review-service 2>/dev/null || true
	@docker run -d --name ifood-review-service -p $(REVIEW_SERVICE_PORT):$(REVIEW_SERVICE_PORT) ifood-review-service:latest

.PHONY: docker-stop-service
docker-stop-service: ## Stop specific service container (usage: make docker-stop-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-stop-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(YELLOW)⏹️  Stopping $(SERVICE) container...$(NC)"
	@docker stop ifood-$(SERVICE) 2>/dev/null || echo "$(YELLOW)Container ifood-$(SERVICE) not running$(NC)"
	@echo "$(GREEN)✅ $(SERVICE) container stopped!$(NC)"

.PHONY: docker-remove-service
docker-remove-service: ## Remove specific service container (usage: make docker-remove-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-remove-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(RED)🗑️  Removing $(SERVICE) container...$(NC)"
	@docker stop ifood-$(SERVICE) 2>/dev/null || true
	@docker rm ifood-$(SERVICE) 2>/dev/null || echo "$(YELLOW)Container ifood-$(SERVICE) not found$(NC)"
	@echo "$(GREEN)✅ $(SERVICE) container removed!$(NC)"

.PHONY: docker-logs-service
docker-logs-service: ## Show logs for specific service container (usage: make docker-logs-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-logs-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(CYAN)📋 Showing logs for $(SERVICE)...$(NC)"
	@docker logs -f --tail=100 ifood-$(SERVICE)

.PHONY: docker-exec-service
docker-exec-service: ## Execute bash in specific service container (usage: make docker-exec-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-exec-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(CYAN)🔧 Accessing $(SERVICE) container...$(NC)"
	@docker exec -it ifood-$(SERVICE) /bin/sh

.PHONY: docker-status-all
docker-status-all: ## Show status of all service containers
	@echo "$(CYAN)📊 Container Status:$(NC)"
	@echo "$(YELLOW)CONTAINER$(NC)           $(YELLOW)IMAGE$(NC)                    $(YELLOW)STATUS$(NC)              $(YELLOW)PORTS$(NC)"
	@docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | grep ifood || echo "$(YELLOW)No ifood containers found$(NC)"

.PHONY: docker-restart-service
docker-restart-service: ## Restart specific service container (usage: make docker-restart-service SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-restart-service SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@echo "$(YELLOW)🔄 Restarting $(SERVICE) container...$(NC)"
	@docker restart ifood-$(SERVICE)
	@echo "$(GREEN)✅ $(SERVICE) container restarted!$(NC)"

.PHONY: docker-cleanup
docker-cleanup: ## Remove all stopped containers and unused images
	@echo "$(YELLOW)🧹 Cleaning up Docker resources...$(NC)"
	@docker container prune -f
	@docker image prune -f
	@echo "$(GREEN)✅ Docker cleanup completed!$(NC)"

.PHONY: docker-full-cycle
docker-full-cycle: ## Build and run specific service (usage: make docker-full-cycle SERVICE=config-server)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-full-cycle SERVICE=config-server$(NC)"; \
		exit 1; \
	fi
	@make docker-remove-service SERVICE=$(SERVICE)
	@make docker-build-service SERVICE=$(SERVICE)
	@make docker-run-service SERVICE=$(SERVICE)
	@echo "$(GREEN)🎉 Full cycle completed for $(SERVICE)!$(NC)"

.PHONY: docker-network-create
docker-network-create: ## Create Docker network for services
	@echo "$(GREEN)🌐 Creating Docker network...$(NC)"
	@docker network create ifood-network 2>/dev/null || echo "$(YELLOW)Network ifood-network already exists$(NC)"
	@echo "$(GREEN)✅ Network ready!$(NC)"

.PHONY: docker-start-core
docker-start-core: docker-network-create ## Start core services (Config, Discovery, Gateway) as containers
	@echo "$(GREEN)🚀 Starting core services as containers...$(NC)"
	@for service in $(CORE_SERVICES) $(API_SERVICES); do \
		echo "$(CYAN)Starting $$service...$(NC)"; \
		make docker-full-cycle SERVICE=$$service; \
		sleep 10; \
	done
	@echo "$(GREEN)✅ Core services started as containers!$(NC)"

.PHONY: docker-start-business
docker-start-business: ## Start business services as containers (requires core services running)
	@echo "$(GREEN)🚀 Starting business services as containers...$(NC)"
	@for service in $(BUSINESS_SERVICES); do \
		echo "$(CYAN)Starting $$service...$(NC)"; \
		make docker-full-cycle SERVICE=$$service; \
		sleep 5; \
	done
	@echo "$(GREEN)✅ Business services started as containers!$(NC)"

.PHONY: docker-stop-all
docker-stop-all: ## Stop all service containers
	@echo "$(YELLOW)⏹️  Stopping all service containers...$(NC)"
	@for service in $(CORE_SERVICES) $(API_SERVICES) $(BUSINESS_SERVICES); do \
		make docker-stop-service SERVICE=$$service; \
	done
	@echo "$(GREEN)✅ All service containers stopped!$(NC)"

.PHONY: docker-health-check
docker-health-check: ## Check health of all running services
	@echo "$(CYAN)🔍 Checking service health...$(NC)"
	@echo "$(YELLOW)Config Server (8888):$(NC)"
	@curl -s http://localhost:8888/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' || echo "❌ Not responding"
	@echo ""
	@echo "$(YELLOW)Service Discovery (8761):$(NC)"
	@curl -s http://localhost:8761/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' || echo "❌ Not responding"
	@echo ""
	@echo "$(YELLOW)API Gateway (8080):$(NC)"
	@curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' || echo "❌ Not responding"
	@echo ""
	@echo "$(GREEN)✅ Health check completed!$(NC)"

.PHONY: docker-urls
docker-urls: ## Show service URLs
	@echo "$(CYAN)🌐 Service URLs:$(NC)"
	@echo "  $(YELLOW)Config Server:$(NC)     http://localhost:8888"
	@echo "  $(YELLOW)Service Discovery:$(NC) http://localhost:8761"
	@echo "  $(YELLOW)API Gateway:$(NC)       http://localhost:8080"
	@echo "  $(YELLOW)Eureka Dashboard:$(NC)  http://localhost:8761/"
	@echo ""
	@echo "$(CYAN)🔍 Health Endpoints:$(NC)"
	@echo "  $(YELLOW)Config Server:$(NC)     http://localhost:8888/actuator/health"
	@echo "  $(YELLOW)Service Discovery:$(NC) http://localhost:8761/actuator/health"
	@echo "  $(YELLOW)API Gateway:$(NC)       http://localhost:8080/actuator/health"

.PHONY: docker-debug-service
docker-debug-service: ## Debug specific service (usage: make docker-debug-service SERVICE=api-gateway)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-debug-service SERVICE=api-gateway$(NC)"; \
		exit 1; \
	fi
	@echo "$(CYAN)🔍 Debugging $(SERVICE)...$(NC)"
	@echo "$(YELLOW)1. Checking if JAR exists:$(NC)"
	@ls -la $(SERVICE)/target/*.jar 2>/dev/null || echo "❌ JAR not found in $(SERVICE)/target/"
	@echo ""
	@echo "$(YELLOW)2. Checking if Docker image exists:$(NC)"
	@docker images | grep ifood-$(SERVICE) || echo "❌ Docker image not found"
	@echo ""
	@echo "$(YELLOW)3. Checking container status:$(NC)"
	@docker ps -a | grep ifood-$(SERVICE) || echo "❌ Container not found"
	@echo ""
	@echo "$(YELLOW)4. Last container logs (if exists):$(NC)"
	@docker logs ifood-$(SERVICE) --tail=20 2>/dev/null || echo "❌ No logs available"

.PHONY: docker-force-rebuild
docker-force-rebuild: ## Force complete rebuild of service (usage: make docker-force-rebuild SERVICE=api-gateway)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)❌ Please specify SERVICE. Usage: make docker-force-rebuild SERVICE=api-gateway$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)🔥 Force rebuilding $(SERVICE)...$(NC)"
	@echo "$(CYAN)Step 1: Stopping and removing container...$(NC)"
	@docker stop ifood-$(SERVICE) 2>/dev/null || true
	@docker rm ifood-$(SERVICE) 2>/dev/null || true
	@echo "$(CYAN)Step 2: Removing Docker image...$(NC)"
	@docker rmi ifood-$(SERVICE):latest 2>/dev/null || true
	@echo "$(CYAN)Step 3: Cleaning Maven build...$(NC)"
	@./mvnw clean -pl $(SERVICE) -q
	@echo "$(CYAN)Step 4: Building JAR...$(NC)"
	@./mvnw package spring-boot:repackage -DskipTests -pl $(SERVICE)
	@echo "$(CYAN)Step 5: Building Docker image...$(NC)"
	@docker build -f $(SERVICE)/Dockerfile -t ifood-$(SERVICE):latest .
	@echo "$(CYAN)Step 6: Starting container...$(NC)"
	@make docker-run-$(SERVICE)
	@echo "$(GREEN)✅ $(SERVICE) force rebuild completed!$(NC)"

# ==========================================
# Observability Management (docker-compose.observability.yml)
# ==========================================

.PHONY: obs-up
obs-up: ## Start observability stack (Prometheus, Grafana, Loki, Promtail)
	@echo "$(GREEN)� Starting observability stack...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_OBSERVABILITY) up -d
	@echo "$(GREEN)✅ Observability stack started!$(NC)"

.PHONY: obs-down
obs-down: ## Stop observability stack
	@echo "$(RED)� Stopping observability stack...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_OBSERVABILITY) down
	@echo "$(RED)✅ Observability stack stopped!$(NC)"

.PHONY: obs-status
obs-status: ## Check status of observability services
	@echo "$(CYAN)� Observability Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_OBSERVABILITY) ps

.PHONY: obs-logs
obs-logs: ## Show logs from observability services
	@echo "$(CYAN)� Observability Logs:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_OBSERVABILITY) logs -f --tail=100

# ==========================================
# Combined Commands
# ==========================================

.PHONY: up-all
up-all: ## Start everything (infra + services + observability)
	@echo "$(PURPLE)� Starting complete environment...$(NC)"
	@make infra-up
	@echo "$(BLUE)⏳ Waiting for infrastructure...$(NC)"
	@sleep 15
	@make services-up
	@echo "$(BLUE)⏳ Waiting for services...$(NC)"
	@sleep 10
	@make obs-up
	@echo "$(GREEN)🎉 Complete environment ready!$(NC)"

.PHONY: down-all
down-all: ## Stop everything
	@echo "$(RED)🛑 Stopping complete environment...$(NC)"
	@make services-down 2>/dev/null || true
	@make infra-down 2>/dev/null || true
	@make obs-down 2>/dev/null || true
	@echo "$(RED)✅ Everything stopped!$(NC)"

.PHONY: status-all
status-all: ## Show status of all environments
	@echo "$(CYAN)📊 Complete Environment Status:$(NC)"
	@echo ""
	@make infra-status
	@echo ""
	@make services-status
	@echo ""
	@make obs-status

# ==========================================
# Organized Service Startup
# ==========================================

.PHONY: start-infra-services
start-infra-services: ## Start infrastructure services (Config Server, Service Discovery, API Gateway)
	@echo "$(PURPLE)🏗️  Starting Infrastructure Services...$(NC)"
	@echo "$(BLUE)📋 Services: $(INFRA_CORE_SERVICES)$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d config-server
	@echo "$(YELLOW)⏳ Waiting for Config Server to be ready...$(NC)"
	@sleep 10
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d service-discovery
	@echo "$(YELLOW)⏳ Waiting for Service Discovery to be ready...$(NC)"
	@sleep 15
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d api-gateway
	@echo "$(YELLOW)⏳ Waiting for API Gateway to be ready...$(NC)"
	@sleep 10
	@echo "$(GREEN)✅ Infrastructure services started successfully!$(NC)"
	@echo "$(CYAN)📊 Infrastructure Services Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) ps config-server service-discovery api-gateway

.PHONY: start-business-core
start-business-core: ## Start core business services (Auth Service, User Service)
	@echo "$(PURPLE)🔐 Starting Core Business Services...$(NC)"
	@echo "$(BLUE)📋 Services: $(BUSINESS_CORE_SERVICES)$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d auth-service
	@echo "$(YELLOW)⏳ Waiting for Auth Service to be ready...$(NC)"
	@sleep 10
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d user-service
	@echo "$(YELLOW)⏳ Waiting for User Service to be ready...$(NC)"
	@sleep 10
	@echo "$(GREEN)✅ Core business services started successfully!$(NC)"
	@echo "$(CYAN)📊 Core Business Services Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) ps auth-service user-service

.PHONY: start-business-extended
start-business-extended: ## Start extended business services (Restaurant, Menu, Order, Payment, etc.)
	@echo "$(PURPLE)🛒 Starting Extended Business Services...$(NC)"
	@echo "$(BLUE)📋 Services: $(BUSINESS_EXTENDED_SERVICES)$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d restaurant-service menu-service
	@echo "$(YELLOW)⏳ Waiting for Restaurant and Menu services...$(NC)"
	@sleep 8
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d order-service payment-service
	@echo "$(YELLOW)⏳ Waiting for Order and Payment services...$(NC)"
	@sleep 8
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) up -d delivery-service notification-service review-service
	@echo "$(YELLOW)⏳ Waiting for remaining services...$(NC)"
	@sleep 8
	@echo "$(GREEN)✅ Extended business services started successfully!$(NC)"
	@echo "$(CYAN)📊 Extended Business Services Status:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) ps restaurant-service menu-service order-service payment-service delivery-service notification-service review-service

.PHONY: start-sequential
start-sequential: ## Start all services in proper sequence (infra → infrastructure → core business → extended business)
	@echo "$(PURPLE)🚀 Starting Complete iFood Clone Environment Sequentially...$(NC)"
	@echo ""
	@echo "$(CYAN)Step 1/4: Infrastructure (Database, Cache, Message Broker)$(NC)"
	@make infra-up
	@echo "$(YELLOW)⏳ Waiting for infrastructure to stabilize...$(NC)"
	@sleep 20
	@echo ""
	@echo "$(CYAN)Step 2/4: Core Infrastructure Services$(NC)"
	@make start-infra-services
	@echo ""
	@echo "$(CYAN)Step 3/4: Core Business Services$(NC)"
	@make start-business-core
	@echo ""
	@echo "$(CYAN)Step 4/4: Extended Business Services$(NC)"
	@make start-business-extended
	@echo ""
	@echo "$(GREEN)🎉 Complete iFood Clone Environment is Ready!$(NC)"
	@echo "$(BLUE)🌐 Access points:$(NC)"
	@echo "  • Config Server:    http://localhost:8888"
	@echo "  • Service Discovery: http://localhost:8761"
	@echo "  • API Gateway:      http://localhost:8080"
	@echo "  • Auth Service:     http://localhost:8081"
	@echo "  • User Service:     http://localhost:8082"

.PHONY: stop-business-services
stop-business-services: ## Stop all business services
	@echo "$(RED)🛑 Stopping all business services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) stop $(BUSINESS_CORE_SERVICES) $(BUSINESS_EXTENDED_SERVICES)
	@echo "$(RED)✅ Business services stopped!$(NC)"

.PHONY: stop-infra-services
stop-infra-services: ## Stop infrastructure services (Config, Discovery, Gateway)
	@echo "$(RED)🛑 Stopping infrastructure services...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) stop $(INFRA_CORE_SERVICES)
	@echo "$(RED)✅ Infrastructure services stopped!$(NC)"

.PHONY: restart-service-%
restart-service-%: ## Restart specific service (e.g., make restart-service-auth-service)
	@echo "$(YELLOW)🔄 Restarting $*...$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) restart $*
	@echo "$(GREEN)✅ $* restarted!$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) ps $*

.PHONY: logs-%
logs-%: ## Show logs for specific service (e.g., make logs-auth-service)
	@echo "$(CYAN)📋 Logs for $*:$(NC)"
	@docker compose -f $(DOCKER_COMPOSE_SERVICES) logs -f --tail=50 $*

.PHONY: health-check-all
health-check-all: ## Check health of all running services
	@echo "$(CYAN)🏥 Health Check for All Services:$(NC)"
	@echo ""
	@echo "$(BLUE)Infrastructure Services:$(NC)"
	@for service in config-server service-discovery api-gateway; do \
		echo -n "$$service: "; \
		if docker compose -f $(DOCKER_COMPOSE_SERVICES) ps $$service | grep -q "Up"; then \
			echo "$(GREEN)✅ Running$(NC)"; \
		else \
			echo "$(RED)❌ Stopped$(NC)"; \
		fi; \
	done
	@echo ""
	@echo "$(BLUE)Business Services:$(NC)"
	@for service in $(BUSINESS_CORE_SERVICES) $(BUSINESS_EXTENDED_SERVICES); do \
		echo -n "$$service: "; \
		if docker compose -f $(DOCKER_COMPOSE_SERVICES) ps $$service | grep -q "Up"; then \
			echo "$(GREEN)✅ Running$(NC)"; \
		else \
			echo "$(RED)❌ Stopped$(NC)"; \
		fi; \
	done

# ==========================================
# Maven Build Commands
# ==========================================

.PHONY: build-all
build-all: ## Build all microservices with Maven
	@echo "$(GREEN)🔨 Building all microservices...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "$(BLUE)Building $$service...$(NC)"; \
		if [ -d "$$service" ]; then \
			(cd $$service && mvn clean package $(MAVEN_OPTS)) || exit 1; \
		else \
			echo "$(RED)❌ Directory $$service not found!$(NC)"; \
			exit 1; \
		fi; \
	done
	@echo "$(GREEN)✅ All services built successfully!$(NC)"

.PHONY: build-%
build-%: ## Build specific service with Maven (e.g., make build-auth-service)
	.PHONY: build-%
build-%: ## Build specific service with Maven (e.g., make build-auth-service)
	@echo "$(BLUE)🔨 Building $*...$(NC)"
	@if [ -d "$*" ]; then \
		(cd $* && mvn clean package $(MAVEN_OPTS)) || exit 1; \
		echo "$(GREEN)✅ $* built successfully!$(NC)"; \
	else \
		echo "$(RED)❌ Directory $* not found!$(NC)"; \
		exit 1; \
	fi

.PHONY: create-docker-poms
create-docker-poms: ## Create standalone pom.docker.xml for all services
	@echo "$(YELLOW)🔧 Creating pom.docker.xml for all services...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "Creating $$service/pom.docker.xml..."; \
		if [ -f "$$service/pom.xml" ] && [ ! -f "$$service/pom.docker.xml" ]; then \
			cp menu-service/pom.docker.xml $$service/pom.docker.xml; \
			sed -i '' "s|<artifactId>menu-service</artifactId>|<artifactId>$$service</artifactId>|g" $$service/pom.docker.xml; \
			sed -i '' "s|<name>Menu Service</name>|<name>$$(echo $$service | sed 's/-/ /g' | sed 's/\b\w/\U&/g') Service</name>|g" $$service/pom.docker.xml; \
		fi; \
	done
	@echo "$(GREEN)✅ All pom.docker.xml files created!$(NC)"

.PHONY: fix-dockerfiles
fix-dockerfiles: ## Fix all Dockerfiles to use standalone pom.docker.xml
	@echo "$(YELLOW)🔧 Fixing all Dockerfiles to use standalone pom.docker.xml...$(NC)"
	@for service in $(ALL_SERVICES); do \
		echo "Fixing $$service/Dockerfile..."; \
		if [ -f "$$service/Dockerfile" ]; then \
			cp menu-service/Dockerfile $$service/Dockerfile.new; \
			sed -i '' "s|menu-service|$$service|g" $$service/Dockerfile.new; \
			mv $$service/Dockerfile.new $$service/Dockerfile; \
		fi; \
	done
	@echo "$(GREEN)✅ All Dockerfiles fixed!$(NC)"

# Default target
.DEFAULT_GOAL := help



# Default target
.DEFAULT_GOAL := help