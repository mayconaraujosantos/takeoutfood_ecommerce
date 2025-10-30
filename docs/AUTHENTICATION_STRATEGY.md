# 🔐 Estratégia de Autenticação/Autorização - iFood Clone

## 🎯 **Arquitetura Escolhida: Híbrida (Dedicado + Gateway)**

### 📋 **Resumo da Decisão**

- ✅ **Auth Service dedicado** (já implementado - :8081)
- ✅ **Validação no API Gateway** (já implementado - :8080)
- ✅ **JWT Tokens** para comunicação
- ✅ **Spring Security** em cada microsserviço

## 🏗️ **Fluxo de Autenticação Atual**

```
📱 Cliente
    ↓ (login/register)
🔐 Auth Service (:8081)
    ↓ (JWT Token)
🚪 API Gateway (:8080)
    ↓ (Token válido + User Info)
🏪 Microsserviços (Business Logic)
```

### 🔄 **Processo Detalhado**

1. **Login/Registro**

   ```
   POST /auth/login
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```

2. **Auth Service Response**

   ```
   {
     "token": "eyJhbGciOiJIUzI1NiIs...",
     "user": {
       "id": 123,
       "email": "user@example.com",
       "roles": ["USER", "CUSTOMER"]
     }
   }
   ```

3. **Requisições Subsequentes**

   ```
   GET /restaurants
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   ```

4. **API Gateway Validation**
   - Valida assinatura do JWT
   - Extrai informações do usuário
   - Injeta headers para microsserviços

5. **Microsserviço Recebe**

   ```
   Headers:
   X-User-ID: 123
   X-User-Email: user@example.com
   X-User-Roles: USER,CUSTOMER
   ```

## 🔧 **Implementação Técnica**

### 🔐 **Auth Service (:8081)**

**Responsabilidades:**

- ✅ Registro de usuários
- ✅ Login/logout
- ✅ Geração de JWT tokens
- ✅ Validação de credenciais
- ✅ Gerenciamento de perfis
- ✅ Reset de senha

**Tecnologias:**

```java
// Dependências principais
spring-boot-starter-security
spring-security-oauth2-jose
spring-boot-starter-data-jpa
```

### 🚪 **API Gateway (:8080)**

**Responsabilidades:**

- ✅ Validação de JWT em cada requisição
- ✅ Extração de claims do token
- ✅ Injeção de headers com dados do usuário
- ✅ Rate limiting por usuário
- ✅ Logging de acesso

**Implementação:**

```java
@Component
public class AuthFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                             GatewayFilterChain chain) {
        // 1. Extract JWT from Authorization header
        // 2. Validate JWT signature and expiration
        // 3. Extract user info from claims
        // 4. Add user headers to request
        // 5. Continue to target service
    }
}
```

### 🏪 **Business Services (:8082-8089)**

**Responsabilidades:**

- ✅ Receber headers com dados do usuário
- ✅ Aplicar autorização baseada em roles
- ✅ Log de ações do usuário
- ✅ Implementar business logic

**Implementação:**

```java
@RestController
public class RestaurantController {
    
    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants(
        @RequestHeader("X-User-ID") Long userId,
        @RequestHeader("X-User-Roles") String roles) {
        
        // Business logic with user context
    }
}
```

## 🔒 **Níveis de Segurança**

### 1️⃣ **Autenticação (Authentication)**

- **JWT Tokens** com assinatura HMAC/RSA
- **Expiração** configurável (ex: 24h)
- **Refresh Tokens** para renovação

### 2️⃣ **Autorização (Authorization)**

- **Role-Based Access Control (RBAC)**
- **Endpoint-level permissions**
- **Resource-level permissions**

### 3️⃣ **Roles Implementados**

```java
public enum UserRole {
    CUSTOMER,        // Cliente final
    RESTAURANT_OWNER, // Dono de restaurante
    DELIVERY_DRIVER,  // Entregador
    ADMIN,           // Administrador
    SUPPORT         // Suporte
}
```

## 📊 **Vantagens da Arquitetura Escolhida**

### ✅ **Para Desenvolvimento**

- **Controle total** sobre lógica de auth
- **Facilidade de debug** e troubleshooting
- **Customização** específica para iFood
- **Aprendizado** de conceitos de segurança

### ✅ **Para Performance**

- **Validação única** no Gateway
- **Headers pré-processados** para serviços
- **Cache de validação** de tokens
- **Redução de round-trips**

### ✅ **Para Escalabilidade**

- **Stateless authentication** com JWT
- **Horizontal scaling** do Auth Service
- **Load balancing** transparent
- **Microservice independence**

## 🔄 **Melhorias Futuras (Roadmap)**

### 📈 **Curto Prazo**

- [ ] **Refresh tokens** implementation
- [ ] **Password policy** enforcement
- [ ] **Account lockout** após tentativas
- [ ] **Audit logging** de ações

### 🚀 **Médio Prazo**

- [ ] **Two-factor authentication (2FA)**
- [ ] **Social login** (Google, Facebook)
- [ ] **JWT blacklisting** para logout
- [ ] **Rate limiting** por usuário

### 🎯 **Longo Prazo**

- [ ] **Migration to OAuth2/OIDC** (se necessário)
- [ ] **Integration with Keycloak** (se crescer muito)
- [ ] **Advanced RBAC** com permissions granulares
- [ ] **Single Sign-On (SSO)** entre apps

## 🛠️ **Implementação no Código**

### 🔐 **Auth Service Configuration**

```yaml
# auth-service/application.yml
jwt:
  secret: ${JWT_SECRET:mySecretKey}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

### 🚪 **Gateway Filter Configuration**

```yaml
# api-gateway/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**
        - id: protected-services
          uri: lb://user-service
          predicates:
            - Path=/api/**
          filters:
            - name: JWTAuthenticationFilter
```

### 🏪 **Service Security Configuration**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

## 🎉 **Conclusão**

A arquitetura híbrida escolhida é **ideal para o iFood Clone** porque:

1. ✅ **Balanceia complexidade** e funcionalidade
2. ✅ **Aproveita infraestrutura** já existente  
3. ✅ **Permite evolução** futura sem refactory completo
4. ✅ **Fornece aprendizado** de padrões reais da indústria
5. ✅ **Mantém performance** e escalabilidade

**🚀 Esta é a escolha certa para seu projeto!**
