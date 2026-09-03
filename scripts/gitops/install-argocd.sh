#!/usr/bin/env bash
# Installs Argo CD into the current kubectl context (the local Minikube cluster).
set -euo pipefail

ARGOCD_VERSION="${ARGOCD_VERSION:-v2.13.2}"
NAMESPACE="argocd"

command -v kubectl >/dev/null 2>&1 || { echo "kubectl not found in PATH" >&2; exit 1; }

kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

echo "Installing Argo CD $ARGOCD_VERSION into namespace '$NAMESPACE'..."
kubectl apply -n "$NAMESPACE" -f "https://raw.githubusercontent.com/argoproj/argo-cd/${ARGOCD_VERSION}/manifests/install.yaml"

echo "Waiting for Argo CD server to become ready (this can take a few minutes on first install)..."
kubectl -n "$NAMESPACE" wait --for=condition=available --timeout=300s deployment/argocd-server
kubectl -n "$NAMESPACE" wait --for=condition=available --timeout=300s deployment/argocd-repo-server

echo
echo "Argo CD is up. Next steps:"
echo "  1) Get the initial admin password:"
echo "     kubectl -n $NAMESPACE get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d; echo"
echo "  2) Port-forward the UI (in a separate terminal):"
echo "     kubectl -n $NAMESPACE port-forward svc/argocd-server 8080:443"
echo "     then open https://localhost:8080 (user: admin)"
echo "  3) Bootstrap the app-of-apps:"
echo "     kubectl apply -f gitops/argocd/root-app.yaml"
