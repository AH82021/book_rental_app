#!/usr/bin/env bash
# ============================================================
# smoke-test.sh — Service Health Check / Smoke Tests
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'; BOLD='\033[1m'
log()     { echo -e "${BLUE}[SMOKE]${NC}  $*"; }
ok()      { echo -e "${GREEN}[PASS]${NC}   $*"; }
warn()    { echo -e "${YELLOW}[SKIP]${NC}   $*"; }
fail()    { echo -e "${RED}[FAIL]${NC}   $*"; }
section() { echo -e "\n${BOLD}══ $* ══${NC}"; }

ENV="${1:?Usage: smoke-test.sh <env> <host>}"
HOST="${2:?Missing host}"

# ─── Service endpoint map ────────────────────────────────────
# Format: "service:port:context_path"
# Add future services here (e.g. ["report-service"]="8086:/actuator/health")
declare -A SERVICES=(
    ["auth-service"]="8088:/actuator/health"
    ["user-service"]="8081:/actuator/health"
    ["book-service"]="8080:/actuator/health"
)

MAX_RETRIES=5
RETRY_INTERVAL=10
CONNECT_TIMEOUT=10
passed=0; failed=0; skipped=0

check_endpoint() {
    local service="$1"
    local port="$2"
    local path="$3"
    local url="http://${HOST}:${port}${path}"

    log "Checking ${service} → ${url}"

    local attempt=0
    while [[ $attempt -lt $MAX_RETRIES ]]; do
        attempt=$((attempt + 1))
        HTTP_STATUS=$(curl -s -o /tmp/health.json -w "%{http_code}" --connect-timeout $CONNECT_TIMEOUT "$url" 2>/dev/null || echo "000")

        if [[ "$HTTP_STATUS" == "200" ]]; then
            HEALTH_STATUS=$(jq -r '.status // "UP"' /tmp/health.json 2>/dev/null || echo "UNKNOWN")
            if [[ "$HEALTH_STATUS" == "UP" ]]; then
                ok "  ${service}: HTTP 200 → status=${HEALTH_STATUS}"
                passed=$((passed + 1))
                return 0
            else
                warn "  ${service}: HTTP 200 but status=${HEALTH_STATUS}"
            fi
        else
            warn "  ${service}: HTTP ${HTTP_STATUS} (attempt ${attempt}/${MAX_RETRIES})"
        fi
        [[ $attempt -lt $MAX_RETRIES ]] && sleep $RETRY_INTERVAL
    done
    fail "  ${service}: FAILED after ${MAX_RETRIES} attempts"
    failed=$((failed + 1))
    return 1
}

section "Smoke Tests — ${ENV} (${HOST})"
errors=()

for service in "${!SERVICES[@]}"; do
    port_path="${SERVICES[$service]}"
    port="${port_path%%:*}"
    path="${port_path#*:}"
    if ! check_endpoint "$service" "$port" "$path"; then
        errors+=("$service")
    fi
done

section "Results"
echo -e "  ${GREEN}Passed${NC}  : ${passed}"
echo -e "  ${RED}Failed${NC}  : ${failed}"

if [[ ${#errors[@]} -gt 0 ]]; then
    fail "Failed services:"
    for svc in "${errors[@]}"; do echo -e "  ${RED}✗${NC} ${svc}"; done
    exit 1
fi
ok "All services are healthy! ✓"
exit 0
