# Scaling Benchmark

This benchmark verifies, under real load on a real cluster, that the autoscaling described in
[Deployment on Kubernetes](kubernetes.md) works end to end and keeps the service within its service level objectives:
p95 latency below 800 ms and an error rate below 1%, with no requests lost to scaling itself.

The configuration as first designed missed two of the three objectives. The analysis of that run led to three changes,
and the same test on the changed configuration meets all three. The chapter compares the two runs; their raw output is
under `loadtest/results/`:

| Run | Configuration |
|---|---|
| `20261106T110107Z` | **Initial**: 1 to 8 replicas |
| `20261109T143809Z` | **Final**: 2 to 8 replicas, connections rebalanced, graceful shutdown |

## Setup

**Workload.** The load targets `gateway-service`, the entry point of every request, on `/auth/verify`: a benchmark
endpoint that checks the bearer token with `jwt.verify` (HS256), the same check the gateway's auth middleware runs on
every authenticated request. Each request therefore costs the CPU a real one does, and the gateway answers it itself,
without calling other services, MongoDB or Kafka, so the test measures the scaling mechanism and nothing behind it.

**Load.** A [k6](https://k6.io) Job inside the cluster (`./loadtest/run.sh`) runs an _open model_
(`ramping-arrival-rate`): it starts requests at a set rate whatever the response time, as independent users would, so
a slow service gets requests queued on it rather than fewer of them. k6 keeps at most 400 requests in flight, each on
its own virtual user (VU) with its own keep-alive connection; when all 400 are waiting, the next request cannot start
and is counted as a _dropped iteration_.

Times are offsets from the start of the ramp, as in the timelines of the runs below:

| Phase | Time | Load | Purpose |
|---|---|---|---|
| Baseline | -2:00 to +0:00 | 20 req/s | the minimum number of replicas, well below the HPA target |
| Ramp | +0:00 to +1:00 | 20 to 1,000 req/s | the load rises past what the starting replicas can handle, so scale-up starts |
| Peak | +1:00 to +5:00 | 1,000 req/s | scale-up to the ceiling, then a steady state |
| Low hold | +5:00 to +12:00 | 20 req/s | scale-in while requests are still served, so any request lost to it is counted |

**Autoscaler.** Target 60% of the `100m` CPU request, scale-up by up to 4 pods every 30 s, scale-down by 1 pod every
30 s, at most 8 replicas, CPU limited to one core per pod. The ceiling, the step and the limit are raised from the
chart's defaults for `gateway-service` only.

**Environment.** Single-node Minikube (Docker driver) on a laptop on mains power, with `metrics-server` providing the
CPU readings. The node sees all 8 hardware threads of the host (an Intel i5-8250U, 4 physical cores) and 3,800 MiB of
memory, and k6 runs on the same node as the pods it loads. Each run records the environment, k6's output and summary,
the replica count and CPU reading every 10 s, and the HPA's events (`environment.txt`, `k6.log`, `summary.json`,
`scaling.csv`, `hpa-events.txt`).

A calibration run held one replica alone at full load: it served about 700 req/s, with a p95 of 93 ms. The peak of
1,000 req/s is therefore more than one replica can take, but within what a few can.

## Initial configuration

| Metric | Value |
|---|---|
| Requests | 275,223 |
| Failed requests | 104 (0.04%): 103 timeouts during scale-up, 1 refused connection during scale-in |
| Dropped iterations | 20,277 |
| Latency median / p90 / p95 / p99 | 6.1 ms / 712 ms / 867 ms / 1,134 ms |

In both timelines, times are offsets from the start of the ramp, as in the phase table. Throughput and busy VUs are
per 10 s window. The CPU column is the HPA's reading, which `metrics-server` refreshes about once a minute: a value
appears only on the row where a new reading arrived, and a range row lists the readings that arrived within it, in
order of arrival.

| Time | Served (req/s) | Busy VUs (requests in flight) | Ready replicas | CPU (% of request) | What is happening |
|---|---|---|---|---|---|
| +0:40 | 628 | 138 | 1 | | the load rises past what one replica can handle; requests queue |
| +0:50 | 652 | 399 | 1 | | all 400 VUs waiting: k6 drops iterations |
| +0:56 | | | 1 | **479%** | first CPU reading that shows the load |
| +1:06 | 587 | 399 | 4 | | **scale-up 1 to 5** |
| +1:37 | 680 | 400 | 7 | | **scale-up 5 to 8**; throughput still that of one replica |
| +1:50 | 1,003 | 262 | 7 | | 103 requests time out after 60 s; throughput reaches the load |
| +1:58 to +4:50 | 996 to 1,007 | 218 to 240 | 7 | 992%, 377%, 331% | steady state |
| +7:00 to +10:15 | 20 | 0 | 8 to 1 | 8%, 11%, 13%, 21% | first reading after the drop, then scale-in, one pod every 30 s; one request refused |

The 992% reading at +1:58 is most likely the original replica alone, at its one-core limit: the HPA averages only the
pods that already report metrics, and the replicas started in the previous minute probably had none yet.

The HPA itself behaved as configured. The first CPU reading that showed the overload arrived 16 seconds after it began,
and the HPA acted on it 10 seconds later: `ceil(1 × 479 / 60) = 8` replicas wanted, 5 allowed by the scale-up step,
and 8 at the next evaluation. Two things went wrong around it.

**The new replicas were ready but got no load for 45 seconds.** kube-proxy picks a pod when a connection is opened, not
for each request (see [Service discovery](kubernetes.md#service-discovery)). k6 opened its 400 connections during the
overload, while there was a single replica, and a keep-alive connection is reused for every request on it: all of
them stayed on the original replica after the new ones were ready. Throughput rose only when 103 requests hit k6's
60 s timeout and their VUs reconnected, this time spread over the seven ready replicas. The VUs still connected to the
original replica kept waiting on it for the rest of the peak, which is what pushed p95 over the objective. A separate
check confirmed the mechanism: with a fixed set of connections on one replica, three replicas added beside it stayed at
the CPU of their probes alone while the original one stayed at its one-core limit.

**Scale-in lost one request.** When Kubernetes removes a pod, it does two things at the same time: it takes the pod
out of the `Service`, which kube-proxy applies on the node a moment later, and it sends the pod `SIGTERM`. The gateway
did not handle `SIGTERM`, so it exited at once. For that moment, the process was gone but kube-proxy still routed new
connections to it: at +7:06 one of k6's connections arrived in that gap and was refused. Whether a request falls in
the gap is a matter of timing, so the loss is intermittent: this is the _shutdown race_.

A repeat of this run on 9 November reproduced the scale-up sequence closely (p95 955 ms, 99 timeouts) and lost no
request at scale-in.

## Changes

| Change | Where | What it fixes |
|---|---|---|
| **Two replicas as the floor** (`minReplicas: 2`) | `helm/values/gateway-service.yaml` | a single replica takes the whole load until the autoscaler reacts, and is a single point of failure |
| **Rebalanced connections**: the gateway closes each connection after 20 requests | `gateway-service/src/main/ts/index.ts` | connections stay on the pods that existed when they were opened |
| **Graceful shutdown**: a 10 s `preStop` sleep and a `SIGTERM` handler | `helm/munchies-service` (`shutdown.preStopSleepSeconds`), `index.ts` | a pod stops before it is out of the `Service` |

**Rebalanced connections.** Node's HTTP server closes a keep-alive connection after a set number of requests
(`server.maxRequestsPerSocket`), answering the last one with `Connection: close`. The client reconnects and kube-proxy
picks a pod again, so the load moves onto new replicas within seconds, at the cost of one TCP handshake per 20
requests.

**Graceful shutdown.** The `preStop` hook makes Kubernetes wait 10 seconds before sending `SIGTERM`, so the pod is
out of the `Service` well before the process stops, and the gap of the shutdown race no longer exists. On `SIGTERM`
the gateway stops accepting connections, closes the idle ones, lets the requests in progress finish and exits, within
15 s, inside the pod's 30 s grace period.

The 2 replicas floor alone was not enough. A run with only that change passed the k6 thresholds narrowly (p95 617 ms): k6's
connections, all opened while two replicas were ready, stayed on those two for the whole peak, since none timed out to
force a reconnection. That run also lost a request at scale-in.

## Final configuration

| Metric | Value |
|---|---|
| Requests | 293,434 |
| Failed requests | 0 |
| Dropped iterations | 2,066, all between +0:55 and +1:07 |
| Latency median / p90 / p95 / p99 | 2.8 ms / 50 ms / 109 ms / 292 ms |

| Time | Served (req/s) | Busy VUs (requests in flight) | Ready replicas | CPU (% of request) | What is happening |
|---|---|---|---|---|---|
| +0:40 | 737 | 87 | 2 | | the load rises past what two replicas can handle; requests queue |
| +0:50 | 756 | 398 | 2 | | all 400 VUs waiting from +0:55: k6 drops iterations |
| +0:53 | | | 2 | **403%** | first CPU reading that shows the load |
| +1:00 | 969 | 50 | 6 | | **scale-up 2 to 6**; the queue clears by +1:07 |
| +1:10 to +5:00 | 983 to 1,017 | 3 to 19 | 5 to 8 | 643%, 393%, 385%, 393% | steady state; **scale-up 6 to 8** at +1:36 |
| +7:02 to +9:47 | 20 | 0 | 8 to 2 | 8%, 11%, 13% | first reading after the drop, then scale-in, one pod every 30 s; no request lost |

**The new replicas took their share within seconds.** From +1:10 the service served the full 1,000 req/s with 3 to
19 requests in flight, against 218 to 240 in the initial run, and the median request took 2.8 ms. The CPU reading,
about 390% of the request on each of the 8 replicas, shows the load spread over all of them.

**What remains is the wait for the autoscaler.** The two replicas were overloaded from +0:46, the first new ones were
ready at +1:03, and the queue had cleared by +1:07: the 2,066 dropped iterations fall in those 21 seconds. The changes
shorten this window and limit its effect, but its length is set by how often `metrics-server` reports and how long a pod takes to start.

**Scale-in lost no request**, over six steps from 8 replicas to 2 under live traffic.

## Against the service level objectives

The k6 thresholds are the first two SLOs, so k6 itself passes or fails a run on them.

| SLO | Objective | Initial | Met | Final | Met |
|---|---|---|---|---|---|
| Request latency | p95 below 800 ms | 867 ms | no | 109 ms | **yes** |
| Error rate | below 1% | 0.04% | yes | 0% | **yes** |
| Availability during scaling | no requests lost to scaling | 104 lost | no | none lost | **yes** |

Dropped iterations are requests k6 never sent, so they are not in the latency figures. Counting all 2,066 of the final
run as slower than 800 ms would still put under 2% of the requests over the objective, within the 5% that p95 allows.

## Limitations and further work

- **Shared node.** k6 and the pods share the laptop's 4 physical cores, so the absolute numbers show the mechanism,
  not the gateway's capacity on dedicated hardware.
- **Few, long-lived connections.** k6's 400 connections are the worst case for per-connection balancing; many
  independent clients reconnect more often. The gateway's own connections to the services behind it are still not
  rebalanced; a layer 7 proxy (an ingress controller or a service mesh) would balance per request everywhere.
- **Few repetitions.** The initial run was reproduced once, the final run was made once, and neither configuration was
  tested above the ceiling's capacity. This matters most for the shutdown race: before the fix it lost a request in
  only some runs (one in the initial run, none in its repeat), so the final run's six clean scale-in steps could
  partly be luck. The fix removes the gap by design, but only many more scale-ins under load would prove it.
- **Scaling still waits for the CPU reading.** Lowering `metrics-server`'s `--metric-resolution` to 15 s, or scaling on
  the request rate through a custom-metrics adapter (Prometheus Adapter or KEDA), would shorten the remaining overload.
- **Scope.** Only `gateway-service`, only the CPU metric, on a single node, on an endpoint that answers instead of
  proxying. The JVM services carry the same HPA but were not loaded.

## Reproducing this benchmark

```bash
minikube start --memory=3800 --cpus=4
minikube addons enable metrics-server
./gradlew deploy -Pservice=gateway-service

# wait for 2 replicas and a CPU reading below 60%, then run (about 20 minutes)
minikube kubectl -- get hpa -n gateway-service
./loadtest/run.sh
./loadtest/show.sh
```

Compare `environment.txt` with the recorded one before comparing results. If Minikube already held a
`gateway-service:latest` image, check with `minikube image ls` that the deploy replaced it; during these runs it did
not, and loading the image with `docker save gateway-service:latest | minikube ssh --native-ssh=false -- docker load`,
then restarting the deployment, did.
