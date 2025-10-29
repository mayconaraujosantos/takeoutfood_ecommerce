# 🎨 Guia para Criar Diagrama iFood Clone no Excalidraw

## 📋 **Instruções para Excalidraw**

### 🔗 **Link do Excalidraw**
<https://excalidraw.com/>

## 🏗️ **Layout Sugerido para o Diagrama**

### 📱 **Camada Cliente (Topo)**
```
┌─────────────┐    ┌─────────────┐
│ 📱 Mobile   │    │ 🌐 Web App  │
│    App      │    │             │
└─────────────┘    └─────────────┘
       │                   │
       └───────────┬───────┘
                   │
```

### 🚪 **API Gateway (Centro Superior)**
```
           ┌─────────────────┐
           │ 🚪 API Gateway  │
           │     :8080       │
           └─────────────────┘
                   │
```

### ⚙️ **Infraestrutura (Lado Esquerdo)**
```
┌─────────────────┐    ┌─────────────────┐
│ ⚙️ Config       │    │ 🔍 Service      │
│   Server        │    │   Discovery     │
│   :8888         │    │   :8761         │
└─────────────────┘    └─────────────────┘
```

### 🔐 **Microsserviços (Centro)**
```
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ 🔐 Auth     │ │ 👤 User     │ │ 🏪 Restaurant│
│   :8081     │ │   :8082     │ │   :8083     │
└─────────────┘ └─────────────┘ └─────────────┘

┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ 📋 Menu     │ │ 🛒 Order    │ │ 💳 Payment  │
│   :8084     │ │   :8085     │ │   :8086     │
└─────────────┘ └─────────────┘ └─────────────┘

┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ 📢 Notify   │ │ 🚚 Delivery │ │ ⭐ Review   │
│   :8087     │ │   :8088     │ │   :8089     │
└─────────────┘ └─────────────┘ └─────────────┘
```

### 🗄️ **Bancos de Dados (Parte Inferior)**
```
┌─────────────────┐ ┌─────────────────┐
│ 🐘 PostgreSQL   │ │ 🍃 MongoDB      │
│    :5433        │ │    :27018       │
└─────────────────┘ └─────────────────┘

┌─────────────────┐ ┌─────────────────┐
│ ⚡ Redis        │ │ 📨 Kafka        │
│    :6380        │ │    :9092        │
└─────────────────┘ └─────────────────┘
```

## 🎨 **Instruções Detalhadas para Excalidraw**

### 1️⃣ **Criando as Caixas**
- **Ferramenta**: Rectangle (retângulo)
- **Cores sugeridas**:
  - 🔵 **Azul claro** (#E3F2FD) - Clientes
  - 🟣 **Roxo claro** (#F3E5F5) - Infraestrutura
  - 🟢 **Verde claro** (#E8F5E8) - Microsserviços
  - 🟠 **Laranja claro** (#FFF3E0) - Bancos de dados

### 2️⃣ **Adicionando Texto**
- **Ferramenta**: Text
- **Formato**: 
  ```
  🔐 Auth Service
  :8081
  ```

### 3️⃣ **Conectando com Setas**
- **Ferramenta**: Arrow
- **Tipos de conexão**:
  - **Setas sólidas** (→) - Comunicação HTTP
  - **Setas tracejadas** (⇢) - Conexões de banco
  - **Setas duplas** (↔) - Comunicação bidirecional

### 4️⃣ **Layout de Conexões**

#### **Clientes → Gateway**
```
Mobile App  ──→  API Gateway  ←── Web App
```

#### **Gateway → Serviços**
```
API Gateway ──→ Auth Service
            ──→ User Service  
            ──→ Restaurant Service
            ──→ Menu Service
            ──→ Order Service
            ──→ Payment Service
            ──→ Notification Service
            ──→ Delivery Service
            ──→ Review Service
```

#### **Infraestrutura → Serviços**
```
Config Server ⇢ Todos os serviços
Service Discovery ⇢ Todos os serviços
```

#### **Serviços → Bancos**
```
Auth, User, Restaurant, Menu,
Order, Payment, Delivery ⇢ PostgreSQL

Notification, Review ⇢ MongoDB

Auth, Gateway ⇢ Redis

Todos os serviços ⇢ Kafka
```

## 🎯 **Dicas de Design**

### 📐 **Disposição Espacial**
1. **Coloque clientes no topo**
2. **Gateway no centro superior**
3. **Infraestrutura nas laterais**
4. **Microsserviços no centro**
5. **Bancos de dados na parte inferior**

### 🎨 **Cores e Estilos**
- **Background**: Branco (#FFFFFF)
- **Borders**: 2px, cor escura
- **Text**: 14px, negrito para nomes
- **Arrows**: 3px de espessura

### 📝 **Labels Importantes**
- **Portas** em cada serviço
- **Tipos de conexão** (HTTP, TCP, etc.)
- **Protocolos** (REST, gRPC, Kafka)

## 📊 **Informações para Incluir**

### 🔍 **Por Serviço**
```
Nome do Serviço
Porta
Responsabilidade
Banco de Dados
```

### 🔗 **Por Conexão**
```
Tipo (HTTP/TCP/Kafka)
Protocolo (REST/gRPC/Event)
Direção (unidirecional/bidirecional)
```

## 🚀 **Template Pronto**

Você pode usar este template como base:

```
📱 Mobile & 🌐 Web
        ↓
   🚪 API Gateway
        ↓
┌────────────────────────────┐
│     Microsserviços         │
│ 🔐🧑🏪📋🛒💳📢🚚⭐        │
└────────────────────────────┘
        ↓
┌────────────────────────────┐
│    Bancos de Dados         │
│    🐘🍃⚡📨              │
└────────────────────────────┘
```

## 🎁 **Arquivo Exemplo**

Após criar, você pode:
1. **Exportar como PNG** para documentação
2. **Compartilhar link público** do Excalidraw
3. **Salvar como .excalidraw** para edição futura

**🎯 Com essas instruções, você conseguirá criar um diagrama profissional e claro no Excalidraw!**