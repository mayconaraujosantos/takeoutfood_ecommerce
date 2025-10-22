package com.ifoodclone.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DevTokenDto {
    private String token;
    private String type;
    private String username;
    private String role;
    private Integer validityDays;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    private String usage;

    // Informações adicionais para desenvolvimento
    private String swaggerInstructions = "1. Copie o token acima\n2. Clique em 'Authorize' no Swagger\n3. Cole: Bearer {seu_token}\n4. Clique em 'Authorize'";
}