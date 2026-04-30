#!/usr/bin/env bash
# ============================================================
# deploy.sh — Docker Compose Deployment Script
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'; BOLD='\033[1m'
log()  { echo -e "${BLUE}[DEPLOY]${NC} $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}     $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}   $*"; }
fail() { echo -e "${RED}[FAIL]${NC}   $*"; exit 1; }

ENV="${1:?Usage: deploy.sh <env> <host> <ssh_key> <ssh_user> <image_tag>}"
HOST="${2:?Missing host}"
SSH_KEY="${3:?Missing ssh_key}"
SSH_USER="${4:?Missing ssh_user}"
IMAGE_TAG="${5:?Missing image_tag}"

[[ "$ENV" != "staging" && "$ENV" != "production" ]] && fail "Invalid env: $ENV"

COMPOSE_FILE="jenkins/docker-compose.${ENV}.yml"
[[ ! -f "$COMPOSE_FILE" ]] && fail "Compose file not found: $COMPOSE_FILE"

DEPLOY_DIR="/opt/book-nest"
DOCKER_ORG="${DOCKER_ORG:-bookstore}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-docker.io}"

ssh_exec() { ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -o ConnectTimeout=30 "${SSH_USER}@${HOST}" "$@"; }
scp_file() { scp -i "$SSH_KEY" -o StrictHostKeyChecking=no "$1" "${SSH_USER}@${HOST}:$2"; }

log "Checking SSH connectivity to ${HOST}..."
ssh_exec "echo 'SSH OK'" || fail "Cannot reach ${HOST}"

log "Transferring compose files..."
ssh_exec "mkdir -p ${DEPLOY_DIR}"
scp_file "$COMPOSE_FILE" "${DEPLOY_DIR}/docker-compose.yml"
scp_file "docker-compose.yml" "${DEPLOY_DIR}/docker-compose.base.yml"

log "Creating .env..."
ssh_exec "cat > ${DEPLOY_DIR}/.env" << EOF
IMAGE_TAG=${IMAGE_TAG}
DOCKER_ORG=${DOCKER_ORG}
DOCKER_REGISTRY=${DOCKER_REGISTRY}
DEPLOY_ENV=${ENV}
EOF

# Extract services from docker-compose overrides to support future services dynamically
# Fallback to current services if it fails
SERVICES_STR=$(grep 'container_name:' $COMPOSE_FILE | awk -F'-' '{print $2"-"$3}' || echo "auth-service book-service user-service")
# Fallback to hardcoded list if extraction returns empty
if [[ -z "$SERVICES_STR" ]]; then
    SERVICES=(auth-service user-service book-service)
else
    SERVICES=($SERVICES_STR)
fi

log "Deploying services: ${SERVICES[*]}"

log "Pulling Docker images..."
for svc in "${SERVICES[@]}"; do
    ssh_exec "docker pull ${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:${IMAGE_TAG}" || warn "Could not pull ${svc} — using cache"
done

if [[ "$ENV" == "production" ]]; then
    for svc in "${SERVICES[@]}"; do
        log "  Updating ${svc}..."
        ssh_exec "cd ${DEPLOY_DIR} && docker compose -f docker-compose.base.yml -f docker-compose.yml up -d --no-deps ${svc}" || fail "Failed to update ${svc}"
        for i in {1..12}; do
            if ssh_exec "docker inspect --format='{{.State.Health.Status}}' ${ENV}-${svc} 2>/dev/null | grep -q 'healthy'"; then
                ok "  ${svc} is healthy"
                break
            fi
            [[ $i -eq 12 ]] && fail "${svc} health check failed"
            sleep 5
        done
    done
else
    ssh_exec "cd ${DEPLOY_DIR} && docker compose -f docker-compose.base.yml -f docker-compose.yml up -d --remove-orphans"
fi

ok "Deployment to ${ENV} complete! Tag: ${IMAGE_TAG}"
