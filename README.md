# Munchies

[![CodeFactor](https://www.codefactor.io/repository/github/norby99/munchies/badge)](https://www.codefactor.io/repository/github/norby99/munchies)

Munchies is a restaurant management system focused on order delivery scheduling and restaurant logistics, built as a
set of microservices.

## Tech Stack

- **Frontend**: Vue.js
- **Backend**: Express.js, Kotlin + Micronaut
- **Database**: MongoDB
- **Messaging**: REST between services, Kafka for asynchronous communication with the Notification service
- **Orchestration**: Docker & Kubernetes (Helm charts under `helm/`)
- **API Standard**: OpenAPI

## Requirements

| Tool   | Version                                          |
|--------|---------------------------------------------------|
| Java   | 21 (see [`.java-version`](.java-version))         |
| Gradle | 8.14.5 (via the [Gradle Wrapper](gradlew), no local install needed) |
| Node.js | See individual service `package.json` files for Express.js/Vue.js services |
| Docker | Required to run MongoDB, Kafka, and the services locally |

The JVM services use Gradle's toolchain support (`javaVersion` in [`gradle.properties`](gradle.properties)), so the
correct JDK is provisioned automatically as long as Java 21 (or a JDK auto-provisioner) is available.

## Microservices

| Package                     | Stack              | Role                                                                                                                                    |
|------------------------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `user-service`               | Kotlin + Micronaut | Manages user accounts, authentication, and authorization.                                                                                  |
| `restaurant-service`         | Kotlin + Micronaut | Manages restaurant data, menus, and availability of dishes.                                                                                |
| `order-service`              | Kotlin + Micronaut | Handles order creation, processing, and status tracking.                                                                                   |
| `gateway-service`            | Express.js         | Acts as the API gateway, routing requests to appropriate services.                                                                         |
| `notification-service`       | Express.js         | Sends notifications to users and restaurant staff via email/SMS. Receives messages only via Kafka. Agent-based architecture.               |
| `payment-service`            | Express.js         | Handles payment processing and integration with payment gateways.                                                                          |
| `scheduler-service`          | Express.js         | Manages delivery scheduling and logistics; acts as a middleware between the user and the order-service.                                   |
| `table-reservation-service`  | Express.js         | Manages table reservations for restaurants.                                                                                                |
| `frontend-service`           | Vue.js             | Provides the user interface for customers and restaurant staff.                                                                            |

Each service module has a matching `-shared` module containing code shared with other services.

## Architecture

- **Domain-Driven Design (DDD)** with a Hexagonal Architecture (Ports & Adapters) layout inside each backend service
  (`domain/`, `application/`, `infrastructure/`). See `order-service` for the Kotlin reference implementation and
  `payment-service` for the Express.js one.
- **Event-Driven Architecture**: Kafka is used for asynchronous communication with the Notification service.
- **Microservices patterns**: API Gateway, SAGA, Circuit Breaker, Observability.

## Getting Started

### Prerequisites

- JDK 21
- Docker (and Docker Compose)
- Node.js (for the Express.js and Vue.js services)

### Running the system

The whole system (all services and their infrastructure dependencies: MongoDB, Kafka, etc.) is started and stopped
via Docker Compose, either directly through Gradle or via the deployment script:

```bash
./gradlew composeUp    # Build images and start all services
./gradlew composeDown  # Stop and tear down all services
```

Alternatively, [`scripts/deploy.sh`](scripts/deploy.sh) wraps the same `composeDown`/`composeUp` cycle with a git
pull step and post-deploy health checks, and is meant for deploying to a host:

```bash
./scripts/deploy.sh
```

The individual Docker Compose files for each infrastructure dependency are also available under `config/`
(`config/mongodb`, `config/kafka`, `config/service`) if you need to start a single dependency in isolation.

### Building and testing

The JVM services are built with Gradle (via the wrapper, no local Gradle install required):

```bash
./gradlew build          # Build all JVM services
./gradlew test           # Unit tests
./gradlew integrationTest # Integration tests
./gradlew componentTest  # Component tests
./gradlew e2eTest        # End-to-end tests (BDD + Cucumber)
```

```bash
./gradlew spotlessApply  # Fix formatting
./gradlew detekt         # Static analysis
```

Each Express.js/Vue.js service is built and tested independently via its own `package.json` (see the individual
service directories).

## Documentation

- Architecture and template references: [`docs/`](docs)
- Engineering reports site: [`mkdocs.yml`](mkdocs.yml)
- Helm charts: [`helm/`](helm)
- Kubernetes manifests: [`k8s/`](k8s)

## License

This project is licensed under the [Apache License 2.0](LICENSE).
