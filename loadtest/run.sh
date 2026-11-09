#!/usr/bin/env bash
# Runs the k6 load Job, prints replica count + HPA CPU% every 10s next to it, keeps sampling after
# k6 ends until the service is back to its minimum replicas, and saves everything under
# loadtest/results/<timestamp>/ so it can be dropped into the report:
#   environment.txt - node capacity, HPA spec and pod resources, captured before the load starts
#   k6.log          - raw k6 stdout (human-readable summary + progress)
#   summary.json    - k6's full summary object, extracted from k6.log
#   scaling.csv     - timestamp,ready_replicas,cpu_percent sampled every 10s
#   hpa-events.txt  - `kubectl describe hpa` + recent namespace events, captured at the end
#
# Usage:
#   ./loadtest/run.sh                    # the benchmark: gateway-service /auth/verify, peak 1000 req/s
#   PEAK_RATE=2000 ./loadtest/run.sh     # a higher peak
#
# Environment overrides:
#   PEAK_RATE  - peak requests per second (default: 1000); the profile itself is in the k6 script
#   TARGET_URL - endpoint to load (default: gateway-service /auth/verify)
#   WATCH_NS   - namespace whose Deployment and HPA are sampled (default: gateway-service)
#   SCALE_IN_TIMEOUT - most seconds to wait for scale-in after k6 ends (default: 600)
#   KUBECTL    - kubectl command (default: "minikube kubectl --")
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KUBECTL=${KUBECTL:-"minikube kubectl --"}
WATCH_NS=${WATCH_NS:-gateway-service}
TARGET_URL=${TARGET_URL:-"http://gateway-service.gateway-service.svc.cluster.local.:8080/auth/verify"}
PEAK_RATE=${PEAK_RATE:-1000}
SCALE_IN_TIMEOUT=${SCALE_IN_TIMEOUT:-600}

if [ "$#" -gt 0 ]; then
  sed -n '2,/^set -euo/p' "$0" | sed '$d; s/^# \{0,1\}//'
  exit 0
fi

# Pre-flight check: ensure Kubernetes cluster is reachable
if ! $KUBECTL get ns >/dev/null 2>&1; then
  echo "Error: Kubernetes cluster is unreachable via '$KUBECTL'." >&2
  echo "If you are running Minikube, please start it first (e.g. 'minikube start')." >&2
  exit 1
fi

RESULTS_DIR="$HERE/results/$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$RESULTS_DIR"
echo ">> Results will be saved to $RESULTS_DIR"
echo ">> Target: $TARGET_URL (peak $PEAK_RATE req/s, watching: $WATCH_NS)"

# Record what the run ran on, so the report never has to say "not recorded".
{
  echo "### run"
  echo "started (UTC): $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "target: $TARGET_URL"
  echo "peak rate: $PEAK_RATE req/s"
  echo
  echo "### node capacity and allocatable resources"
  $KUBECTL get nodes -o custom-columns='NODE:.metadata.name,CPU:.status.capacity.cpu,MEMORY:.status.capacity.memory,ALLOCATABLE_CPU:.status.allocatable.cpu,ALLOCATABLE_MEMORY:.status.allocatable.memory' 2>&1
  echo
  # With the Docker driver the node reports the host's capacity; the cluster's real limits are
  # those of the Minikube container.
  if command -v docker >/dev/null 2>&1 && docker inspect minikube >/dev/null 2>&1; then
    echo "### Minikube container limits (Docker driver)"
    docker inspect minikube --format '{{.HostConfig.NanoCpus}} {{.HostConfig.Memory}}' \
      | awk -v host="$(nproc)" '{
          if ($1 > 0) printf "cpus: %g\n", $1 / 1e9; else printf "cpus: not limited (all %s host CPUs)\n", host
          if ($2 > 0) printf "memory: %d MiB\n", $2 / 1048576; else print "memory: not limited"
        }'
    echo
  fi
  echo "### HPA spec ($WATCH_NS)"
  $KUBECTL -n "$WATCH_NS" get hpa -o yaml 2>&1 | sed -n '/^  spec:/,/^  status:/p'
  echo
  echo "### pod resources ($WATCH_NS)"
  $KUBECTL -n "$WATCH_NS" get deploy -o jsonpath='{range .items[*]}{.metadata.name}: {.spec.template.spec.containers[0].resources}{"\n"}{end}' 2>&1
  echo
  echo "### ready replicas at start"
  $KUBECTL -n "$WATCH_NS" get deploy 2>&1
} > "$RESULTS_DIR/environment.txt"

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
    -e "/name: PEAK_RATE/{n;s|value:.*|value: \"$PEAK_RATE\"|}" \
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

# Keep sampling until the service is back to its minimum replicas, so scale-in is captured even
# when it finishes after k6 does.
if [ "$SCALE_IN_TIMEOUT" -gt 0 ]; then
  MIN_REPLICAS=$($KUBECTL -n "$WATCH_NS" get hpa -o jsonpath='{.items[0].spec.minReplicas}' 2>/dev/null || echo 1)
  echo ">> k6 finished; sampling until $WATCH_NS is back to ${MIN_REPLICAS:-1} replica(s) (at most ${SCALE_IN_TIMEOUT}s)..."
  waited=0
  while [ "$waited" -lt "$SCALE_IN_TIMEOUT" ]; do
    reps=$($KUBECTL -n "$WATCH_NS" get deploy -o jsonpath='{.items[0].status.readyReplicas}' 2>/dev/null || echo '')
    if [ "${reps:-0}" -le "${MIN_REPLICAS:-1}" ]; then
      echo ">> $WATCH_NS is back to $reps replica(s) after ${waited}s"
      break
    fi
    sleep 10
    waited=$((waited + 10))
  done
  if [ "$waited" -ge "$SCALE_IN_TIMEOUT" ]; then
    echo "!! $WATCH_NS still has ${reps:-?} replicas after ${SCALE_IN_TIMEOUT}s; scale-in not complete" >&2
  fi
  # One last sample, so the end state is in scaling.csv.
  sleep 10
fi

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
