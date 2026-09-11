# Helm chart

`munchies-service` is a generic chart for one microservice: Deployment + Service, an
optional HorizontalPodAutoscaler, and an optional dedicated MongoDB StatefulSet + PVC. Each
real service is just a small values file under `helm/values/`.

This replaced the duplication in `k8s/` — `k8s/order-service/`, `k8s/user-service/` and
`k8s/restaurant-service/` used to be near-identical copies of each other (the restaurant one
was literally cloned from order's with a handful of names changed). One chart + four
~20-line values files replaces that; the old `k8s/{gateway,user,order,restaurant}-service/`
folders were removed once the chart was proven equivalent.

**Status:** this is now the canonical source of truth for these four services.
`./gradlew deploy` / `./gradlew undeploy` (see `DeployServicesTask`/`UndeployServicesTask`)
detect a matching `helm/values/<service>.yaml` and run `helm upgrade --install` /
`helm uninstall` instead of raw `kubectl apply`/`delete` — no change to the command-line UX
you already use (`./gradlew deploy -Pservice=gateway-service` works exactly as before).
`kafka`, `payment-service`, `notification-service` and `table-reservation-service` still use
the raw manifests under `k8s/`, and both paths are discovered together by `-Pservice=all`.

Verification: rendering each values file and diffing it (structurally, not textually)
against the removed `k8s/<service>/*.yml` produced **zero differences**, aside from the
`Namespace` object (this chart doesn't template it — see below) and one deliberate addition,
a `helm.sh/resource-policy: keep` annotation on the Mongo PVC (see "Data on uninstall" below).
The Gradle wiring itself is compile-verified but **not exercised against a live cluster** in
this session — minikube was kept stopped throughout to avoid another OOM cycle. Treat your
first `./gradlew deploy -Pservice=<one of the four>` as the real end-to-end check.

## Prerequisite

Install the `helm` CLI (not required by the existing `k8s/`-based deploy path — only for using
this chart): https://helm.sh/docs/intro/install/. On Arch/Manjaro: `sudo pacman -S helm`.

## Usage

The normal path is unchanged from before Helm existed:

```bash
./gradlew deploy -Pservice=gateway-service      # or user-service / order-service / restaurant-service
./gradlew deploy                                # all services, kafka first (Helm-backed ones included)
./gradlew undeploy -Pservice=gateway-service
./gradlew undeploy [-PwipeData=true]
```

Sanity-check before trusting that against a real cluster (renders/dry-runs, touches nothing):
```bash
helm template gateway-service ./helm/munchies-service -n gateway-service -f helm/values/gateway-service.yaml
helm install gateway-service ./helm/munchies-service -n gateway-service --create-namespace \
  -f helm/values/gateway-service.yaml --dry-run
```

What `DeployServicesTask` runs under the hood, if you want to do it by hand:
```bash
helm upgrade --install gateway-service ./helm/munchies-service \
  -n gateway-service --create-namespace \
  -f helm/values/gateway-service.yaml

# after `minikube image load <service>:latest` rebuilds an image, the tag doesn't
# change, so force a rollout the same way DeployServicesTask does:
minikube kubectl -- rollout restart deployment -n gateway-service

# tear down
helm uninstall gateway-service -n gateway-service
```

## Data on uninstall

The Mongo `PersistentVolumeClaim` template carries `helm.sh/resource-policy: keep`, so
`helm uninstall` (what `./gradlew undeploy` runs) never deletes it — matching the project's
existing "keep data by default" convention. `UndeployServicesTask` deletes it explicitly,
same as for the raw-manifest services, only when you pass `-PwipeData=true`.

## Chart shape

| File | Purpose |
| --- | --- |
| `munchies-service/Chart.yaml` | chart metadata |
| `munchies-service/values.yaml` | every knob, documented, with safe defaults |
| `munchies-service/templates/deployment.yaml` | Deployment (image, env, resources, readiness probe) |
| `munchies-service/templates/service.yaml` | Service (ClusterIP/NodePort, port mapping) |
| `munchies-service/templates/hpa.yaml` | HorizontalPodAutoscaler, only rendered if `autoscaling.enabled` |
| `munchies-service/templates/mongodb.yaml` | PVC + StatefulSet + Service for the service's own Mongo, only rendered if `mongodb.enabled` |
| `munchies-service/templates/_helpers.tpl` | `shortName` — strips the `-service` suffix, mirroring the Gradle `getServiceName()` convention, so Mongo resources keep their existing names (`user-mongodb`, not `user-service-mongodb`) and every already-hardcoded `MONGODB_URI` keeps resolving |
| `values/gateway-service.yaml` | stateless: no `mongodb`, HPA on |
| `values/user-service.yaml`, `order-service.yaml`, `restaurant-service.yaml` | HPA on, `mongodb.enabled: true` |

Not templated: the `Namespace` object. `--create-namespace` handles it, which is the more
idiomatic Helm pattern (namespace lifecycle is usually considered a separate concern from a
single service's release) and keeps the chart simpler.

`payment-service`, `notification-service`, `table-reservation-service` and `kafka` are
intentionally **not** migrated — they're either incomplete or, for Kafka, a singleton that
doesn't benefit from a per-instance template. They keep using the raw manifests under `k8s/`.
