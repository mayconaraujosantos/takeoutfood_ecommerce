# Deploy local via Argo CD (GitOps) usando Podman

Este guia descreve o caminho de deploy **novo**, baseado em GitOps com Argo CD,
rodando localmente em cima do **Podman** (via Minikube). Ele não substitui o
`docker-compose` nem o deploy no Railway — é uma alternativa para quem quer
exercitar o mesmo fluxo de um cluster Kubernetes gerenciado por Argo CD.

## Visão geral

```
gitops/
  argocd/root-app.yaml          # Application raiz (App-of-Apps) - aplicado manualmente 1x
  charts/microservice/          # Helm chart genérico usado pelos 12 microsserviços
  apps/
    infra/                      # Postgres, MongoDB, Redis, Kafka (charts Bitnami)
    observability/              # otel-collector, Jaeger, kube-prometheus-stack, Loki, Promtail
    services/values/            # 1 values.yaml por microsserviço
  environments/local/applications/
    infra.yaml                  # Application -> gitops/apps/infra
    observability.yaml          # Application -> gitops/apps/observability
    services-appset.yaml        # ApplicationSet -> 1 Application por values/*.yaml
```

Depois do bootstrap inicial (`kubectl apply -f gitops/argocd/root-app.yaml`), o
Argo CD passa a observar `gitops/environments/local/applications/` sozinho:
qualquer mudança commitada nesse diretório (ou nos charts/values que ele
referencia) é sincronizada automaticamente (`selfHeal: true`).

## Pré-requisitos

- Podman já instalado (`podman version`).
- [Minikube](https://minikube.sigs.k8s.io/docs/start/) e `kubectl` no PATH.
- [Helm](https://helm.sh/docs/intro/install/) no PATH (só é necessário para
  rodar `helm dependency update` uma vez nos charts `gitops/apps/infra` e
  `gitops/apps/observability` antes do primeiro sync do Argo CD, e para
  validar com `helm lint`/`helm template`).
- Acesso de saída à internet (para baixar os charts Bitnami/prometheus-community/grafana
  e a imagem base `eclipse-temurin` durante o build).

## Passo a passo

### 1. Subir o cluster local

```bash
make gitops-cluster-up
# equivalente a: minikube start --driver=podman --cpus=4 --memory=8192 --addons=ingress,metrics-server
```

### 2. Instalar o Argo CD

```bash
make gitops-argocd-install
```

Pegue a senha inicial do `admin` e abra a UI (comandos impressos no final do
script, resumidos aqui):

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d; echo
kubectl -n argocd port-forward svc/argocd-server 8080:443
# https://localhost:8080  (user: admin)
```

### 3. Resolver as dependências dos charts guarda-chuva (uma vez)

```bash
make gitops-helm-deps
```

Isso roda `helm dependency update` em `gitops/apps/infra` e
`gitops/apps/observability`, gerando `Chart.lock` + `charts/*.tgz`. Faça
commit desses arquivos para que o Argo CD sincronize contra as versões já
resolvidas em vez de re-resolver a cada sync.

> As versões em `Chart.yaml` são faixas (`^x.y.z`) porque os charts da
> Bitnami/prometheus-community/Grafana evoluem rápido. Se `make
> gitops-helm-deps` falhar por causa de uma versão, ajuste a faixa no
> `Chart.yaml` correspondente e rode de novo.

### 4. Build + load das imagens (sem precisar de registry)

```bash
make gitops-build-images   # podman build para os 12 serviços
make gitops-load-images    # minikube image load para dentro do cluster
```

### 5. Bootstrap do GitOps

```bash
make gitops-bootstrap
# equivalente a: kubectl apply -f gitops/argocd/root-app.yaml
```

Acompanhe o sync na UI do Argo CD (`ifood-root` → `ifood-infra`,
`ifood-observability`, e um `svc-*` por microsserviço). O primeiro sync
completo (baixar os charts de terceiros, subir Postgres/Mongo/Redis/Kafka,
Prometheus Operator, Grafana, Loki, Jaeger e os 12 serviços) pode levar
alguns minutos.

Atalho para rodar os passos 1, 2, 4 e 5 de uma vez (ainda requer o passo 3
manual na primeira execução):

```bash
make gitops-up
```

### 6. Acessar os serviços

```bash
kubectl -n ifood get pods
kubectl -n ifood port-forward svc/api-gateway 8080:8080
kubectl -n observability port-forward svc/kube-prometheus-stack-grafana 3000:80
kubectl -n observability port-forward svc/jaeger 16686:16686
```

| UI | Endpoint local (via port-forward) | Credenciais |
|---|---|---|
| Argo CD | https://localhost:8080 | admin / (senha do passo 2) |
| Grafana | http://localhost:3000 | admin / ifood_grafana_pass |
| Jaeger | http://localhost:16686 | - |
| API Gateway | http://localhost:8080 (após port-forward do serviço) | - |

## Telemetria e rastreabilidade

- Todos os 12 microsserviços têm `micrometer-tracing-bridge-otel` +
  `opentelemetry-exporter-otlp` + `micrometer-registry-prometheus` herdados do
  `pom.xml` raiz (`dependencyManagement`/`dependencies` comuns) - não precisa
  adicionar dependência por serviço.
- Cada `Deployment` gerado pelo chart injeta `OTEL_SERVICE_NAME`,
  `OTEL_EXPORTER_OTLP_ENDPOINT` (apontando para
  `otel-collector.observability.svc.cluster.local:4318`) e
  `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` automaticamente
  (`gitops/charts/microservice/templates/deployment.yaml`).
- O `otel-collector` recebe os traces via OTLP e reexporta para o Jaeger; o
  Jaeger também aceita OTLP diretamente (`COLLECTOR_OTLP_ENABLED=true`), então
  em `docker-compose` (sem o collector) os serviços continuam funcionando -
  o endpoint padrão sem override é `http://jaeger:4318`.
- Logs estruturados em JSON (`logback-spring.xml`, com `traceId`/`spanId`/
  `correlationId` em todo log) existem agora nos 12 serviços - antes só
  `auth-service` tinha. Correlation ID (`X-Correlation-ID`) é gerado/propagado
  via interceptor nos 9 serviços REST síncronos (`auth`, `user`, `restaurant`,
  `menu`, `order`, `payment`, `notification`, `delivery`, `review`);
  `config-server`/`service-discovery` (sem rotas `/api/**`) e `api-gateway`
  (WebFlux, já tem seu próprio `LoggingFilter`) ficaram de fora desse
  interceptor específico, mas exportam métricas e traces normalmente.
- Métricas Prometheus continuam em `/actuator/prometheus` (`ServiceMonitor`
  por serviço, criado pelo próprio chart) - sem depender do `otel-collector`.

## Adicionando um 13º microsserviço

1. Crie `gitops/apps/services/values/novo-service.yaml` seguindo o padrão dos
   existentes (`nameOverride`, `image.repository`, `service.port/targetPort`,
   `env`).
2. Garanta que `novo-service/Dockerfile` existe e builda a partir da raiz do
   monorepo (mesmo padrão dos outros - veja `scripts/gitops/build-images.sh`,
   adicione o nome na lista `SERVICES`).
3. Commit + push. O `ApplicationSet` (`services-appset.yaml`) detecta o novo
   arquivo no próximo poll do generator `git.files` e cria a Application
   `svc-novo-service` sozinho.

## Troubleshooting

- **`ServiceMonitor` não aparece no Prometheus**: confirme que a Application
  `ifood-observability` sincronizou primeiro (sync-wave `0` vs `1` dos
  serviços) - o CRD `ServiceMonitor` só existe depois do
  `kube-prometheus-stack` subir.
- **`helm.valueFiles` com `../../` recusado pelo Argo CD**: versões antigas do
  Argo CD restringem `valueFiles` fora do diretório do chart. Se acontecer,
  troque `services-appset.yaml` para uma Application multi-source (`sources`
  com `$values` apontando pra `gitops/apps/services/values`) - ver
  [docs do Argo CD sobre Helm values de outro source](https://argo-cd.readthedocs.io/en/stable/user-guide/multiple_sources/#helm-value-files-from-external-git-repository).
- **Pod em `ImagePullBackOff`**: rode `make gitops-load-images` de novo - o
  Minikube perde as imagens carregadas manualmente se o cluster for recriado.
- **`ifood-infra`/`ifood-observability` travados em `OutOfSync`**: rode
  `helm dependency update` no chart correspondente (passo 3) e faça commit do
  `Chart.lock`/`charts/` gerado, ou ajuste a versão no `Chart.yaml` se a
  resolução falhar.
