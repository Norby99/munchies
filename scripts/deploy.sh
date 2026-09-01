#!/usr/bin/env bash

# Enable short-circuiting: exit immediately if any command fails, treat unset variables as error, and propagate pipe errors
set -euo pipefail

# Step 1: Pull latest changes or shallow clone repository
REPO_URL="${REPO_URL:-$(git config --get remote.origin.url 2>/dev/null || echo "git@github.com:Norby99/munchies.git")}"
REPO_DIR="${REPO_DIR:-$(basename "${REPO_URL%.git}")}"

echo "[INFO] Pulling latest changes from git..."
if git pull; then
  echo "[INFO] Git pull successful. Continuing with deployment..."
else
  echo "[WARN] Git pull failed. Shallow cloning repository from ${REPO_URL}..."
  git clone --depth 1 "${REPO_URL}" "${REPO_DIR}"
  cd "${REPO_DIR}"
fi

# Step 2: Compose Down & Compose Up
echo "[INFO] Tearing down existing containers..."
./gradlew composeDown

echo "[INFO] Building and starting containers..."
./gradlew composeUp

# Step 3: Health check / ping services
check_service_health() {
  local service_name="$1"
  local url="$2"
  local expected_status="${3:-200}"
  local max_retries="${4:-30}"
  local delay="${5:-2}"

  echo -n "[CHECK] Checking $service_name ($url)... "
  for ((i=1; i<=max_retries; i++)); do
    local status_code
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

    if [ "$status_code" = "$expected_status" ]; then
      echo "OK (HTTP $status_code)"
      return 0
    fi
    sleep "$delay"
  done

  echo "FAILED (HTTP status: ${status_code:-none}, expected: $expected_status after $max_retries retries)"
  return 1
}

echo "[INFO] Running service health checks..."
check_service_health "Order Service" "http://localhost:8081/health" "200"
check_service_health "User Service" "http://localhost:8082/health" "200"
check_service_health "Payment Service" "http://localhost:8083/health" "200"
check_service_health "Restaurant Service" "http://localhost:8087/health" "200"
check_service_health "Gateway Service" "http://localhost:8086/health" "200"

echo ""
echo "[INFO] Deployment completed successfully. All services are up."