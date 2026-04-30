#!/usr/bin/env bash
# ============================================================
# build.sh — Maven Build Helper for book-nest
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'; BOLD='\033[1m'
log()  { echo -e "${BLUE}[BUILD]${NC} $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
fail() { echo -e "${RED}[FAIL]${NC}  $*"; exit 1; }

MODULE="${1:-}"
PHASE="${2:-package}"
EXTRA="${3:-}"

MAVEN_OPTS="${MAVEN_OPTS:--Xmx1024m -XX:+UseG1GC}"
MAX_RETRIES=2
RETRY_DELAY=10
MAVEN_CLI_OPTS=("--batch-mode" "--errors" "--fail-at-end" "--show-version" "-Dmaven.repo.local=${HOME}/.m2/repository" "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn")

run_maven() {
    local cmd=("./mvnw" "${MAVEN_CLI_OPTS[@]}")
    if [[ -n "$MODULE" ]]; then
        cmd+=("-pl" "$MODULE" "-am")
        log "Building module: ${BOLD}${MODULE}${NC} (phase: ${PHASE})"
    else
        log "Building ${BOLD}ALL modules${NC} (phase: ${PHASE})"
    fi
    cmd+=("$PHASE")
    [[ -n "$EXTRA" ]] && cmd+=($EXTRA)

    local attempt=0
    while [[ $attempt -le $MAX_RETRIES ]]; do
        attempt=$((attempt + 1))
        log "Attempt ${attempt}/${MAX_RETRIES}..."
        if "${cmd[@]}"; then
            ok "Build succeeded"
            return 0
        else
            [[ $attempt -le $MAX_RETRIES ]] && sleep $RETRY_DELAY
        fi
    done
    fail "Build failed after ${MAX_RETRIES} attempts."
}

log "Java version: $(java -version 2>&1 | head -1)"
[[ ! -f "pom.xml" ]] && fail "Must run from project root"

start_time=$(date +%s)
run_maven
ok "Total build time: $(($(date +%s) - start_time))s"
