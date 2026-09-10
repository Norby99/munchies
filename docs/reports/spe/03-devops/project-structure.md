# Project Structure & Build System

This project uses Gradle as its build system.

## Project Structure

_Munchies_ is a *monorepo* with a *multi project* structure. These are the project available inside the repository:

- ```architecture-rules``` (Kotlin): contains Konsist's architectural tests for a clean DDD architecture
- ```commons``` (Multiplatform): contains common code for all other projects
- ```e2e-test``` (Kotlin): contains end-to-end tests for the services
- ```frontend-service``` (TypeScript): contains the code for the frontend
- ```gateway-service``` (TypeScript): contains the gateway's microservice code
- ```gateway-shared``` (Multiplatform): contains the gateway's API signatures
- ```notification-service``` (TypeScript): contains the notification's microservice code
- ```notification-shared``` (Multiplatform): contains the notification's API signatures
- ```order-service``` (Kotlin): contains the order's microservice code
- ```order-shared``` (Multiplatform): contains the order's API signatures
- ```payment-service``` (TypeScript): contains the payment's microservice code
- ```payment-shared``` (TypeScript): contains the payment's API signatures
- ```restaurant-service``` (Kotlin): contains the restaurant's microservice code
- ```restaurant-shared``` (Multiplatform): contains the restaurant's API signatures
- table-reservation-service (TypeScript): contains the table reservation's microservice code (INCOMPLETE)
- table-reservation-shared (Multiplatform): contains the table reservation's API signatures (INCOMPLETE)
- ```user-service``` (Kotlin): contains the user's microservice code
- ```user-shared``` (Multiplatform): contains the user's API signatures

Meanwhile, these folders:

- ```build-logic```: contains the Gradle conventions, tasks and configurations for the projects
- ```config```: contains the detekt and docker images configurations
- ```docs```: contains the internal and external documentation
- ```k8s```: contains the Kubernetes configurations
- ```loadtest```: contains a minikube load testing configuration with autoscaling
- ```scripts```: contains the script for building documentation, publishing to NPM, DockerHub, Maven and a script to
  deploy and run our project.

## Build System

Our project heavely relies on Gradle to test, compile, build artifacts and run.

We've written custom _tasks_ that allow to link and execute commands in different platform such as the JVM and Node;
this was possible by a plugin which allows Gradle to run npm commands.
We linked the ```./gradlew build``` command to also run a TypeScript project's ```npm run build``` using a
multiplatform's generated JavaScript code, which is beforehand compressed in a .tgz archive in order to have the
up-to-date code.

We've also created tasks to create the service's dockerfiles', their images and link them via a
```./gradlew composeUp``` to run the whole project with a single command, furthermore ```showDb``` tasks were created to
better analyze the mongodb containers running.
These dockerfiles and images, were also used to be able to utilize kubernetes as a deployment method instead of docker.

We've also created two tasks to help during the deploy-docs workflow that copies the generated docs to be then further
translated into a web page.


These are all the tasks we've created:
```
Munchies tasks
--------------
composeBuild - Builds images for services of docker-compose project
composeDown - Stops and removes containers of docker-compose project (only if stopContainers is set to true)
composeShowDb - Shows MongoDB data for a service. Usage: ./gradlew composeShowDb -Pservice=<name> [-Pcollection=<name>]
composeUp - Builds and starts containers of docker-compose project
deploy - Deploys all services to Minikube. Usage: ./gradlew deploy
deployServices
dockerBuild
dockerCreate
graphDump - Dumps project dependencies to a mermaid file.
graphUpdate - Updates Markdown file with the corresponding dependency graph.
k8sInfo - Prints the current pods and deployments across all namespaces in Minikube (except for kubernetes' pods).
moveJsDeps
pack_commons
pack_gateway-shared
pack_notification-shared
pack_order-shared
pack_payment-shared
pack_restaurant-shared
pack_table-reservation-shared
pack_user-shared
printJsDeps
run
showDb - Shows MongoDB data for a specific service. Usage: ./gradlew showDb -Pservice=<name> [-Pcollection=<name>]
showKf - Tails a Kafka topic from Minikube. Usage: ./gradlew showKf -Ptopic=<name>
test
typeDocs
undeploy - Undeploys all services from Minikube. Usage: ./gradlew undeploy [-PwipeData=true]
undeployServices
vitestCoverageVerify
```

Lastly, we've created a ```./gradlew graphUpdate``` task which updates a [
```README.md```](https://github.com/Norby99/munchies/blob/master/order-service/README.md) file for each Gradle
subproject that displays its dependencies to other subprojects.

An example:

```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
    :payment-shared[payment-shared]:::unknown
    :payment-service[payment-service]:::unknown
    :commons[commons]:::unknown

    :payment-service -.->|jsImplementation| :commons
    :payment-service -.->|jsImplementation| :payment-shared
    :payment-shared -.->|commonMainImplementation| :commons

    classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```


## Shared build logic

Many subproject share the same build logic configuration, that are defined in the ```build-logic``` folder, these
conventions also have a hierarchical structure.

These are the currenct build conventions available:

- ```dokka-convention```: dokka configuration for jvm projects
- ```express-server```: configuration for TypeScript projects
- ```kotlin-jvm```: base jvm configuration
- ```linter-convention```: linter for both Kotlin and TypeScript
- ```maven-publish-convention```: configuration for maven publishing
- ```micronaut-base```: micronaut base plugins
- ```micronaut-server```: Kotlin micronaut service configuration
- ```multiplatform-base```: Kotlin Multiplatform configuration
- ```munchies-subproject```: subproject with dependency README
- ```test-suites```: component and integration configuration for Micronaut services

```mermaid

flowchart TB
    ms["micronaut-server"]:::pj
    dk["dokka-convention"]
    kj["kotlin-jvm"]
    mb["micronaut-base"]
    ts["test-suites"]
    mpb["multiplatform-base"]:::pj
    lc["linter-convention"]
    mpc["maven-publish-convention"]
    msp["munchies-subproject"]
    es["express-server"]:::pj

    ms --> msp
    es --> msp
    mpb --> msp
    mpb --> mpc
    mpb --> lc
    es --> lc
    kj --> lc
    kj --> dk
    mb --> ts
    mpb --> dk
    ms --> mpc
    mb --> kj
    ms --> mb

    classDef pj fill:#9956e0,stroke:#333,stroke-width:2px,color:#fff
```

## Dependencies
