# 🔐 Auth Service - Collection do Bruno API Client

Esta collection contém todas as requisições para testar o **Auth Service** do projeto TakeoutFood E-commerce.

## 📋 **Endpoints Disponíveis**

### **🟢 Endpoints Públicos (sem autenticação)**
1. **Health Check** - Verificar status do serviço
2. **Register Customer** - Registrar cliente
3. **Register Restaurant Owner** - Registrar dono de restaurante
4. **Login** - Fazer login
5. **Refresh Token** - Renovar token de acesso
6. **Password Reset Request** - Solicitar reset de senha

### **🔒 Endpoints Protegidos (requer autenticação)**
7. **Get Profile** - Obter perfil do usuário
8. **Change Password** - Alterar senha
9. **Logout** - Fazer logout
10. **Logout All Devices** - Logout de todos dispositivos

### **❌ Casos de Teste de Erro**
11. **Invalid Login** - Teste de login com senha incorreta
12. **Invalid Registration** - Teste de validação de dados
13. **Unauthorized Access** - Teste de acesso sem token válido

---

## 🚀 **Como Usar**

### **1. Configuração Inicial**
- Importe a collection no Bruno API Client
- Configure o ambiente (dev/prod) nas variáveis
- Execute os endpoints na ordem sugerida

### **2. Fluxo Básico de Teste**
```bash
1. Health Check          # Verificar se serviço está rodando
2. Register Customer     # Criar usuário de teste
3. Login                 # Fazer login (tokens são salvos automaticamente)
4. Get Profile          # Verificar dados do usuário
5. Change Password      # (Opcional) Testar alteração de senha
6. Logout               # Encerrar sessão
```

### **3. Variáveis Automáticas**
As seguintes variáveis são configuradas automaticamente:
- `accessToken` - Token de acesso (salvo após login)
- `refreshToken` - Token de refresh (salvo após login)
- `userId` - ID do usuário (salvo após registro/login)

---

## 🔧 **Configurações dos Ambientes**

### **Desenvolvimento**
```
baseUrl: http://localhost:8081
```

### **Produção**
```
baseUrl: https://api.takeoutfood.com
```

---

## 📊 **Estrutura de Resposta Padrão**

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": {
    // Dados específicos da resposta
  },
  "timestamp": "2025-11-03T22:45:00Z"
}
```

### **Resposta de Erro**
```json
{
  "success": false,
  "message": "Descrição do erro",
  "error": "Detalhes técnicos do erro",
  "timestamp": "2025-11-03T22:45:00Z"
}
```

---

## 🧪 **Testes Automáticos**

Cada endpoint inclui testes automáticos que verificam:
- ✅ Status code correto
- ✅ Estrutura da resposta
- ✅ Dados obrigatórios presentes
- ✅ Tokens válidos (quando aplicável)

---

## 👥 **Tipos de Usuário**

### **CUSTOMER (Cliente)**
```json
{
  "email": "customer@example.com",
  "password": "password123",
  "firstName": "João",
  "lastName": "Silva",
  "role": "CUSTOMER"
}
```

### **RESTAURANT_OWNER (Dono do Restaurante)**
```json
{
  "email": "restaurant@example.com",
  "password": "password123",
  "firstName": "Maria",
  "lastName": "Santos",
  "role": "RESTAURANT_OWNER"
}
```

### **DELIVERY_DRIVER (Entregador)**
```json
{
  "email": "driver@example.com",
  "password": "password123",
  "firstName": "Carlos",
  "lastName": "Lima",
  "role": "DELIVERY_DRIVER"
}
```

---

## 🔐 **Autenticação JWT**

O serviço usa **JWT (JSON Web Tokens)** com:
- **Access Token**: Expira em 15 minutos
- **Refresh Token**: Expira em 7 dias
- **Bearer Token**: Formato `Authorization: Bearer <token>`

---

## 🛠️ **Requisitos**

- **Auth Service** rodando na porta `8081`
- **PostgreSQL** para persistência de dados
- **Redis** para cache de tokens
- **Bruno API Client** instalado

---

## 📝 **Logs e Debugging**

- Logs estruturados em JSON
- Trace IDs para rastreamento distribuído
- Métricas de performance integradas
- Health checks automáticos

---

**🎯 Collection criada para facilitar testes e desenvolvimento do Auth Service!**