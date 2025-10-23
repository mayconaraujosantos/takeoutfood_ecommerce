#!/bin/bash

# Kubernetes Setup Helper Script
# Para configurar diferentes tipos de clusters Kubernetes

set -e

KUBE_DIR="$HOME/.kube"
CONFIG_FILE="$KUBE_DIR/config"

echo "🚀 Kubernetes Setup Helper"
echo "=========================="
echo ""

# Função para instalar minikube
install_minikube() {
    echo "📦 Instalando Minikube..."
    curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
    chmod +x minikube
    mkdir -p ~/.local/bin
    mv minikube ~/.local/bin/
    echo "✅ Minikube instalado!"
}

# Função para instalar kind
install_kind() {
    echo "📦 Instalando Kind..."
    curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
    chmod +x ./kind
    mkdir -p ~/.local/bin
    mv ./kind ~/.local/bin/
    echo "✅ Kind instalado!"
}

# Função para configurar minikube
setup_minikube() {
    echo "🔧 Configurando Minikube..."
    minikube start --driver=docker
    kubectl config use-context minikube
    echo "✅ Minikube configurado e cluster iniciado!"
}

# Função para configurar kind
setup_kind() {
    echo "🔧 Configurando Kind..."
    cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: ifood-cluster
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
EOF
    kubectl config use-context kind-ifood-cluster
    echo "✅ Kind configurado com cluster multi-node!"
}

# Menu principal
echo "Escolha uma opção:"
echo "1) Instalar e configurar Minikube (recomendado para desenvolvimento)"
echo "2) Instalar e configurar Kind (recomendado para testes)"
echo "3) Configurar cluster remoto (AWS EKS, GKE, AKS)"
echo "4) Verificar configuração atual"
echo "5) Sair"
echo ""

read -p "Digite sua opção (1-5): " choice

case $choice in
    1)
        if ! command -v minikube &> /dev/null; then
            install_minikube
        fi
        setup_minikube
        ;;
    2)
        if ! command -v kind &> /dev/null; then
            install_kind
        fi
        setup_kind
        ;;
    3)
        echo "📋 Para configurar um cluster remoto:"
        echo "   - AWS EKS: aws eks update-kubeconfig --region <region> --name <cluster-name>"
        echo "   - GKE: gcloud container clusters get-credentials <cluster-name> --zone <zone>"
        echo "   - AKS: az aks get-credentials --resource-group <rg> --name <cluster-name>"
        echo ""
        echo "💡 Após executar o comando apropriado, execute 'kubectl config get-contexts' para verificar."
        ;;
    4)
        echo "🔍 Configuração atual:"
        kubectl config current-context 2>/dev/null || echo "Nenhum contexto ativo"
        echo ""
        echo "📋 Contextos disponíveis:"
        kubectl config get-contexts 2>/dev/null || echo "Nenhum contexto configurado"
        ;;
    5)
        echo "👋 Saindo..."
        exit 0
        ;;
    *)
        echo "❌ Opção inválida!"
        exit 1
        ;;
esac