#!/usr/bin/env bash
# Builds all 12 microservice images locally with Podman, tagged to match
# gitops/apps/services/values/*.yaml (image.repository/tag).
set -euo pipefail

cd "$(dirname "$0")/../.."

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

command -v podman >/dev/null 2>&1 || { echo "podman not found in PATH" >&2; exit 1; }

for svc in "${SERVICES[@]}"; do
  echo "==> Building localhost/ifood-clone/${svc}:local"
  podman build -f "${svc}/Dockerfile" -t "localhost/ifood-clone/${svc}:local" .
done

echo "All images built. Run scripts/gitops/load-images.sh to load them into Minikube."
