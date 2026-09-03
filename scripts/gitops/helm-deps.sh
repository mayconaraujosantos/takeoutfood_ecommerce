#!/usr/bin/env bash
# Resolves the Helm chart dependencies (Bitnami / prometheus-community / Grafana)
# for the umbrella charts and writes Chart.lock + charts/ so Argo CD syncs against
# pinned, already-resolved versions instead of re-resolving on every sync.
set -euo pipefail

cd "$(dirname "$0")/../.."

command -v helm >/dev/null 2>&1 || { echo "helm not found in PATH - install it first: https://helm.sh/docs/intro/install/" >&2; exit 1; }

CHARTS=(
  gitops/apps/infra
  gitops/apps/observability
)

for chart in "${CHARTS[@]}"; do
  echo "==> helm dependency update ${chart}"
  helm dependency update "$chart"
done

echo "Chart.lock/charts/ updated. Commit them so Argo CD syncs against the pinned versions."
