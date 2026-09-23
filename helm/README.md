# Helm chart

`munchies-service` is a generic chart for one microservice: Deployment + Service, an
optional HorizontalPodAutoscaler, and an optional dedicated MongoDB StatefulSet + PVC. Each
real service is just a small values file under `helm/values/`.
This replaced the duplication in `k8s/` from `k8s/order-service/`, `k8s/user-service/` and
`k8s/restaurant-service/` that used to be almost identical copies of each other.

## Prerequisite

`helm` CLI has to be installed.

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
