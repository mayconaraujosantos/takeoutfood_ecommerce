package com.ifoodclone.auth.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.ifoodclone.auth.dto.DevTokenDto;
import com.ifoodclone.auth.service.JwtService;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile({ "dev", "local" })
@Tag(name = "Development", description = "Endpoints exclusivos para desenvolvimento")
public class DevController {

    private final JwtService jwtService;

    @PostMapping("/token")
    @Operation(summary = "Gerar Token de Desenvolvimento", description = "Gera um JWT com expiração longa (30 dias) para uso em desenvolvimento e testes")
    @ApiResponse(responseCode = "200", description = "Token gerado com sucesso")
    public ResponseEntity<DevTokenDto> generateDevToken(
            @RequestParam(defaultValue = "developer") String username,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(defaultValue = "30") int validityDays) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("role", role);
        claims.put("type", "DEV_TOKEN");
        claims.put("team", "development");

        // Token válido por X dias (padrão 30 dias)
        String token = jwtService.generateLongLivedToken(claims, validityDays);

        DevTokenDto response = DevTokenDto.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .role(role)
                .validityDays(validityDays)
                .expiresAt(LocalDateTime.now().plusDays(validityDays))
                .usage("Authorization: Bearer " + token)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/teams")
    @Operation(summary = "Gerar Tokens para Equipes", description = "Gera tokens específicos para diferentes equipes (frontend, mobile, qa, etc.)")
    public ResponseEntity<Map<String, DevTokenDto>> generateTeamTokens() {

        Map<String, DevTokenDto> teamTokens = new HashMap<>();

        // Token para Frontend
        teamTokens.put("frontend", createTeamToken("frontend-dev", "CUSTOMER", "frontend"));

        // Token para Mobile
        teamTokens.put("mobile", createTeamToken("mobile-dev", "CUSTOMER", "mobile"));

        // Token para QA
        teamTokens.put("qa", createTeamToken("qa-tester", "ADMIN", "qa"));

        // Token para Backend
        teamTokens.put("backend", createTeamToken("backend-dev", "ADMIN", "backend"));

        return ResponseEntity.ok(teamTokens);
    }

    @GetMapping("/token/validate")
    @Operation(summary = "Validar Token", description = "Valida se um token está funcionando e retorna informações sobre ele")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        Map<String, Object> result = new HashMap<>();
        result.put("valid", jwtService.isTokenValid(token));
        result.put("subject", jwtService.extractUsername(token));
        result.put("expiresAt", jwtService.extractExpiration(token));
        result.put("remainingTime", jwtService.getTokenRemainingTime(token) + " seconds");

        return ResponseEntity.ok(result);
    }

    private DevTokenDto createTeamToken(String username, String role, String team) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("role", role);
        claims.put("type", "TEAM_TOKEN");
        claims.put("team", team);

        String token = jwtService.generateLongLivedToken(claims, 30);

        return DevTokenDto.builder()
                .token(token)
                .type("Bearer")
                .username(username)
                .role(role)
                .validityDays(30)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .usage("Authorization: Bearer " + token)
                .build();
    }
}