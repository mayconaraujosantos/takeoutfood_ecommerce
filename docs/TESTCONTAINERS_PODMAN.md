# Testcontainers com Podman

Este guia resolve problemas comuns ao executar testes de integracao com Testcontainers em ambiente local usando Podman.

## Sintomas comuns

- Falha ao detectar Docker environment.
- Timeout ao iniciar containers de teste.
- Erro relacionado ao Ryuk em ambiente rootless.

## Pre-requisitos

- Podman instalado.
- Socket do Podman ativo para o usuario atual.

## Inicializacao do socket

```bash
systemctl --user enable --now podman.socket
systemctl --user status podman.socket
```

Alternativa sem systemd:

```bash
podman system service --time=0 unix:///run/user/$(id -u)/podman/podman.sock
```

## Execucao recomendada dos testes

Use o script do repositorio para configurar variaveis necessarias:

```bash
bash scripts/run-tests-podman.sh ./mvnw -B -ntp -pl auth-service -am failsafe:integration-test failsafe:verify
```

### Smoke integration por servico

```bash
bash scripts/run-tests-podman.sh ./mvnw -B -ntp -pl user-service -am failsafe:integration-test failsafe:verify
bash scripts/run-tests-podman.sh ./mvnw -B -ntp -pl order-service -am failsafe:integration-test failsafe:verify
```

## Variaveis aplicadas pelo script

- `DOCKER_HOST=unix:///run/user/<uid>/podman/podman.sock`
- `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`
- `TESTCONTAINERS_RYUK_DISABLED=true`
- `TESTCONTAINERS_CHECKS_DISABLE=true`
- `TESTCONTAINERS_HOST_OVERRIDE=localhost`

## Observacoes

- No CI do GitHub Actions, mantenha a execucao padrao (Docker no runner).
- Esta configuracao e para ambiente local com Podman.
