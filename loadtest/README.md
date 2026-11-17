# Load testing & scaling

k6-based load test used to verify that services scale horizontally under Kubernetes
(manual `kubectl scale` and the Horizontal Pod Autoscaler).

## Contents

| File | Purpose |
| --- | --- |
| `namespace.yml` | `loadtest` namespace for the k6 Job |
| `k6-script.configmap.yml` | the k6 script (`test.js`), mounted into the Job |
| `k6-job.yml` | the k6 runner Job — target URL and load profile are set via `env` here |
| `run.sh` | applies the Job and prints replica count + HPA CPU% every 10s next to the k6 output |
| `show.sh` | CLI summary tool to list and view load test metrics, timelines, and scaling graphs |
| `error-rate-analysis.md` | In-depth technical explanation of zero-error reliability under load |

The HPA itself lives with the service it scales, in `helm/values/gateway-service.yaml`
(`autoscaling:` block) — see `helm/README.md`. `gateway-service`, `user-service`,
`order-service` and `restaurant-service` are all deployed via that Helm chart, not raw
manifests under `k8s/`.

---

## Prerequisites

- A running minikube cluster (`docker` driver assumed).
- `metrics-server` — required for the HPA to read CPU:
  ```bash
  minikube addons enable metrics-server
  # wait ~45s, then check it has data:
  minikube kubectl -- top pods -A
  ```
- The service you want to scale-test, deployed and `Ready`. See the two options below.

> All commands use `minikube kubectl --` (no standalone `kubectl` needed). If you have
> `kubectl` on PATH, `export KUBECTL=kubectl` and the scripts pick it up.

---

## Option A — deploy with Gradle (normal machines)

The Gradle task builds each image, loads it into minikube, and applies the manifests — raw
`k8s/` manifests for kafka/payment/notification/table-reservation, `helm upgrade --install`
for gateway/user/order/restaurant (see `helm/README.md`).

```bash
# everything: kafka first, then every service (k8s/ and helm/ ones alike):
./gradlew deploy

# or one at a time:
./gradlew deploy -Pservice=kafka
./gradlew deploy -Pservice=user-service
./gradlew deploy -Pservice=gateway-service
```

Check it came up:

```bash
./gradlew k8sInfo
minikube kubectl -- get pods -A
```

Tear down (keeps PVCs/namespaces unless `-PwipeData=true`):

```bash
./gradlew undeploy
./gradlew undeploy -PwipeData=true
```

> **Heads-up:** `deploy` runs a nested `./gradlew :<svc>:dockerBuild` *while minikube is
> running*. The Kotlin/JS compile for a fresh build can spike memory by several GB. On a
> machine with < ~12 GB RAM this can get the minikube container OOM-killed mid-deploy
> (`no route to host` on the next command, `docker inspect minikube` shows
> `OOMKilled=true`). If that happens, use Option B.

---

## Option B — deploy without Gradle (low-memory machines)

Idea: **do the heavy compile while minikube is stopped**, then start a small cluster and
install by hand (`helm upgrade --install`, same command `DeployServicesTask` runs, minus the
nested Gradle process). No compile runs against the live cluster.

Needs `helm` — see `helm/README.md` for install instructions.

For the **gateway scaling test** you only need `gateway-service` — it has no Mongo and no
Kafka dependency, so this is the lightest possible setup.

```bash
# 1. build the image with the cluster DOWN (this is the memory-hungry step)
minikube stop
./gradlew :gateway-service:dockerBuild        # produces gateway-service:latest locally

# 2. start a small cluster
minikube start --memory=2560 --cpus=4
minikube addons enable metrics-server

# 3. load the pre-built image into minikube (no rebuild)
minikube image load gateway-service:latest

# 4. install via Helm (creates the namespace, Deployment, Service, HPA)
helm upgrade --install gateway-service ./helm/munchies-service \
  -n gateway-service --create-namespace \
  -f helm/values/gateway-service.yaml

# 5. wait until Ready
minikube kubectl -- get pods -n gateway-service -w
```

To redeploy after a code change, repeat steps 1–4 (stop → build → start → load → install).
`minikube kubectl -- rollout restart deploy -n gateway-service` forces pods onto the new
image (the tag is always `:latest`, so k8s won't notice otherwise; `helm upgrade` alone
won't trigger a restart when nothing in the values changed).

### Testing a JVM service instead (restaurant / user)

Those need a reachable Kafka (the Micronaut Kafka health check gates readiness) and their
own Mongo, so the footprint is larger — bump `--memory` to ~3800 and also deploy:

```bash
minikube stop
./gradlew :restaurant-service:dockerBuild
minikube start --memory=3800 --cpus=4
minikube addons enable metrics-server
minikube image load restaurant-service:latest    # kafka/mongo images pull from Docker Hub automatically

minikube kubectl -- apply -f k8s/kafka/namespace.yml
minikube kubectl -- apply -f k8s/kafka/
helm upgrade --install restaurant-service ./helm/munchies-service \
  -n restaurant-service --create-namespace \
  -f helm/values/restaurant-service.yaml
```

Each service ships its own HPA (`autoscaling:` in `helm/values/<svc>.yaml`, for gateway,
user, order, restaurant), installed automatically with the rest of the release. Point the
load test at the service you want to scale with `TARGET_URL` and `WATCH_NS` (see [Tuning the load](#tuning-the-load)).

---

## Running the scaling test

`run.sh` takes no arguments; it is configured through environment variables (see
[Tuning the load](#tuning-the-load)).

### 1. Manual scaling (no HPA)

Shows that the Service load-balances across replicas.

```bash
# make sure the HPA is NOT active, or it will fight manual scaling:
minikube kubectl -- delete hpa gateway-service -n gateway-service --ignore-not-found

minikube kubectl -- scale deploy/gateway-service -n gateway-service --replicas=1
SCALE_IN_TIMEOUT=0 ./loadtest/run.sh    # note the req/s served at the peak and the dropped iterations

minikube kubectl -- scale deploy/gateway-service -n gateway-service --replicas=3
SCALE_IN_TIMEOUT=0 ./loadtest/run.sh    # same load: more served, fewer dropped
```

### 2. HPA (autoscaling)

```bash
# recreates the HPA deleted in step 1 (helm upgrade reconciles the full release state)
helm upgrade --install gateway-service ./helm/munchies-service \
  -n gateway-service -f helm/values/gateway-service.yaml
minikube kubectl -- get hpa -n gateway-service          # TARGETS shows a %, not <unknown>

./loadtest/run.sh                        # prints "ready=N cpu%=X" every 10s
./loadtest/show.sh                       # view latest test report & scaling timeline
./loadtest/show.sh --list                # list all recorded test runs
# optional, second terminal:
minikube kubectl -- get hpa,pods -n gateway-service -w
```

This is the run the [Scaling Benchmark](../docs/reports/saap/03-Deployment/scaling-benchmark.md) is based on, about
14 minutes plus the scale-in wait. It targets `/auth/verify` with an open-model profile: k6 starts requests at a set
rate (`ramping-arrival-rate`), whatever the response time, so a slow service gets queued requests instead of fewer of
them. `PEAK_RATE` sets the peak (default 1000 req/s; the benchmark's overload run uses 2000):

| Phase | Load | Duration | Purpose |
| --- | --- | --- | --- |
| Start | 0 to 20 req/s | 30s | |
| Baseline | 20 req/s | 90s | the minimum replicas (2 for `gateway-service`), below the HPA target |
| Ramp | 20 req/s to `PEAK_RATE` | 1m | scale-up starts |
| Peak | `PEAK_RATE` | 4m | scale-up to the ceiling, then a steady state there |
| Drop | `PEAK_RATE` to 20 req/s | 30s | load falls |
| Low hold | 20 req/s | 6m | the service scales back in while requests are still served, so any request lost to scale-in is counted |
| End | 20 to 0 req/s | 30s | |

k6 keeps up to 400 requests in flight. When all of them are waiting on the service, it cannot start the next request
on time and counts it as a _dropped iteration_ instead: a run with dropped iterations offered less load than the
profile asked for, and the summary prints how many.

After k6 ends, `run.sh` keeps sampling until the service is back to its minimum replicas (at most 10 minutes,
`SCALE_IN_TIMEOUT`).

For `/auth/verify`, the k6 script asks the gateway's `/auth/token` for a fresh token before the load starts, so runs
never fail on an expired one. k6's thresholds are the project's SLOs (p95 below 800 ms, error rate below 1%), so a
run that misses them is reported as failed.

---

## Reading the output

`run.sh` streams the same two things to the terminal as before, and now **also saves them**
to `loadtest/results/<UTC timestamp>/` (git-ignored — copy the numbers you want into the
report, don't commit raw run dumps):

| File | Contents |
| --- | --- |
| `k6.log` | raw k6 stdout: live progress line + the `handleSummary()` text block |
| `summary.json` | k6's full summary object (all metrics, all percentiles), extracted from `k6.log` |
| `scaling.csv` | `timestamp,ready_replicas,cpu_percent`, sampled every 10s for the whole run |
| `hpa-events.txt` | `kubectl describe hpa` + `kubectl get events --sort-by=.lastTimestamp`, captured once at the end |
| `environment.txt` | node CPU and memory, HPA spec and pod resources, captured before the load starts |

The Job's own filesystem disappears with the pod, so getting `summary.json` out doesn't use
`kubectl cp` — the k6 script's `handleSummary()` prints the JSON as a single line prefixed
with `K6_JSON:`, and `run.sh` greps that prefix out of the log it's already tailing.

Terminal output during and after the run:

- **k6 summary block** (`=== k6 summary ===` … `===================`) — throughput (req/s),
  latency avg/p95/p99/max, error rate. Same numbers as `summary.json`, human-readable.
- **watcher line** every 10s: `[HH:MM:SS] gateway-service ready=N cpu%=X`
  — `ready` = current ready replicas, `cpu%` = HPA's averaged CPU utilisation.
- **live progress line**, e.g. `running (4m13.0s), 225/400 VUs, 162259 complete and 0 interrupted iterations`
  = elapsed time, busy VUs (requests in flight) / VUs allocated, cumulative completed iterations
  (≈ requests), and interrupted iterations (should stay 0).

Interpreting it:
- throughput roughly flat while `ready` climbs → the new replicas are not getting the load: k6's
  keep-alive connections opened before the scale-up stay on the old pods (the Service balances
  per connection), or the bottleneck is elsewhere (k6 pod CPU, the single node's cores). The
  gateway closes each connection after 20 requests, so for it this should clear within seconds;
  if it does not, check that the pods run the current image (`minikube image ls`).
- `cpu%` pegged well above 60 and `ready` marching to `maxReplicas` (8 for `gateway-service`, 4 for the others) → HPA working.
- `scaling.csv` plotted over time is the evidence to paste into the Benchmark report
  (replicas ramping up during the peak, back down during the low hold).

---

## Tuning the load

Set these when calling `run.sh`, e.g. `PEAK_RATE=2000 ./loadtest/run.sh`; it writes them into the Job's `env`:

| Var | Default | Meaning |
| --- | --- | --- |
| `PEAK_RATE` | `1000` | peak requests per second |
| `TARGET_URL` | gateway `/auth/verify` (in-cluster FQDN) | endpoint to load |
| `WATCH_NS` | `gateway-service` | namespace whose replicas and HPA are sampled |
| `SCALE_IN_TIMEOUT` | `600` | most seconds to wait for scale-in after k6 ends; `0` to stop when k6 does |

The shape of the load (durations and the baseline rate) is the `STAGES` list at the top of the script in
`k6-script.configmap.yml`; edit it for a different profile. For a gentler HPA ramp, raise `resources.requests.cpu`
(and `limits.cpu`) in `helm/values/gateway-service.yaml` — the HPA percentage is measured against the request.

Pin the k6 image (`grafana/k6:latest` → e.g. `grafana/k6:0.55.0`) for reproducible numbers.

---

## Troubleshooting

**`no route to host` to `192.168.49.2:8443`** — the API server is unreachable.
```bash
minikube status
docker inspect minikube --format '{{.State.Status}} OOMKilled={{.State.OOMKilled}}'
```
If `OOMKilled=true`: the compile starved the machine. Recreate smaller and use Option B:
```bash
minikube delete && minikube start --memory=2560 --cpus=4
```

**HPA `TARGETS` shows `<unknown>`** — metrics-server not ready yet (wait ~1 min) or the
Deployment has no `resources.requests.cpu` (it does — `100m`).

**k6 pod `ImagePullBackOff`** — no internet for Docker Hub; `minikube image load grafana/k6:latest`
from a machine that can pull it.

**Load test can't resolve the target** — `TARGET_URL` must be the cross-namespace FQDN
`http://<svc>.<ns>.svc.cluster.local:8080/...` since k6 runs in the `loadtest` namespace.
