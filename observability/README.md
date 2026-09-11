# Observability (Prometheus + Grafana)

A scoped-down Prometheus + Grafana stack for the benchmark chapter — history and graphs for
the same scaling behaviour `loadtest/` already exercises, instead of the hand-rolled
`scaling.csv` poll loop.

**Status:** statically verified only — `helm lint` clean, `helm template` renders without
error, and the dashboard JSON that ends up in the cluster's ConfigMap was extracted from the
rendered output and re-parsed as JSON. **Not yet installed against a live cluster** in this
session (minikube was kept stopped throughout, deliberately, to avoid another OOM cycle).
Do the install yourself and treat the first run as the real verification.

Like `loadtest/`, this is **intentionally not part of `k8s/` or `./gradlew deploy`** — bring
it up right before a benchmark run, tear it down after. It is not meant to run continuously.

## Why this shape, not the full `kube-prometheus-stack`

The common "just install kube-prometheus-stack" path bundles the Prometheus Operator
(another controller pod + CRDs), Alertmanager, node-exporter and a much larger default
Grafana — too heavy for a single-node minikube that has already been OOM-killed once this
project. Instead:

- **`prometheus-community/prometheus`** — the classic (pre-operator) chart: a plain
  Prometheus `Deployment` with a ConfigMap-based scrape config, no CRDs, no operator.
  Alertmanager, `prometheus-node-exporter` and `prometheus-pushgateway` sub-charts are
  disabled; `kube-state-metrics` is kept (it's what exports Deployment/HPA object state —
  `kube_deployment_status_replicas_ready`, `kube_horizontalpodautoscaler_status_*_replicas`
  — as metrics; without it there's no replica-count time series to graph).
- Actual per-container CPU usage (not just the static request/limit) comes for free from the
  **`kubernetes-nodes-cadvisor`** scrape job this chart enables by default — the same
  cAdvisor data `metrics-server` itself reads to drive the HPA, now retained as history
  instead of only existing for a few seconds.
- **`grafana-community/grafana`** — resources trimmed, no persistence (ephemeral session),
  datasource and one dashboard provisioned entirely from `values/grafana.yaml` (no manual
  clicking, no external `dashboards.grafana.com` fetch — the whole dashboard JSON is inlined
  in the values file).
- Application-level metrics (request rate/latency from inside the services themselves, via
  Micrometer on the JVM side / `prom-client` on the gateway) are **not** included — that's
  real code work across two stacks, deliberately deferred. What's here already covers the
  scaling story (replicas + CPU) without touching application code.

Approximate footprint: `prometheus-server` (256Mi/512Mi req/limit) + `kube-state-metrics`
(64Mi/128Mi) + `grafana` (96Mi/256Mi) ≈ **~420 MiB requested**, comfortably alongside a
gateway-only scaling test on a `--memory=2560` minikube.

## Prerequisite

`helm` CLI (same one used for `helm/`, see its README for install instructions).

## Install

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
# NB: grafana.github.io/helm-charts is deprecated/migrating — use the new repo:
helm repo add grafana-community https://grafana-community.github.io/helm-charts
helm repo update

helm upgrade --install prometheus prometheus-community/prometheus \
  -n observability --create-namespace \
  -f observability/values/prometheus.yaml

helm upgrade --install grafana grafana-community/grafana \
  -n observability \
  -f observability/values/grafana.yaml

kubectl -n observability get pods -w   # wait for both Deployments Ready
```

Release names (`prometheus`, `grafana`) and namespace (`observability`) matter: the Grafana
datasource URL hardcoded in `values/grafana.yaml` is
`http://prometheus-server.observability.svc.cluster.local` — installing under different
names means editing that URL to match.

## View it

```bash
kubectl -n observability port-forward svc/grafana 3000:80
```
Open `http://localhost:3000` (default admin credentials: `admin` / a random generated
password — retrieve it with
`kubectl -n observability get secret grafana -o jsonpath='{.data.admin-password}' | base64 -d`).
Dashboard: **Munchies → Scaling** in the left nav. It has a `namespace` textbox variable
(defaults to `gateway-service`) — change it to `user-service` / `order-service` /
`restaurant-service` to look at a different service.

Prometheus's own UI, if you want to run ad-hoc PromQL directly:
```bash
kubectl -n observability port-forward svc/prometheus-server 9090:80
```

## Run a benchmark with it

Bring this stack up, then run `./loadtest/run.sh` as usual — no changes needed there, it's
independent. Watch the **Munchies → Scaling** dashboard live during the run, and export a
panel (or just screenshot it) once the run finishes, for `docs/reports/spe/05-benchmark.md`.

## Tear down

```bash
helm uninstall grafana -n observability
helm uninstall prometheus -n observability
kubectl delete namespace observability
```

## Dashboard panels

| Panel | Query | Shows |
| --- | --- | --- |
| Ready replicas | `kube_deployment_status_replicas_ready{namespace="$namespace"}` | the Deployment's ready pod count over time |
| HPA current vs desired replicas | `kube_horizontalpodautoscaler_status_{current,desired}_replicas{namespace="$namespace"}` | what the HPA is asking for vs what's actually running |
| CPU usage per pod vs request | `rate(container_cpu_usage_seconds_total{namespace="$namespace"}[1m])` vs `kube_pod_container_resource_requests{...,resource="cpu"}` | the metric the HPA actually reacts to, and how close each pod is to the 60% threshold |
