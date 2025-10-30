# 🎨 Diagrama Visual iFood Clone - Template para Excalidraw

## 🏗️ **Layout Completo do Sistema**

```
                        ┌─────────────────┐    ┌─────────────────┐
                        │  📱 Mobile App  │    │   🌐 Web App    │
                        │                 │    │                 │
                        └─────────────────┘    └─────────────────┘
                                 │                       │
                                 └───────────┬───────────┘
                                             │
                                ┌─────────────────────┐
                                │   🚪 API Gateway   │
                                │       :8080         │
                                └─────────────────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
        ┌─────────────────┐                 │                 ┌─────────────────┐
        │ ⚙️ Config Server │                │                 │ 🔍 Service      │
        │     :8888       │                │                 │   Discovery     │
        └─────────────────┘                │                 │     :8761       │
                    │                      │                 └─────────────────┘
                    │                      │                          │
                    └──────────────────────┼──────────────────────────┘
                                           │
        ┌──────────────────────────────────┼──────────────────────────────────┐
        │                                  │                                  │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ 🔐 Auth     │ │ 👤 User     │ │ 🏪 Restaurant│ │ 📋 Menu     │ │ 🛒 Order    │
│   :8081     │ │   :8082     │ │   :8083     │ │   :8084     │ │   :8085     │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │               │               │               │               │
        │               │               │               │               │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ 💳 Payment  │ │ 📢 Notify   │ │ 🚚 Delivery │ │ ⭐ Review   │
│   :8086     │ │   :8087     │ │   :8088     │ │   :8089     │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │               │               │               │
        └───────────────┼───────────────┼───────────────┘
                        │               │
                        │               │
        ┌───────────────┼───────────────┼───────────────┬───────────────┐
        │               │               │               │               │
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ 🐘 PostgreSQL   │ │ 🍃 MongoDB      │ │ ⚡ Redis        │ │ 📨 Kafka        │
│    :5433        │ │    :27018       │ │    :6380        │ │    :9092        │
│                 │ │                 │ │                 │ │                 │
│ • Auth          │ │ • Notifications │ │ • Cache         │ │ • Events        │
│ • Users         │ │ • Reviews       │ │ • Sessions      │ │ • Messages      │
│ • Restaurants   │ │ • Logs          │ │ • JWT Blacklist │ │ • Async Comm    │
│ • Menus         │ │                 │ │ • Rate Limiting │ │                 │
│ • Orders        │ │                 │ │                 │ │                 │
│ • Payments      │ │                 │ │                 │ │                 │
│ • Deliveries    │ │                 │ │                 │ │                 │
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 🎨 **Guia de Cores para Excalidraw**

### 🎯 **Paleta de Cores**

| Componente | Cor | Código Hex |
|------------|-----|------------|
| **Clientes** | Azul claro | `#E3F2FD` |
| **Gateway** | Azul médio | `#BBDEFB` |
| **Infraestrutura** | Roxo claro | `#F3E5F5` |
| **Auth Service** | Verde escuro | `#C8E6C9` |
| **Business Services** | Verde claro | `#E8F5E8` |
| **PostgreSQL** | Azul escuro | `#E1F5FE` |
| **MongoDB** | Verde mongo | `#E8F5E8` |
| **Redis** | Vermelho claro | `#FFEBEE` |
| **Kafka** | Laranja | `#FFF3E0` |

## 🔗 **Mapa de Conexões**

### 📱 **Fluxo Principal**
```
Mobile/Web → API Gateway → Microsserviços → Bancos de Dados
```

### ⚙️ **Configuração**
```
Config Server → Todos os Microsserviços
Service Discovery ↔ Todos os Microsserviços
```

### 🗄️ **Persistência**
```
PostgreSQL ←→ Auth, User, Restaurant, Menu, Order, Payment, Delivery
MongoDB ←→ Notification, Review  
Redis ←→ Auth (JWT), Gateway (Rate Limiting)
Kafka ←→ Todos os Microsserviços (Eventos)
```

## 📐 **Dimensões Sugeridas**

### 📦 **Caixas de Serviços**
- **Largura**: 140px
- **Altura**: 80px
- **Espaçamento**: 20px

### 🗄️ **Bancos de Dados**
- **Largura**: 160px  
- **Altura**: 120px
- **Espaçamento**: 15px

### 🔗 **Setas**
- **Espessura**: 3px
- **Tipo HTTP**: Sólida
- **Tipo Database**: Tracejada
- **Tipo Config**: Pontilhada

## 🏷️ **Labels Detalhados**

### 🔐 **Auth Service**
```
🔐 Auth Service
:8081
─────────────
• JWT Token
• User Login
• Authorization
• Security
```

### 👤 **User Service**
```
👤 User Service  
:8082
─────────────
• User Profiles
• Registration
• User Management
• Preferences
```

### 🏪 **Restaurant Service**
```
🏪 Restaurant Service
:8083
─────────────
• Restaurant Info
• Business Hours
• Location Data
• Owner Management
```

### 📋 **Menu Service**
```
📋 Menu Service
:8084
─────────────
• Food Items
• Pricing
• Categories
• Availability
```

### 🛒 **Order Service**
```
🛒 Order Service
:8085
─────────────
• Order Processing
• Cart Management
• Order Status
• Order History
```

### 💳 **Payment Service**
```
💳 Payment Service
:8086
─────────────
• Payment Processing
• Transaction Log
• Refunds
• Payment Methods
```

### 📢 **Notification Service**
```
📢 Notification Service
:8087
─────────────
• Push Notifications
• Email Alerts
• SMS Messages
• Real-time Updates
```

### 🚚 **Delivery Service**
```
🚚 Delivery Service
:8088
─────────────
• Delivery Tracking
• Driver Assignment
• Route Optimization
• GPS Integration
```

### ⭐ **Review Service**
```
⭐ Review Service
:8089
─────────────
• Ratings
• Comments
• Review Moderation
• Feedback System
```

## 🎯 **Elementos Especiais**

### 🔄 **Setas de Evento (Kafka)**
```
Serviço ~~~~> Kafka ~~~~> Outro Serviço
        (Async Event)
```

### 💾 **Conexões de Banco**
```
Serviço -.-.-> PostgreSQL
        (SQL Connection)
```

### ⚡ **Cache**
```
Serviço <--> Redis
       (Cache Read/Write)
```

## 🚀 **Dicas de Layout no Excalidraw**

1. **Comece de cima para baixo**
2. **Mantenha alinhamento horizontal**
3. **Use grid/snap para precisão**
4. **Agrupe elementos relacionados**
5. **Use cores consistentes**
6. **Adicione legendas explicativas**

**🎨 Com este template, você terá um diagrama profissional e completo no Excalidraw!**