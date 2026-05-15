#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="docker-compose.blue-green.yml"
NGINX_CONF="./nginx/nginx.conf"
SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BLUE_UPSTREAM="workhub-blue:8080"
GREEN_UPSTREAM="workhub-green:8080"
STABILISATION_SECS=30 

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
step()    { echo -e "${CYAN}[STEP]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }
die()     { error "$*"; exit 1; }

usage() {
    echo "Usage: $0 <docker-image-with-tag>"
    echo "  e.g. $0 ghcr.io/besology512/enterprise-application-project:abc1234"
    exit 1
}

[[ $# -lt 1 ]] && usage
NEW_IMAGE="$1"

[[ -f "$COMPOSE_FILE" ]]  || die "Compose file not found: $COMPOSE_FILE"
[[ -f "$NGINX_CONF" ]]    || die "Nginx config not found: $NGINX_CONF"
command -v docker         >/dev/null 2>&1 || die "'docker' is not installed or not in PATH"

if grep -q "$BLUE_UPSTREAM" "$NGINX_CONF"; then
    ACTIVE_SLOT="blue";  IDLE_SLOT="green"
    IDLE_SERVICE="workhub-green"; IDLE_CONTAINER="workhub-green"
elif grep -q "$GREEN_UPSTREAM" "$NGINX_CONF"; then
    ACTIVE_SLOT="green"; IDLE_SLOT="blue"
    IDLE_SERVICE="workhub-blue";  IDLE_CONTAINER="workhub-blue"
else
    die "Cannot detect active slot from $NGINX_CONF"
fi

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║          WorkHub Blue/Green Deployment                   ║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║${NC}  New image  : ${NEW_IMAGE}"
echo -e "${CYAN}║${NC}  Active slot: ${ACTIVE_SLOT^^} (stays live until health verified)"
echo -e "${CYAN}║${NC}  Target slot: ${IDLE_SLOT^^}  (will receive new image + traffic)"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

step "1/5  Pulling new image..."
docker pull "$NEW_IMAGE"
info "Image pulled ✓"

step "2/5  Recreating idle slot ($IDLE_SLOT) with new image..."
export WORKHUB_IMAGE="$NEW_IMAGE"

docker compose -f "$COMPOSE_FILE" up -d \
    --no-deps \
    --force-recreate \
    "$IDLE_SERVICE"

info "Idle slot container recreated ✓"

step "3/5  Switching traffic to $IDLE_SLOT slot..."
"$SCRIPTS_DIR/switch-traffic.sh"
info "Traffic is now on $IDLE_SLOT ✓"

step "4/5  Monitoring new slot for ${STABILISATION_SECS}s stabilisation window..."
echo "       (press Ctrl+C to abort and initiate rollback)"
sleep "$STABILISATION_SECS"
info "Stabilisation complete ✓"

step "5/5  Old slot ($ACTIVE_SLOT) is still running as rollback target."
echo ""
echo "  To free resources (removes rollback safety net):"
echo "    docker compose -f $COMPOSE_FILE stop workhub-${ACTIVE_SLOT}"
echo ""
echo "  To roll back instantly:"
echo "    ./scripts/switch-traffic.sh rollback"
echo ""

echo -e "${GREEN} Deployment complete!${NC}"
echo "   Active slot : ${IDLE_SLOT^^}"
echo "   Image       : $NEW_IMAGE"
echo "   Health check: curl -s http://localhost/actuator/health"
echo ""
