#!/usr/bin/env bash
# Runs the k6 load Job and prints replica count + HPA CPU% every 10s next to it.
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

$KUBECTL apply -f "$HERE/namespace.yml"
$KUBECTL apply -f "$HERE/k6-script.configmap.yml"
$KUBECTL -n loadtest delete job k6-load --ignore-not-found
$KUBECTL apply -f "$HERE/k6-job.yml"

# background watcher
(
  while $KUBECTL -n loadtest get job k6-load -o jsonpath='{.status.active}' 2>/dev/null | grep -q 1; do
    ts=$(date +%H:%M:%S)
    reps=$($KUBECTL -n "$WATCH_NS" get deploy -o jsonpath='{.items[0].status.readyReplicas}' 2>/dev/null || echo '?')
    cpu=$($KUBECTL -n "$WATCH_NS" get hpa -o jsonpath='{.items[0].status.currentMetrics[0].resource.current.averageUtilization}' 2>/dev/null || echo '-')
    echo "[$ts] $WATCH_NS  ready=$reps  cpu%=$cpu"
    sleep 10
  done
) &
WATCHER=$!
trap 'kill "$WATCHER" 2>/dev/null || true' EXIT

$KUBECTL -n loadtest wait --for=condition=ready pod -l app=k6-load --timeout=90s || true
$KUBECTL -n loadtest logs -f job/k6-load || true

echo
echo ">> final state ($WATCH_NS):"
$KUBECTL -n "$WATCH_NS" get hpa,deploy,pods
