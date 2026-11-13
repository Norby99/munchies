# Deployment

Munchies is built from source and runs in containers. No cloud account is needed, as the whole system fits on a single
machine (`IR3`). There are two ways to run it:

| Mode           | Runs on            | Scaling and recovery | Observability | Use it to                                             |
|----------------|--------------------|----------------------|---------------|-------------------------------------------------------|
| Docker Compose | Docker             | No                   | No            | Try the application, on a lighter machine and testing |
| Kubernetes     | Minikube, and Helm | Yes                  | Yes           | Get the full system, as it was designed               |

## Prerequisites

| Tool           | Version | Needed for                               | Check                    |
|----------------|---------|------------------------------------------|--------------------------|
| Git            | Any     | Getting the sources                      | `git --version`          |
| JDK            | 21      | Running Gradle, and building Kotlin code | `java -version`          |
| Node.js, npm   | 24      | Building the Express services            | `node --version`         |
| Docker         | Any     | Building images, and running containers  | `docker info`            |
| Docker Compose | V2      | Compose mode only                        | `docker compose version` |
| Minikube       | Any     | Kubernetes mode only                     | `minikube version`       |
| Helm           | 3       | Kubernetes mode only                     | `helm version`           |

Gradle is not listed, because the wrapper in the repository downloads it. The Docker daemon has to be running before
each of the following steps.

Get the sources, and run all the commands below from the root of the repository:

```bash
git clone https://github.com/Norby99/munchies.git
cd munchies
```

## Docker Compose

### Start

```bash
./gradlew composeUp
```

The command compiles all the modules, builds the images, and starts the containers, waiting for them to open their
ports. The first run takes several minutes, since it also downloads dependencies and base images.

### Expected outcome

The command ends with no errors, and `docker ps` lists the containers below, along with Kafka and four MongoDB
instances.

| Service                | URL on the host         |
|------------------------|-------------------------|
| `frontend-service`     | `http://localhost:5173` |
| `gateway-service`      | `http://localhost:8086` |
| `order-service`        | `http://localhost:8081` |
| `user-service`         | `http://localhost:8082` |
| `payment-service`      | `http://localhost:8083` |
| `restaurant-service`   | `http://localhost:8087` |
| `notification-service` | Not exposed             |

The gateway answers its health check:

```bash
curl http://localhost:8086/health
```

```json
{
  "status": "UP"
}
```

Opening `http://localhost:5173` in a browser shows the application.

### Start with health checks

`scripts/deploy.sh`, which is also attached to each GitHub Release, updates the sources, restarts the system, and then
polls the health endpoints of the services:

```bash
./scripts/deploy.sh
```

It ends with `Deployment completed successfully. All services are up.`

### Stop

```bash
./gradlew composeDown            # Keeps the data
./gradlew composeDown -Preset    # Also wipes the data
```

## Kubernetes

### Start

Start the cluster, and enable `metrics-server`, which the autoscaler needs to read CPU usage:

```bash
minikube start
minikube addons enable metrics-server
```

Then deploy:

```bash
./gradlew deploy
```

The command builds each image, loads it into Minikube, and installs the services one namespace at a time, starting
from Kafka. It ends by installing the observability stack.

This is demanding on memory, as it compiles the services while the cluster is running. On a smaller machine, the images
can be built with the cluster stopped, and the services that are not needed can be left out:

```bash
minikube stop
./gradlew :gateway-service:dockerBuild :user-service:dockerBuild :restaurant-service:dockerBuild \
  :frontend-service:dockerBuild
minikube start
./gradlew deploy -PskipBuild=true \
  -Pexclude=order-service,payment-service,notification-service,table-reservation-service
```

### Expected outcome

All pods reach the `Running` state, and each service is `READY`. This can take a few minutes after the command ends:

```bash
./gradlew k8sInfo
```

The output lists one namespace per service, plus `kafka` and `observability`.

The autoscalers of `gateway-service`, `user-service`, `restaurant-service` and `order-service` report their CPU usage,
with a target of 60% and from 1 to 4 replicas:

```bash
minikube kubectl -- get hpa -A
```

If `TARGETS` shows `<unknown>`, `metrics-server` is not ready yet, and it should recover in about a minute.

### Open the application

Forward the port of the frontend, then open `http://localhost:5173`:

```bash
minikube kubectl -- -n frontend-service port-forward svc/frontend-service 5173:8080
```

Dashboards are available in the same way at `http://localhost:3000`, logging in as `admin`:

```bash
minikube kubectl -- -n observability port-forward svc/grafana 3000:80
minikube kubectl -- -n observability get secret grafana -o jsonpath='{.data.admin-password}' | base64 -d
```

The **Munchies** folder holds the dashboards for services, scaling and logs.

### Stop

```bash
./gradlew undeploy                  # Keeps the data
./gradlew undeploy -PwipeData=true  # Also wipes the data
minikube stop
```

## First use

In both modes the databases start empty, so there are no users, restaurants or menus. The
[user guide](../08-user-guide/user-guide.md) explains how to create them.

## Differences between the two modes

- **Table reservations:** `table-reservation-service` is disabled in Compose, and only runs on Kubernetes.
- **Databases:** Compose exposes each MongoDB on the host, while on Kubernetes they stay inside the cluster.
- **Images:** both modes build images from the local sources, so that a change can be tried right away. The images
  published on Docker Hub are not used, as explained in [release](../06-release/release.md).
