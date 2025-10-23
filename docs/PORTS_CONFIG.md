# 🔌 Configuração de Portas - iFood Clone

## 📋 Portas Alteradas para Evitar Conflitos

As portas foram alteradas no `docker-compose.yml` para evitar conflitos com seus serviços locais:

### **Bancos de Dados (Portas Externas Alteradas)**

| Serviço    | Porta Local | Porta Docker | URL Interna Container | URL Externa (Host) |
|------------|-------------|--------------|----------------------|-------------------|
| PostgreSQL | 5432        | **5433**     | `postgres:5432`      | `localhost:5433`  |
| MongoDB    | 27017       | **27018**    | `mongodb:27017`      | `localhost:27018` |
| Redis      | 6379        | **6380**     | `redis:6379`         | `localhost:6380`  |

### **Microsserviços (Portas Originais Mantidas)**

| Serviço           | Porta | URL                     | Descrição              |
|-------------------|-------|-------------------------|------------------------|
| Config Server     | 8888  | http://localhost:8888   | Servidor de Configuração |
| Service Discovery | 8761  | http://localhost:8761   | Eureka Server          |
| API Gateway       | 8080  | http://localhost:8080   | Gateway Principal      |
| Auth Service      | 8081  | http://localhost:8081   | Autenticação           |
| User Service      | 8082  | http://localhost:8082   | Gestão de Usuários     |
| Restaurant Service| 8083  | http://localhost:8083   | Gestão de Restaurantes |
| Menu Service      | 8084  | http://localhost:8084   | Gestão de Menus        |
| Order Service     | 8085  | http://localhost:8085   | Gestão de Pedidos      |
| Payment Service   | 8086  | http://localhost:8086   | Processamento Pagamentos|
| Notification Service| 8087| http://localhost:8087   | Sistema de Notificações |
| Delivery Service  | 8088  | http://localhost:8088   | Gestão de Entregas     |
| Review Service    | 8089  | http://localhost:8089   | Sistema de Avaliações  |

### **Infraestrutura**

| Serviço    | Porta | URL                    | Descrição           |
|------------|-------|------------------------|---------------------|
| Zookeeper  | 2181  | localhost:2181         | Coordenação Kafka   |
| Kafka      | 9092  | localhost:9092         | Message Broker      |

## 🚀 Como Usar

### **1. Subir apenas a infraestrutura (recomendado para desenvolvimento):**
```bash
docker-compose up postgres mongodb redis kafka zookeeper
```

### **2. Subir tudo:**
```bash
docker-compose up --build
```

### **3. Conectar aos bancos externamente:**

#### **PostgreSQL:**
```bash
# Via psql
psql -h localhost -p 5433 -U ifood_user -d ifood_db

# Connection String para aplicações externas
jdbc:postgresql://localhost:5433/ifood_db
```

#### **MongoDB:**
```bash
# Via mongosh
mongosh mongodb://ifood_user:ifood_pass@localhost:27018/admin

# Connection String para aplicações externas
mongodb://ifood_user:ifood_pass@localhost:27018/
```

#### **Redis:**
```bash
# Via redis-cli
redis-cli -h localhost -p 6380

# Connection String para aplicações externas
redis://localhost:6380
```

## ⚙️ Configuração para Desenvolvimento Local

Se você quiser rodar os microsserviços localmente (fora do Docker) e usar apenas a infraestrutura no Docker:

### **1. Subir apenas infraestrutura:**
```bash
docker-compose up postgres mongodb redis kafka zookeeper
```

### **2. Configurar application.yml dos serviços:**
```yaml
# Para serviços que usam PostgreSQL
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ifood_db
    username: ifood_user
    password: ifood_pass

# Para serviços que usam Redis  
spring:
  data:
    redis:
      host: localhost
      port: 6380

# Para serviços que usam MongoDB
spring:
  data:
    mongodb:
      uri: mongodb://ifood_user:ifood_pass@localhost:27018/database_name
```

### **3. Executar serviços localmente:**
```bash
# Terminal 1 - Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 - Service Discovery  
cd service-discovery && mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4+ - Outros serviços
cd auth-service && mvn spring-boot:run
```

## 🔍 Verificar Conectividade

### **Testar conexões:**
```bash
# PostgreSQL
pg_isready -h localhost -p 5433

# MongoDB  
mongosh mongodb://ifood_user:ifood_pass@localhost:27018/admin --eval "db.runCommand('ping')"

# Redis
redis-cli -h localhost -p 6380 ping
```

### **Verificar logs dos containers:**
```bash
docker-compose logs postgres
docker-compose logs mongodb
docker-compose logs redis
```

## 🚫 Conflitos Resolvidos

✅ **PostgreSQL**: Porta alterada de 5432 → 5433  
✅ **MongoDB**: Porta alterada de 27017 → 27018  
✅ **Redis**: Porta alterada de 6379 → 6380  

Seus serviços locais continuam funcionando nas portas originais!