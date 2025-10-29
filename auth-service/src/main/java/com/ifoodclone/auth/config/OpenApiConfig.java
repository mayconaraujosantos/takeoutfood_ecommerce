package com.ifoodclone.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("iFood Clone - Auth Service API")
                        .description("API de Autenticação e Autorização do iFood Clone")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe iFood Clone")
                                .email("dev@ifoodclone.com")
                                .url("https://ifoodclone.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("🔐 **Autenticação JWT Bearer Token**\n\n" +
                        "**Para Desenvolvimento:**\n" +
                        "1. Chame `POST /api/dev/token` para gerar token de 30 dias\n" +
                        "2. Copie o token retornado\n" +
                        "3. Clique em '🔒 Authorize' acima\n" +
                        "4. Cole: `Bearer {seu_token}`\n" +
                        "5. Clique 'Authorize'\n\n" +
                        "**Para Produção:**\n" +
                        "- Use `POST /api/auth/login` para obter token (24h)\n" +
                        "- Ou `POST /api/auth/refresh` para renovar\n\n" +
                        "**Formato:** `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`");
    }
}