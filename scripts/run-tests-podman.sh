#!/usr/bin/env bash
set -euo pipefail

# Usage examples:
#   bash scripts/run-tests-podman.sh ./mvnw -B -ntp -pl auth-service -am failsafe:integration-test failsafe:verify
#   bash scripts/run-tests-podman.sh ./mvnw -B -ntp -DskipITs=true test

if [[ $# -eq 0 ]]; then
  echo "Usage: bash scripts/run-tests-podman.sh <command ...>"
  exit 1
fi

if ! command -v podman >/dev/null 2>&1; then
  echo "Error: podman not found in PATH."
  exit 1
fi

if [[ -z "${XDG_RUNTIME_DIR:-}" ]]; then
  export XDG_RUNTIME_DIR="/run/user/$(id -u)"
fi

PODMAN_SOCKET_PATH="${XDG_RUNTIME_DIR}/podman/podman.sock"

if [[ ! -S "${PODMAN_SOCKET_PATH}" ]]; then
  echo "Podman socket not found at ${PODMAN_SOCKET_PATH}."
  echo "Start it with: systemctl --user enable --now podman.socket"
  echo "Or run once: podman system service --time=0 unix://${PODMAN_SOCKET_PATH}"
  exit 1
fi

# Testcontainers + Podman (rootless) defaults.
export DOCKER_HOST="unix://${PODMAN_SOCKET_PATH}"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE="/var/run/docker.sock"
export TESTCONTAINERS_RYUK_DISABLED="true"
export TESTCONTAINERS_CHECKS_DISABLE="true"

# Optional stability tweak for Podman networking.
export TESTCONTAINERS_HOST_OVERRIDE="localhost"

echo "Using Podman socket: ${DOCKER_HOST}"
echo "Running command: $*"

exec "$@"
