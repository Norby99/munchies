# Observability (Prometheus + Loki + Grafana)

A lightweight stack implementing the Application Metrics and Log Aggregation sides of the
Observability pattern. It records three things and shows them in one Grafana:

- **How the services behave**: request rate, error ratio and latency of every instrumented
  service, measured from inside the application.
- **How the services scale under load**: ready replicas, HPA current vs desired replicas, and
  per-pod CPU usage against the CPU request.
- **What the services log**: the structured logs of every service, collected from the pods
  and searchable in one place.

The stack is **always-on**: `./gradlew deploy` installs it (the `deployObservability` task)
and `./gradlew undeploy` removes it. Deploying a single service with `-Pservice=<name>`
leaves it untouched. Used together with `loadtest/`, it turns a benchmark run into time
series and dashboards.

## Contents

| Path | Purpose |
| --- | --- |
| `values/prometheus.yaml` | Helm values for the `prometheus-community/prometheus` chart |
| `values/loki.yaml` | Helm values for the `grafana-community/loki` chart |
| `values/alloy.yaml` | Helm values for the `grafana/alloy` chart, including the inlined collection pipeline |
| `values/grafana.yaml` | Helm values for the `grafana-community/grafana` chart, including the provisioned datasources and the inlined dashboards |
| `dashboards/scaling.json` | Standalone copy of the **Munchies — Scaling** dashboard, for reference or manual import |
| `dashboards/services.json` | Standalone copy of the **Munchies — Services** dashboard, for reference or manual import |
| `dashboards/logs.json` | Standalone copy of the **Munchies — Logs** dashboard, for reference or manual import |

## Components

- **`prometheus-community/prometheus`**: the classic (non-operator) chart, a plain
  Prometheus `Deployment` with a ConfigMap-based scrape config and no CRDs. Alertmanager,
  `prometheus-node-exporter` and `prometheus-pushgateway` are disabled to keep the footprint
  small. Data lives on a 2Gi volume with a 3 day retention, and targets are scraped every
  15s so that a load test lasting a few minutes still yields usable series.
  - **`kube-state-metrics`** is kept: it exports Deployment and HPA object state
    (`kube_deployment_status_replicas_ready`, `kube_horizontalpodautoscaler_status_*_replicas`)
    as metrics, which is what the replica graphs are built on.
  - Per-container CPU usage comes from the chart's default **`kubernetes-nodes-cadvisor`**
    scrape job, the same cAdvisor data `metrics-server` uses to drive the HPA, retained here
    as history.
- **`grafana-community/loki`**: log storage, run as a single process (`Monolithic` mode)
  writing to a 2Gi volume with a 3 day retention. No object storage, gateway or caches.
- **`grafana/alloy`**: the log collector, a single-replica Deployment. It discovers the pods
  of the application namespaces, tails their logs through the Kubernetes API and pushes them
  to Loki, so no host directory is mounted.
- **`grafana-community/grafana`**: trimmed resources, no persistence. The Prometheus and Loki
  datasources and all dashboards are fully provisioned from `values/grafana.yaml`, so no
  manual setup is needed.

## Application metrics

Each instrumented service exposes its metrics over HTTP and is discovered through pod
annotations (`prometheus.io/scrape`, `prometheus.io/path`, `prometheus.io/port`), which the
chart's default `kubernetes-pods` scrape job picks up. Nothing in a service knows where
Prometheus is. The annotations come from the `metrics` values of `helm/munchies-service`, or
directly from the manifests under `k8s/` for the services not yet migrated to the chart.

| Service | Library | Endpoint | Metrics |
| --- | --- | --- | --- |
| `user-service`, `order-service`, `restaurant-service` | Micrometer + Prometheus registry | `/prometheus` | HTTP server requests, JVM, plus what Micronaut binds by default |
| `gateway-service`, `payment-service` | `prom-client` | `/metrics` | HTTP server requests, Node.js runtime |
| `notification-service` | `prom-client` | `/metrics` | `notifications_received_total` by notification type, Node.js runtime |

Both stacks publish request latency under the same name and labels,
`http_server_requests_seconds` with `method`, `uri` and `status`, so one query covers every
service. `uri` holds the route template (`/orders/{id}`, `/orders/:id`), never the concrete
path, to keep the number of series bounded. `notification-service` has no business HTTP
traffic, as it only consumes Kafka events, so it reports a counter instead.

Every service runs in a namespace named after itself, which is why the dashboards group and
filter by the `namespace` label.

## Logs

Services only write to stdout, one JSON object per line. Nothing in a service knows where
Loki is: Alloy picks the lines up from the pods and labels them with `namespace`, `pod`,
`container` and `app`, the same names Prometheus uses, plus `level`, taken from the JSON.

| Service | Library | Notes |
| --- | --- | --- |
| `user-service`, `order-service`, `restaurant-service` | Logback `JsonEncoder` | selected with `LOG_FORMAT=json`, set by `helm/values`; plain text otherwise, for local runs |
| `gateway-service`, `payment-service`, `notification-service` | `pino` | always JSON; verbosity set with `LOG_LEVEL` (default `info`) |

- **Shared Logback setup**: the appender lives in `micronaut-commons`
  (`munchies-logback-text.xml`, `munchies-logback-json.xml`) and each service's `logback.xml`
  includes one of them, keeping only its own logger levels.
- **Request logs**: every service that serves HTTP logs one line per handled request, through
  Micronaut's access logger or `pino-http`. `/health`, `/metrics` and `/prometheus` are
  excluded.
- **Level**: both stacks write the severity as an upper-case name (`INFO`, `WARN`, `ERROR`),
  so `{level="ERROR"}` selects errors from any service.
- **What is not logged**: request and response bodies, headers, cookies and query strings.
  They carry credentials and tokens, which must not reach a shared log store.

The MongoDB pods of the services are not collected: Alloy drops every container named
`mongodb`. Lines that are not JSON, such as those of the Kafka pod, are stored as they are,
without a `level` label.

## Footprint

| Component | Memory request / limit |
| --- | --- |
| `prometheus-server` | 256Mi / 512Mi |
| `kube-state-metrics` | 64Mi / 128Mi |
| `loki` | 192Mi / 384Mi |
| `alloy` | 128Mi / 256Mi |
| `grafana` | 192Mi / 512Mi |

About **830 MiB requested** in total.

## Prerequisite

`helm` CLI (the same one used for `helm/`, see its README for install instructions).

## Install

`./gradlew deploy` does this as part of a full deployment. To install or upgrade the stack on
its own:

```bash
./gradlew deployObservability
```

which is equivalent to:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
# NB: the Grafana and Loki charts moved to the community repo; Alloy is still published
# in the original one.
helm repo add grafana-community https://grafana-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

helm upgrade --install prometheus prometheus-community/prometheus \
  -n observability --create-namespace \
  -f observability/values/prometheus.yaml

helm upgrade --install loki grafana-community/loki \
  -n observability \
  -f observability/values/loki.yaml

helm upgrade --install alloy grafana/alloy \
  -n observability \
  -f observability/values/alloy.yaml

helm upgrade --install grafana grafana-community/grafana \
  -n observability \
  -f observability/values/grafana.yaml
```

Release names (`prometheus`, `loki`, `grafana`) and namespace (`observability`) matter: the
datasource URLs in `values/grafana.yaml` and the push URL in `values/alloy.yaml` are built
from them (`http://prometheus-server.observability.svc.cluster.local`,
`http://loki.observability.svc.cluster.local:3100`). Installing under different names
requires editing those URLs to match.

## View it

```bash
kubectl -n observability port-forward svc/grafana 3000:80
```

Open `http://localhost:3000`. The admin user is `admin`; the password is randomly generated
and can be retrieved with:

```bash
kubectl -n observability get secret grafana -o jsonpath='{.data.admin-password}' | base64 -d
```

The dashboards are under the **Munchies** folder in the left nav: **Services**, **Scaling**
and **Logs**. The first two have a `namespace` textbox variable (defaults to
`gateway-service`); change it to another service's namespace, e.g. `order-service`, to
inspect that service. **Logs** takes a regular expression instead (defaults to `.+-service`,
every service) and a **Contains** box to search the lines.

For ad-hoc log queries use **Explore** with the Loki datasource, for example:

```logql
{namespace="order-service"} | json
{namespace=~".+-service", level="ERROR"}
```

To run ad-hoc PromQL directly against Prometheus:

```bash
kubectl -n observability port-forward svc/prometheus-server 9090:80
```

## Run a benchmark with it

Run `./loadtest/run.sh` as usual; the two are independent and no changes are needed there.
Watch the **Munchies** dashboards live during the run, and export or screenshot the panels
once it finishes (e.g. for `docs/reports/spe/05-benchmark.md`).

## Tear down

`./gradlew undeploy` removes the stack together with the services. On its own:

```bash
./gradlew undeployObservability                  # uninstall the four releases
./gradlew undeployObservability -PwipeData=true  # also delete the namespace
```

## Dashboard panels

### Munchies — Scaling

| Panel | Query | Shows |
| --- | --- | --- |
| Ready replicas | `kube_deployment_status_replicas_ready{namespace="$namespace"}` | the Deployment's ready pod count over time |
| HPA current vs desired replicas | `kube_horizontalpodautoscaler_status_{current,desired}_replicas{namespace="$namespace"}` | what the HPA is asking for vs what is actually running |
| CPU usage per pod vs request | `rate(container_cpu_usage_seconds_total{namespace="$namespace"}[1m])` vs `kube_pod_container_resource_requests{...,resource="cpu"}` | the metric the HPA reacts to, and how close each pod is to the 60% threshold |

### Munchies — Services

Requests to `/health`, `/metrics` and `/prometheus` are excluded from every panel, so probes
and scrapes do not dilute the traffic figures.

| Panel | Built on | Shows |
| --- | --- | --- |
| Request rate by service | `rate(http_server_requests_seconds_count)` by `namespace` | handled requests per second, per service |
| Error ratio by service | the same rate restricted to `status=~"5.."`, over the total | share of requests answered with a 5xx |
| Latency p95 by service | `histogram_quantile(0.95, ...)` on `http_server_requests_seconds_bucket` | 95th percentile of request duration, aggregated across replicas |
| Latency p50 / p95 / p99 | the same histogram, for `$namespace` | latency distribution of the selected service |
| Request rate by route | the request rate by `method` and `uri`, for `$namespace` | which routes of the selected service carry the traffic |
| Requests by status | the request rate by `status`, for `$namespace` | response codes of the selected service |
| Notifications consumed | `rate(notifications_received_total)` by `type` | events `notification-service` consumed from Kafka |
| Heap used by pod | `jvm_memory_used_bytes{area="heap"}` and `nodejs_heap_size_used_bytes` | memory pressure per pod of the selected service |

### Munchies — Logs

| Panel | Built on | Shows |
| --- | --- | --- |
| Log lines by service | `rate({namespace=~"$namespace"})` by `namespace` | how much each service is logging |
| Warnings and errors by service | the same rate restricted to `level=~"WARN\|ERROR"` | when and where problems are being reported |
| Logs | `{namespace=~"$namespace"} \|= "$search"` | the log lines themselves, newest first |
