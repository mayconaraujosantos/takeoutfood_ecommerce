#!/usr/bin/env bash
# Starts (or reuses) a local Kubernetes cluster on top of Podman via Minikube.
set -euo pipefail

CLUSTER_NAME="${MINIKUBE_PROFILE:-ifood-clone}"
CPUS="${MINIKUBE_CPUS:-8}"
# 8GB wasn't enough headroom for all 12 Spring Boot services plus infra
# (postgres/mongo/redis/kafka) and observability at once - the cgroup filled
# up and container creation started failing with "resource temporarily
# unavailable". Bump the default; override with MINIKUBE_MEMORY if your host
# has less to spare.
MEMORY="${MINIKUBE_MEMORY:-16384}"

command -v podman >/dev/null 2>&1 || { echo "podman not found in PATH" >&2; exit 1; }
command -v minikube >/dev/null 2>&1 || { echo "minikube not found in PATH - install it first: https://minikube.sigs.k8s.io/docs/start/" >&2; exit 1; }

podman version >/dev/null 2>&1 || { echo "podman machine/daemon not reachable - start it first (e.g. 'podman machine start')" >&2; exit 1; }

if minikube status -p "$CLUSTER_NAME" >/dev/null 2>&1; then
  echo "Minikube profile '$CLUSTER_NAME' is already running."
else
  echo "Starting Minikube profile '$CLUSTER_NAME' (driver=podman, cpus=$CPUS, memory=${MEMORY}MB)..."
  # --container-runtime=containerd: the docker runtime's node bootstrap tries to
  # rewrite/restart docker.service via systemd, which fails under rootless Podman
  # (no privilege to manage the host's docker.service). containerd's bootstrap
  # doesn't need that step, so it works with the podman driver in rootless mode.
  minikube start \
    -p "$CLUSTER_NAME" \
    --driver=podman \
    --container-runtime=containerd \
    --cpus="$CPUS" \
    --memory="$MEMORY" \
    --addons=ingress,metrics-server
fi

kubectl config use-context "$CLUSTER_NAME"
echo "Cluster ready. Current context: $(kubectl config current-context)"
