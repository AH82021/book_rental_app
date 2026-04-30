#!/usr/bin/env bash
# ============================================================
# rollback.sh — Automated Rollback Script
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'; BOLD='\033[1m'
log()  { echo -e "${BLUE}[ROLLBACK]${NC} $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}       $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}     $*"; }
fail() { echo -e "${RED}[FAIL]${NC}     $*"; exit 1; }

ENV="${1:?Usage: rollback.sh <env> <host> <ssh_key> <ssh_user>}"
HOST="${2:?Missing host}"
SSH_KEY="${3:?Missing ssh_key}"
SSH_USER="${4:?Missing ssh_user}"

DEPLOY_DIR="/opt/book-nest"
DOCKER_ORG="${DOCKER_ORG:-bookstore}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-docker.io}"

ssh_exec() { ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -o ConnectTimeout=30 "${SSH_USER}@${HOST}" "$@"; }

log "Fetching deployment history from ${HOST}..."
CURRENT_TAG=$(ssh_exec "cat ${DEPLOY_DIR}/.env | grep IMAGE_TAG | cut -d= -f2" 2>/dev/null || echo "unknown")
PREVIOUS_TAG=$(ssh_exec "cat ${DEPLOY_DIR}/rollback.state 2>/dev/null || echo 'latest'")

log "Current tag  : ${CURRENT_TAG}"
log "Rollback tag : ${PREVIOUS_TAG}"

[[ "$PREVIOUS_TAG" == "unknown" || -z "$PREVIOUS_TAG" ]] && fail "No previous deployment state found."
warn "⚠️  ROLLING BACK ${ENV} from ${CURRENT_TAG} → ${PREVIOUS_TAG}"

# Dynamically find running services based on docker-compose files to support future services
COMPOSE_FILE="jenkins/docker-compose.${ENV}.yml"
SERVICES_STR=$(ssh_exec "cat ${DEPLOY_DIR}/${COMPOSE_FILE} 2>/dev/null | grep 'container_name:' | awk -F'-' '{print \$2\"-\"\$3}'" || echo "auth-service book-service user-service")
if [[ -z "$SERVICES_STR" ]]; then
    SERVICES=(auth-service user-service book-service)
else
    SERVICES=($SERVICES_STR)
fi

log "Pulling previous images..."
for svc in "${SERVICES[@]}"; do
    ssh_exec "docker pull ${DOCKER_REGISTRY}/${DOCKER_ORG}/${svc}:${PREVIOUS_TAG}" || warn "Using cached ${svc}"
done

log "Updating .env and Redeploying..."
ssh_exec "sed -i 's/IMAGE_TAG=.*/IMAGE_TAG=${PREVIOUS_TAG}/' ${DEPLOY_DIR}/.env"
ssh_exec "cd ${DEPLOY_DIR} && docker compose -f docker-compose.base.yml -f docker-compose.yml up -d --remove-orphans"

log "Waiting 30s..."
sleep 30

HEALTHY=true
for svc in "${SERVICES[@]}"; do
    STATUS=$(ssh_exec "docker inspect --format='{{.State.Health.Status}}' ${ENV}-${svc} 2>/dev/null || echo 'unknown'")
    if [[ "$STATUS" == "healthy" ]]; then
        ok "  ${svc}: healthy"
    else
        warn "  ${svc}: status=${STATUS}"
        HEALTHY=false
    fi
done

ssh_exec "echo '${CURRENT_TAG}' > ${DEPLOY_DIR}/rollback.state"

if [[ "$HEALTHY" == "true" ]]; then
    ok "✅ Rollback to ${PREVIOUS_TAG} succeeded!"
else
    warn "⚠️  Rollback complete but some services report non-healthy status."
fi

if [[ -n "${SLACK_WEBHOOK:-}" ]]; then
    STATUS_MSG=$( [[ "$HEALTHY" == "true" ]] && echo "✅ Rollback successful" || echo "⚠️ Rollback partial" )
    curl -s -X POST "$SLACK_WEBHOOK" -H "Content-Type: application/json" -d "{\"text\": \"🔄 *ROLLBACK TRIGGERED* — ${ENV}\n${STATUS_MSG}\nRolled back: \`${CURRENT_TAG}\` → \`${PREVIOUS_TAG}\`\"}" || true
fi
