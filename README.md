# iFood Clone - Microservices Architecture

Um clone do iFood implementado com arquitetura de microsserviços usando tecnologias modernas.

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 3.2+**
- **Spring Cloud 2023.0.x**
- **Apache Kafka**
- **Docker & Docker Compose**
- **Kubernetes**
- **PostgreSQL**
- **Redis**
- **MongoDB**

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │    Web App      │
└─────────────────┘    └─────────────────┘
          │                      │
          └──────────┬───────────┘
                     │
         ┌─────────────────┐
         │   API Gateway   │
         └─────────────────┘
                     │
    ┌────────────────┼────────────────┐
    │                │                │
┌─────────────────┐ │ ┌─────────────────┐
│ Service Discovery│ │ │ Config Server   │
└─────────────────┘ │ └─────────────────┘
                    │
     ┌──────────────┼──────────────┐
     │              │              │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│Auth Service │ │User Service │ │Rest. Service│
└─────────────┘ └─────────────┘ └─────────────┘
     │              │              │
     └──────────────┼──────────────┘
                    │
         ┌─────────────────┐
         │     Kafka       │
         └─────────────────┘
```

## 📋 Microsserviços

1. **API Gateway** - Roteamento e autenticação
2. **Service Discovery** - Eureka Server
3. **Config Server** - Configuração centralizada
4. **Auth Service** - Autenticação JWT
5. **User Service** - Gerenciamento de usuários
6. **Restaurant Service** - Gerenciamento de restaurantes
7. **Menu Service** - Cardápios e pratos
8. **Order Service** - Processamento de pedidos
9. **Payment Service** - Processamento de pagamentos
10. **Notification Service** - Notificações em tempo real
11. **Delivery Service** - Rastreamento de entregas
12. **Review Service** - Avaliações e comentários

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Execução com Docker Compose
```bash
docker-compose up -d
```

### Execução com Kubernetes
```bash
kubectl apply -f k8s/
```

## 📡 APIs

| Serviço | Porta | Endpoint |
|---------|-------|----------|
| API Gateway | 8080 | http://localhost:8080 |
| Eureka Server | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| Auth Service | 8081 | http://localhost:8081 |
| User Service | 8082 | http://localhost:8082 |
| Restaurant Service | 8083 | http://localhost:8083 |

## 🔧 Configuração

Cada microsserviço possui sua própria configuração no Config Server localizada em `config-server/src/main/resources/config/`.

## 📊 Monitoramento

- **Spring Boot Actuator** - Health checks
- **Zipkin** - Distributed tracing
- **Prometheus** - Métricas
- **Grafana** - Dashboards

## 🗄️ Bancos de Dados

- **PostgreSQL** - Dados relacionais (Users, Restaurants, Orders)
- **MongoDB** - Dados não relacionais (Reviews, Logs)
- **Redis** - Cache e sessões