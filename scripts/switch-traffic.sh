#!/usr/bin/env bash

set -euo pipefail

NGINX_CONTAINER="workhub-nginx"
NGINX_CONF="./nginx/nginx.conf"
BLUE_UPSTREAM="workhub-blue:8080"
GREEN_UPSTREAM="workhub-green:8080"
HEALTH_TIMEOUT=120   
HEALTH_INTERVAL=5     

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }
die()     { error "$*"; exit 1; }

detect_active_slot() {
    if grep -q "$BLUE_UPSTREAM" "$NGINX_CONF"; then
        echo "blue"
    elif grep -q "$GREEN_UPSTREAM" "$NGINX_CONF"; then
        echo "green"
    else
        die "Cannot detect active slot from $NGINX_CONF. Check the ACTIVE_UPSTREAM placeholder."
    fi
}

wait_for_healthy() {
    local container="$1"
    local url="http://localhost:8080/actuator/health"
    local elapsed=0

    info "Waiting for '$container' to report healthy (timeout: ${HEALTH_TIMEOUT}s)..."
    while [[ $elapsed -lt $HEALTH_TIMEOUT ]]; do
        if docker exec "$container" wget -qO- "$url" 2>/dev/null | grep -q '"status":"UP"'; then
            info "'$container' is healthy ✓"
            return 0
        fi
        sleep $HEALTH_INTERVAL
        elapsed=$((elapsed + HEALTH_INTERVAL))
        warn "  Still waiting... (${elapsed}s elapsed)"
    done

    error "'$container' did not become healthy within ${HEALTH_TIMEOUT}s."
    return 1
}

switch_nginx() {
    local target_upstream="$1"

    info "Updating Nginx config → upstream: $target_upstream"
    sed -i "s|proxy_pass .*http://[^;]*;|proxy_pass         http://${target_upstream};|g" "$NGINX_CONF"
    sed -i "s|proxy_pass .*http://[^/]*/actuator/health;|proxy_pass         http://${target_upstream}/actuator/health;|g" "$NGINX_CONF"

    info "Reloading Nginx (zero-downtime graceful reload)..."
    docker exec "$NGINX_CONTAINER" nginx -s reload
    info "Nginx reloaded ✓"
}

main() {
    local mode="${1:-auto}"

    [[ -f "$NGINX_CONF" ]] || die "nginx.conf not found at $NGINX_CONF"

    local current_slot
    current_slot=$(detect_active_slot)
    info "Current active slot: $current_slot"

    local target_slot target_upstream target_container
    if [[ "$mode" == "rollback" ]]; then
        # rollback
        if [[ "$current_slot" == "blue" ]]; then
            target_slot="green"; target_upstream="$GREEN_UPSTREAM"; target_container="workhub-green"
        else
            target_slot="blue";  target_upstream="$BLUE_UPSTREAM";  target_container="workhub-blue"
        fi
        warn "ROLLBACK mode — switching from $current_slot → $target_slot"
    else
        # normal
        if [[ "$current_slot" == "blue" ]]; then
            target_slot="green"; target_upstream="$GREEN_UPSTREAM"; target_container="workhub-green"
        else
            target_slot="blue";  target_upstream="$BLUE_UPSTREAM";  target_container="workhub-blue"
        fi
        info "Switching traffic: $current_slot → $target_slot"
    fi

    if ! wait_for_healthy "$target_container"; then
        die "Aborting switch — $target_slot slot is not healthy. Traffic remains on $current_slot."
    fi

    switch_nginx "$target_upstream"

    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  Traffic switched to: ${target_slot^^}               ║${NC}"
    echo -e "${GREEN}║  Previous slot (${current_slot}) is still running   ║${NC}"
    echo -e "${GREEN}║  for instant rollback.                   ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
    echo ""
    echo "  Rollback command:  ./scripts/switch-traffic.sh rollback"
    echo "  Health check:      curl -s http://localhost/actuator/health"
    echo ""
}

main "$@"
