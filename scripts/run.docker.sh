#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_PROJECT_NAME="authz-server"
ACTION="${1:-up}"
ENV_NAME="${2:-dev}"
shift $(( $# > 0 ? 1 : 0 )) || true
shift $(( $# > 0 ? 1 : 0 )) || true

usage() {
  echo "Usage: ./scripts/run.docker.sh [up|down|build|logs|ps|restart] [dev|prod] [docker compose options]" >&2
}

case "$ACTION" in
  up|down|build|logs|ps|restart) ;;
  *) usage; exit 1 ;;
esac

case "$ENV_NAME" in
  dev|prod)
    BASE_COMPOSE_FILE="$PROJECT_ROOT/docker/compose.yml"
    ENV_COMPOSE_FILE="$PROJECT_ROOT/docker/$ENV_NAME/compose.yml"
    ;;
  *) usage; exit 1 ;;
esac

ENV_FILE="$PROJECT_ROOT/.env.$ENV_NAME"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Env file not found: $ENV_FILE" >&2
  echo "Create it from .env.example before running Docker." >&2
  exit 1
fi

SHARED_NETWORK="${SERVICE_SHARED_NETWORK:-${BACKEND_SHARED_NETWORK:-${MSA_SHARED_NETWORK:-service-backbone-shared}}}"
if ! docker network inspect "$SHARED_NETWORK" >/dev/null 2>&1; then
  echo "Creating external network: $SHARED_NETWORK"
  docker network create "$SHARED_NETWORK" >/dev/null
fi

compose() {
  APP_ENV="$ENV_NAME" SERVICE_SHARED_NETWORK="$SHARED_NETWORK" BACKEND_SHARED_NETWORK="$SHARED_NETWORK" MSA_SHARED_NETWORK="$SHARED_NETWORK" \
    docker compose --env-file "$ENV_FILE" -p "$COMPOSE_PROJECT_NAME" -f "$BASE_COMPOSE_FILE" -f "$ENV_COMPOSE_FILE" "$@"
}

case "$ACTION" in
  up) compose up --build "$@" ;;
  down) compose down --remove-orphans "$@" ;;
  build) compose build "$@" ;;
  logs) compose logs -f "$@" ;;
  ps) compose ps "$@" ;;
  restart) compose down --remove-orphans && compose up --build "$@" ;;
esac
