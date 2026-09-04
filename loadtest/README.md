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

The HPA itself lives with the service it scales: `k8s/gateway-service/hpa.yml`.

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

The Gradle task builds each image, loads it into minikube, and applies the manifests.

```bash
# everything under k8s/ (kafka first, then every service dir):
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
apply the plain manifests by hand. No nested Gradle process runs against the live cluster.

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

# 4. apply the manifests by hand — namespace FIRST, then the rest
minikube kubectl -- apply -f k8s/gateway-service/namespace.yml
minikube kubectl -- apply -f k8s/gateway-service/

# 5. wait until Ready
minikube kubectl -- get pods -n gateway-service -w
```

To redeploy after a code change, repeat steps 1–4 (stop → build → start → load → apply).
`minikube kubectl -- rollout restart deploy -n gateway-service` forces pods onto the new
image (the tag is always `:latest`, so k8s won't notice otherwise).

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
minikube kubectl -- apply -f k8s/restaurant-service/namespace.yml
minikube kubectl -- apply -f k8s/restaurant-service/
```

Each service ships its own HPA (`k8s/<svc>/hpa.yml` for gateway, user, order, restaurant),
applied automatically with the rest of the folder. Point the load test at the service you
want to scale via `TARGET_URL` in `k6-job.yml`.

---

## Running the scaling test

### 1. Manual scaling (no HPA)

Shows that the Service load-balances across replicas.

```bash
# make sure the HPA is NOT active, or it will fight manual scaling:
minikube kubectl -- delete hpa gateway-service -n gateway-service --ignore-not-found

minikube kubectl -- scale deploy/gateway-service -n gateway-service --replicas=1
./loadtest/run.sh                       # note req/s and p95 from the k6 summary

minikube kubectl -- scale deploy/gateway-service -n gateway-service --replicas=3
./loadtest/run.sh                       # same load → expect ~2-3x req/s, lower p95
```

### 2. HPA (autoscaling)

```bash
minikube kubectl -- apply -f k8s/gateway-service/hpa.yml
minikube kubectl -- get hpa -n gateway-service          # TARGETS shows a %, not <unknown>

./loadtest/run.sh                        # prints "ready=N cpu%=X" every 10s
# optional, second terminal:
minikube kubectl -- get hpa,pods -n gateway-service -w
```

Expected: replicas climb during the 3-minute sustain phase, then step back down ~30s
after the load stops (`scaleDown.stabilizationWindowSeconds: 30` in `hpa.yml`).

`run.sh` takes the watched namespace as `$1` (default `gateway-service`):

```bash
./loadtest/run.sh restaurant-service
```

---

## Reading the output

`run.sh` streams two things to the terminal (nothing is saved to a file):

- **k6 end-of-test summary** — the important numbers:
  - `http_reqs` .......... total requests + **rate (req/s)** = aggregate throughput
  - `http_req_duration` .. avg / med / **p(95)** / p(99) / max latency
  - `http_req_failed` .... error rate (threshold: < 1%)
  - `iterations` ......... = requests here (1 GET per iteration)
  - `✓ / ✗` on thresholds
- **watcher line** every 10s: `[HH:MM:SS] gateway-service ready=N cpu%=X`
  - `ready` = current ready replicas, `cpu%` = HPA's averaged CPU utilisation

The live progress line during the run:
```
running (4m13.0s), 150/150 VUs, 662259 complete and 0 interrupted iterations
```
= elapsed time, active VUs (virtual users) / target, cumulative completed iterations (≈ requests),
and interrupted iterations (should stay 0).

Interpreting it:
- throughput roughly flat while `ready` climbs → bottleneck is elsewhere
  (k6 pod CPU, the single node's cores, or the Service), not the app.
- `cpu%` pegged well above 60 and `ready` marching to `maxReplicas` (4) → HPA working.

For richer analysis, capture after the run:
```bash
minikube kubectl -- describe hpa gateway-service -n gateway-service
minikube kubectl -- get events -n gateway-service --sort-by=.lastTimestamp
```

---

## Tuning the load

Edit `env` in `k6-job.yml` before running:

| Var | Default | Meaning |
| --- | --- | --- |
| `TARGET_URL` | gateway `/health` (in-cluster FQDN) | endpoint to hammer |
| `PEAK_VUS` | `150` | peak concurrent virtual users |
| `SUSTAIN` | `3m` | how long to hold peak load |

`/health` is cheap — if `cpu%` won't cross 60, raise `PEAK_VUS` to 300–400.
For a gentler HPA ramp, raise `resources.requests.cpu` (and `limits.cpu`) in
`k8s/gateway-service/deployment.yml` — the HPA percentage is measured against the request.

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
