#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROFILE="${1:-local}"
ENV_FILE="${APP_ENV_FILE:-.env.local}"

if [[ "$PROFILE" != "local" ]]; then
  echo "usage: bash scripts/run.local.sh [local]" >&2
  exit 1
fi

cd "$ROOT_DIR"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "local env file not found: $ENV_FILE" >&2
  echo "copy .env.example to .env.local and set PERMISSION_INTERNAL_REQUEST_SECRET" >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
export PERMISSION_INTERNAL_REQUEST_SECRET="${PERMISSION_INTERNAL_REQUEST_SECRET:-local-authz-internal-secret}"

./gradlew :app:bootRun
