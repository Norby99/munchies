# Deployment

For the deployment stage, we structured it by two complementary phases:
- Containerization: achieved using Docker and Gradle plugins
- Orchestration: achieved using Kubernetes and Helm for templating configurations

## Containerization

### Docker Build
In the containerization phase, every service builds a Docker image thanks to a process
defined in the Gradle task `dockerBuild`, regardless of the stack of it.
Two ways possible:
- For **Micronaut** services: the `micronaut.application` plugin generates it layered,
by the `BuildLayersTask`, that splits an image into separate layers for dependency jars,
application classes and resources. This allows to reduce build time by not downloading
dependencies every time, exploiting Docker's caching.
- For **Express** services: the `express-server.gradle.ktw` configures manually all the steps
to build the image, copying libraries and setting up the Dockerfile.

### Docker Compose
We then configured tasks from `com.avast.gradle.docker-compose` for handling 
the automatic launch of the project containers in `com.munchies.tasks.compose`
and named respectively `composeBuild`, `composeUp` and `composeDown`.
The docker compose configuration files are used by these tasks, are located in 
`config/` folder and define properties and costraints like on what container
the compose depends on.
`composeUp` and `composeDown` respectively start or shut the containers, while `composeBuild` 
build them while ensuring the different management for image composition depending
on the platform.
We finally defined a task `composeShowDb` to show the current state of the containerized
databases.

## Orchestration

### Kubernetes
As a strong standard for deployment, we decided to use Kubernetes for handling the container
orchestration.
Configuration files are defined in `k8s/` folder for `kafka`, `notification-service` and 
`payment-service`. In every subfolder we have definitions for *Namespace*, ...