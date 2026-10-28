#!/usr/bin/env bash
# Runs the k6 load Job, prints replica count + HPA CPU% every 10s next to it, and saves
# everything under loadtest/results/<timestamp>/ so it can be dropped into the report:
#   k6.log         - raw k6 stdout (human-readable summary + progress)
#   summary.json   - k6's full summary object, extracted from k6.log
#   scaling.csv    - timestamp,ready_replicas,cpu_percent sampled every 10s
#   hpa-events.txt - `kubectl describe hpa` + recent namespace events, captured at the end
#
# Usage:
#   ./loadtest/run.sh                       # defaults from k6-job.yml (gateway /health)
#   ./loadtest/run.sh restaurant-service    # watches & targets restaurant-service
#   ./loadtest/run.sh --fast                # runs shortened stages (~70s) for quick verification
#   ./loadtest/run.sh restaurant-service --fast
#
# Environment overrides:
#   KUBECTL    - kubectl command (default: "minikube kubectl --")
#   TARGET_URL - target URL to hammer
#   PEAK_VUS   - peak concurrent virtual users (default: 150, fast: 50)
#   SUSTAIN    - sustain stage duration (default: 3m, fast: 30s)
#   WARMUP     - warm up duration (default: 1m, fast: 10s)
#   BASELINE   - baseline hold duration (default: 1m, fast: 10s)
#   RAMP       - ramp up duration (default: 1m, fast: 10s)
#   RAMPDOWN   - ramp down duration (default: 2m, fast: 10s)
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KUBECTL=${KUBECTL:-"minikube kubectl --"}

# Parse arguments
FAST_MODE=false
AUTH_MODE=false
WATCH_NS="gateway-service"

for arg in "$@"; do
  case "$arg" in
    --fast)
      FAST_MODE=true
      ;;
    --auth|--jwt)
      AUTH_MODE=true
      ;;
    --help|-h)
      echo "Usage: $0 [NAMESPACE] [--fast] [--jwt]"
      echo "  NAMESPACE: target namespace to watch (default: gateway-service)"
      echo "  --fast:    run shortened stages (~70s) for quick verification"
      echo "  --jwt:     target /auth/verify for cryptographic JWT verification benchmark"
      exit 0
      ;;
    *)
      if [[ "$arg" != -* ]]; then
        WATCH_NS="$arg"
      fi
      ;;
  esac
done

if [[ "${FAST:-false}" == "true" || "${FAST:-false}" == "1" ]]; then
  FAST_MODE=true
fi
if [[ "${JWT:-false}" == "true" || "${JWT:-false}" == "1" || "${AUTH:-false}" == "true" || "${AUTH:-false}" == "1" ]]; then
  AUTH_MODE=true
fi

# Pre-flight check: ensure Kubernetes cluster is reachable
if ! $KUBECTL get ns >/dev/null 2>&1; then
  echo "Error: Kubernetes cluster is unreachable via '$KUBECTL'." >&2
  echo "If you are running Minikube, please start it first (e.g. 'minikube start')." >&2
  exit 1
fi

# Configure target URL and stages
if [ "$AUTH_MODE" = true ]; then
  DEFAULT_TARGET="http://${WATCH_NS}.${WATCH_NS}.svc.cluster.local.:8080/auth/verify"
else
  DEFAULT_TARGET="http://${WATCH_NS}.${WATCH_NS}.svc.cluster.local.:8080/health"
fi
TARGET_URL=${TARGET_URL:-"$DEFAULT_TARGET"}
# Ensure trailing dot on cluster.local to prevent host search-domain interception
if [[ "$TARGET_URL" =~ \.svc\.cluster\.local:[0-9]+ ]] || [[ "$TARGET_URL" =~ \.svc\.cluster\.local$ ]] || [[ "$TARGET_URL" =~ \.svc\.cluster\.local/ ]]; then
  TARGET_URL=$(echo "$TARGET_URL" | sed 's/\.svc\.cluster\.local\([:/]\|$\)/.svc.cluster.local.\1/')
fi

if [ "$FAST_MODE" = true ]; then
  echo ">> Fast mode active: shortened durations for rapid testing"
  PEAK_VUS=${PEAK_VUS:-50}
  WARMUP=${WARMUP:-10s}
  BASELINE=${BASELINE:-10s}
  RAMP=${RAMP:-10s}
  SUSTAIN=${SUSTAIN:-30s}
  RAMPDOWN=${RAMPDOWN:-10s}
else
  PEAK_VUS=${PEAK_VUS:-150}
  WARMUP=${WARMUP:-1m}
  BASELINE=${BASELINE:-1m}
  RAMP=${RAMP:-1m}
  SUSTAIN=${SUSTAIN:-3m}
  RAMPDOWN=${RAMPDOWN:-2m}
fi

RESULTS_DIR="$HERE/results/$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$RESULTS_DIR"
echo ">> Results will be saved to $RESULTS_DIR"
echo ">> Target: $TARGET_URL (Peak VUs: $PEAK_VUS, Sustain: $SUSTAIN, Watching: $WATCH_NS)"

# Apply namespace and script ConfigMap
$KUBECTL apply -f "$HERE/namespace.yml"
$KUBECTL apply -f "$HERE/k6-script.configmap.yml"

# Remove any old job cleanly before applying the new one
echo ">> Cleaning up previous k6-load job..."
$KUBECTL -n loadtest delete job k6-load --ignore-not-found --wait=true >/dev/null 2>&1 || true
while $KUBECTL -n loadtest get pod -l app=k6-load 2>/dev/null | grep -q 'k6-load'; do
  sleep 1
done

# Render and apply k6-job with runtime env configurations
sed -e "/name: TARGET_URL/{n;s|value:.*|value: \"$TARGET_URL\"|}" \
    -e "/name: PEAK_VUS/{n;s|value:.*|value: \"$PEAK_VUS\"|}" \
    -e "/name: SUSTAIN/{n;s|value:.*|value: \"$SUSTAIN\"|}" \
    -e "/name: WARMUP/{n;s|value:.*|value: \"$WARMUP\"|}" \
    -e "/name: BASELINE/{n;s|value:.*|value: \"$BASELINE\"|}" \
    -e "/name: RAMP\$/{n;s|value:.*|value: \"$RAMP\"|}" \
    -e "/name: RAMPDOWN/{n;s|value:.*|value: \"$RAMPDOWN\"|}" \
    "$HERE/k6-job.yml" | $KUBECTL apply -f -

# Background watcher: human-readable to stdout, structured to scaling.csv
(
  echo "timestamp,ready_replicas,cpu_percent" > "$RESULTS_DIR/scaling.csv"
  while true; do
    ts=$(date -u +%H:%M:%S)
    reps=$($KUBECTL -n "$WATCH_NS" get deploy -o jsonpath='{.items[0].status.readyReplicas}' 2>/dev/null || echo '')
    cpu=$($KUBECTL -n "$WATCH_NS" get hpa -o jsonpath='{.items[0].status.currentMetrics[0].resource.current.averageUtilization}' 2>/dev/null || echo '')
    echo "[$ts] $WATCH_NS  ready=${reps:-?}  cpu%=${cpu:-?}"
    echo "$ts,${reps:-},${cpu:-}" >> "$RESULTS_DIR/scaling.csv"
    sleep 10
  done
) &
WATCHER=$!
trap 'kill "$WATCHER" 2>/dev/null || true' EXIT

# Wait for k6 pod to be ready and stream logs
echo ">> Waiting for k6 pod to start..."
$KUBECTL -n loadtest wait --for=condition=ready pod -l app=k6-load --timeout=90s || true
$KUBECTL -n loadtest logs -f job/k6-load 2>&1 | tee "$RESULTS_DIR/k6.log" || true

# Stop the background watcher cleanly
kill "$WATCHER" 2>/dev/null || true
wait "$WATCHER" 2>/dev/null || true
trap - EXIT

# Extract machine-readable summary from log
if grep -q 'K6_JSON:' "$RESULTS_DIR/k6.log"; then
  sed -n 's/.*K6_JSON://p' "$RESULTS_DIR/k6.log" | tr -d '\r' > "$RESULTS_DIR/summary.json"
else
  echo "!! no K6_JSON line found in k6.log — the run may have crashed before finishing" >&2
fi

# Capture final HPA state and events
{
  echo "### kubectl describe hpa -n $WATCH_NS"
  $KUBECTL -n "$WATCH_NS" describe hpa 2>&1
  echo
  echo "### kubectl get events -n $WATCH_NS --sort-by=.lastTimestamp"
  $KUBECTL -n "$WATCH_NS" get events --sort-by=.lastTimestamp 2>&1
} > "$RESULTS_DIR/hpa-events.txt"

# Display load test summary using show.sh
if [ -x "$HERE/show.sh" ]; then
  "$HERE/show.sh" "$RESULTS_DIR"
fi

echo ">> Final state ($WATCH_NS):"
$KUBECTL -n "$WATCH_NS" get hpa,deploy,pods
echo
echo ">> All results saved to $RESULTS_DIR"
