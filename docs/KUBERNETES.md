# 🚀 Kubernetes Setup - iFood Clone

## 📋 Pré-requisitos

- Docker instalado e rodando
- kubectl instalado (já configurado ✅)
- Pelo menos 4GB RAM disponível
- Pelo menos 20GB espaço em disco

## ⚙️ Configuração Inicial

### 1. Configurar Cluster Kubernetes

Execute o script de configuração:

```bash
./k8s-setup.sh
```

Ou use o Makefile:

```bash
make k8s-setup
```

**Opções disponíveis:**

1. **Minikube** (recomendado para desenvolvimento)
   - Mais leve e simples
   - Ideal para desenvolvimento local
   - Suporte completo a todos os recursos K8s

2. **Kind** (recomendado para testes)
   - Clusters multi-node
   - Mais próximo de ambientes de produção
   - Melhor para testes de integração

### 2. Verificar Configuração

```bash
# Verificar contexto atual
kubectl config current-context

# Listar todos os contextos
kubectl config get-contexts

# Verificar status do cluster
make k8s-status
```

## 🚢 Deploy dos Microserviços

### Deploy Completo
```bash
make k8s-deploy
```

### Deploy Individual (exemplo)
```bash
kubectl apply -f k8s/deployments/auth-service.yaml
```

### Verificar Status
```bash
# Verificar pods
kubectl get pods

# Verificar services
kubectl get services

# Logs de um pod específico
kubectl logs -f deployment/auth-service
```

## 🔧 Comandos Úteis

### Gerenciamento do Cluster

```bash
# Status completo
make k8s-status

# Deletar todos os recursos
make k8s-delete

# Reiniciar cluster (minikube)
minikube stop && minikube start

# Reiniciar cluster (kind)
kind delete cluster --name ifood-cluster
./k8s-setup.sh  # Escolher opção 2
```

### Port Forwarding para Desenvolvimento

```bash
# Auth Service
kubectl port-forward service/auth-service 8081:8081

# API Gateway
kubectl port-forward service/api-gateway 8080:8080

# Service Discovery
kubectl port-forward service/service-discovery 8761:8761
```

### Debugging

```bash
# Descrever um resource
kubectl describe pod <pod-name>

# Entrar em um pod
kubectl exec -it <pod-name> -- /bin/bash

# Verificar logs com follow
kubectl logs -f <pod-name>

# Verificar eventos do cluster
kubectl get events --sort-by=.metadata.creationTimestamp
```

## 📁 Estrutura dos Manifests

```
k8s/
├── deployments/     # Deployments dos microserviços
├── services/        # Services (ClusterIP, NodePort, LoadBalancer)
├── configmaps/      # ConfigMaps para configurações
├── secrets/         # Secrets para senhas e certificados
└── ingress/         # Ingress para exposição externa
```

## 🔑 Configuração de Secrets

Para configurações sensíveis (senhas, certificados):

```bash
# Criar secret para database
kubectl create secret generic db-secret \
  --from-literal=username=ifood_user \
  --from-literal=password=ifood_pass

# Criar secret para JWT
kubectl create secret generic jwt-secret \
  --from-literal=secret=your-jwt-secret-key
```

## 🌐 Acesso aos Serviços

### Minikube

```bash
# Obter IP do minikube
minikube ip

# Abrir dashboard
minikube dashboard

# Usar tunnel para LoadBalancers
minikube tunnel
```

### Kind

```bash
# Port forwarding é necessário para acesso externo
kubectl port-forward service/api-gateway 8080:8080
```

## 📊 Monitoramento

### Dashboard Kubernetes
```bash
# Instalar dashboard (opcional)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Criar usuário admin (development only)
kubectl create serviceaccount dashboard-admin-sa
kubectl create clusterrolebinding dashboard-admin-sa --clusterrole=cluster-admin --serviceaccount=default:dashboard-admin-sa

# Obter token
kubectl create token dashboard-admin-sa
```

## 🚨 Troubleshooting

### Problemas Comuns

1. **Pod fica em Pending**
   ```bash
   kubectl describe pod <pod-name>
   # Verificar recursos disponíveis e node selectors
   ```

2. **ImagePullBackOff**
   ```bash
   # Construir imagem localmente (minikube)
   eval $(minikube docker-env)
   docker build -t ifood-clone/auth-service:latest auth-service/
   ```

3. **Service não responde**
   ```bash
   kubectl get endpoints
   kubectl describe service <service-name>
   ```

4. **ConfigMap não carregado**
   ```bash
   kubectl get configmap
   kubectl describe configmap ifood-config
   ```

## 🔄 Switches Between Contexts

```bash
# Listar contextos
kubectl config get-contexts

# Trocar para minikube
kubectl config use-context minikube

# Trocar para kind
kubectl config use-context kind-ifood-cluster

# Trocar para cluster remoto (exemplo)
kubectl config use-context my-remote-cluster
```

---

## 🤝 Comandos do Makefile Relacionados

| Comando | Descrição |
|---------|-----------|
| `make k8s-setup` | Configurar cluster K8s |
| `make k8s-deploy` | Deploy completo |
| `make k8s-delete` | Remover todos os recursos |
| `make k8s-status` | Status do cluster |

Para mais comandos: `make help`