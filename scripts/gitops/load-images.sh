#!/usr/bin/env bash
# Loads the locally-built images into the Minikube node so pods can pull them
# without needing a registry (imagePullPolicy: IfNotPresent in the chart).
set -euo pipefail

CLUSTER_NAME="${MINIKUBE_PROFILE:-ifood-clone}"

SERVICES=(
  config-server
  service-discovery
  api-gateway
  auth-service
  user-service
  restaurant-service
  menu-service
  order-service
  payment-service
  notification-service
  delivery-service
  review-service
)

command -v minikube >/dev/null 2>&1 || { echo "minikube not found in PATH" >&2; exit 1; }

for svc in "${SERVICES[@]}"; do
  echo "==> Loading localhost/ifood-clone/${svc}:local into Minikube profile '${CLUSTER_NAME}'"
  minikube image load "localhost/ifood-clone/${svc}:local" -p "$CLUSTER_NAME"
done

echo "All images loaded."
