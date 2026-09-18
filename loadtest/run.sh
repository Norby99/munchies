#!/usr/bin/env bash
# Runs the k6 load Job, prints replica count + HPA CPU% every 10s next to it, and saves
# everything under loadtest/results/<timestamp>/ so it can be dropped into the report:
#   k6.log         - raw k6 stdout (human-readable summary + progress)
#   summary.json   - k6's full summary object, extracted from k6.log
#   scaling.csv    - timestamp,ready_replicas,cpu_percent sampled every 10s
#   hpa-events.txt - `kubectl describe hpa` + recent namespace events, captured at the end
#
#   ./loadtest/run.sh                       # defaults from k6-job.yml (gateway /health)
#   ./loadtest/run.sh restaurant-service    # just changes which namespace is watched
#
# To hit a different target or change load, edit env in k6-job.yml
# (TARGET_URL / PEAK_VUS / SUSTAIN) before running.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KUBECTL=${KUBECTL:-"minikube kubectl --"}
WATCH_NS=${1:-gateway-service}

RESULTS_DIR="$HERE/results/$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$RESULTS_DIR"
echo ">> results will be saved to $RESULTS_DIR"

$KUBECTL apply -f "$HERE/namespace.yml"
$KUBECTL apply -f "$HERE/k6-script.configmap.yml"
$KUBECTL -n loadtest delete job k6-load --ignore-not-found
$KUBECTL apply -f "$HERE/k6-job.yml"

# background watcher: human-readable to stdout, structured to scaling.csv
(
  echo "timestamp,ready_replicas,cpu_percent" > "$RESULTS_DIR/scaling.csv"
  while $KUBECTL -n loadtest get job k6-load -o jsonpath='{.status.active}' 2>/dev/null | grep -q 1; do
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

$KUBECTL -n loadtest wait --for=condition=ready pod -l app=k6-load --timeout=90s || true
$KUBECTL -n loadtest logs -f job/k6-load 2>&1 | tee "$RESULTS_DIR/k6.log" || true

kill "$WATCHER" 2>/dev/null || true
wait "$WATCHER" 2>/dev/null || true

# pull the machine-readable summary out of the log (see handleSummary() in the k6 script)
if grep -q '^K6_JSON:' "$RESULTS_DIR/k6.log"; then
  grep '^K6_JSON:' "$RESULTS_DIR/k6.log" | sed 's/^K6_JSON://' > "$RESULTS_DIR/summary.json"
else
  echo "!! no K6_JSON line found in k6.log — the run may have crashed before finishing" >&2
fi

{
  echo "### kubectl describe hpa -n $WATCH_NS"
  $KUBECTL -n "$WATCH_NS" describe hpa 2>&1
  echo
  echo "### kubectl get events -n $WATCH_NS --sort-by=.lastTimestamp"
  $KUBECTL -n "$WATCH_NS" get events --sort-by=.lastTimestamp 2>&1
} > "$RESULTS_DIR/hpa-events.txt"

echo
echo ">> final state ($WATCH_NS):"
$KUBECTL -n "$WATCH_NS" get hpa,deploy,pods
echo
echo ">> results saved to $RESULTS_DIR"
ls -la "$RESULTS_DIR"
