# Munchies

## Project Overview

"Munchies" is a restaurant management system focused on order delivery scheduling and restaurant logistics. It uses a
microservices architecture.

## Tech Stack

- **Frontend**: Vue.js
- **Backend**:
    - Express.js
    - Kotlin + Micronaut
- **Database**: MongoDB
- **Messaging/Communication intra microservices**: API Rest, for the exception to Notification service that uses Kafka
  for asynchronous communication.
- **Orchestration**: Docker & Kubernetes
- **API Standard**: OpenAPI

## Architectural Patterns

- **Domain-Driven Design (DDD)**: Always structure code according to DDD principles. Ensure clear separation between
  Domain, Application, and Infrastructure layers.
- **Event-Driven Architecture**: Use Kafka for asynchronous communication between microservices.
- **Microservices Patterns**:
    - API Gateway
    - SAGA Pattern (for distributed transactions)
    - Circuit Breaker Pattern (for resiliency)
    - Observability Pattern

## Microservices Structure

| Package                     | Stack              | Role                                                                                                                                   |
|-----------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `user-service`              | Kotlin + Micronaut | Manages user accounts, authentication, and authorization.                                                                              |
| `restaurant-service`        | Kotlin + Micronaut | Manages restaurant data, menus, and availability of dishes.                                                                            |
| `order-service`             | Kotlin + Micronaut | Handles order creation, processing, and status tracking.                                                                               |
| `gateway-service`           | Express.js         | Acts as the API gateway, routing requests to appropriate services.                                                                     |
| `notification-service`      | Express.js         | Sends notifications to users and restaurant staff via email/SMS. Recives messages only via Kafka. Follows the agent-based architecture |
| `payment-service`           | Express.js         | Handles payment processing and integration with payment gateways.                                                                      |
| `scheduler-service`         | Kotlin + Micronaut | Manages delivery scheduling and logistics. It acts as a middleware between the user and the order-service.                             |
| `table-reservation-service` | Express.js         | Manages table reservations for restaurants.                                                                                            |
| `frontend-service`          | Vue.js             | Provides the user interface for customers and restaurant staff.                                                                        |

Every service has both a `service` and a `shared` module. The shared module contains common code, that can be accessed
by other services. The service module contains the business logic.

## Service Internal Structure

Backend services follow a Hexagonal Architecture (Ports & Adapters) layout inside the DDD layering, regardless of
stack. Reference implementations for new code:

- **Kotlin + Micronaut**: `order-service`
- **Express.js**: `payment-service`

The folder names are the same across both stacks, only the language differs:

- `domain/`: the core domain, framework-agnostic.
    - `model/`: entities and value objects.
    - `factory/`: domain factories for building complex aggregates (used where construction logic is non-trivial;
      not every service needs one).
    - `port/`: outbound port interfaces the domain depends on (e.g. repositories, external service clients),
      implemented by adapters in `infrastructure`.
- `application/`: orchestrates the domain to fulfill use cases.
    - `usecase/`: one class per use case, implementing the matching interface from `port/inbound`.
    - `port/inbound/`: one inbound port interface per use case; controllers depend on this interface, never on the
      concrete use case class.
    - `port/inbound/command/`: input Command objects for use cases. Optional: use cases with a trivial input (e.g.
      one or two primitive parameters) may skip the Command and take parameters directly instead.
- `infrastructure/`: framework- and technology-specific adapters.
    - `adapter/inbound/web/controller/`: REST controllers, translating HTTP to/from Commands and delegating to
      inbound ports.
    - `adapter/inbound/web/config/`: framework wiring (DI beans/setup, OpenAPI configuration).
    - `adapter/outbound/mongo/`: MongoDB adapters implementing the domain's outbound ports (`document/` for Mongo
      documents, `repository/` for the repository implementation, `factory/` for domain <-> document mapping,
      `config/` for connection setup).
    - `adapter/outbound/kafka/`: Kafka producers/consumers implementing outbound ports, for services that publish
      or react to events (e.g. `payment-service` publishing a payment-success event).
    - `adapter/outbound/<other-service>/`: clients for other microservices' REST APIs (e.g. `payment-service`'s
      `order/` adapter calling `order-service`), implementing an outbound port from `domain/port`.
    - `adapter/dto/factory/`: mapping between DTOs and Commands.

New services, and new code in existing ones, should follow this structure.

### Known deviations

- `restaurant-service` uses a different, more fine-grained domain split (`aggregate/`, `repository/`,
  `valueobject/`) that predates this convention. Do not replicate it in new services or new code; when touching
  `restaurant-service`, prefer staying locally consistent with its existing style over silently migrating it
  mid-change, unless the task is explicitly to realign it with the reference structure.
- `gateway-service` intentionally has no `domain/` or `application/`: as an API Gateway it holds no business logic,
  only routing, auth middleware, and proxying to other services (`infrastructure/adapter/middleware/`).
- `notification-service` intentionally has no `application/` layer: it follows an agent-based, event-driven
  architecture and reacts to Kafka messages directly in `infrastructure/adapter/inbound/kafka/`, with no use cases
  to orchestrate.
- `table-reservation-service` does not yet implement this structure (only a stub controller exists); it should
  adopt it, following `payment-service`, as business logic is added.
- `frontend-service` (Vue.js) does not follow this backend layering; it is structured separately as a frontend
  application.

## Language & Framework Rules

### Kotlin / Micronaut

- Prefer idiomatic Kotlin (coroutines, extension functions, sealed classes).
- Use Micronaut's dependency injection and declarative syntax.

### Express.js

- Structure routes and controllers cleanly, adhering to DDD where applicable.
- Use asynchronous operations and robust error handling.
- When importing a `-shared` package (e.g. `munchies-order-service-shared`) as a local tarball dependency
  (`file:build/libs/*.tgz`), remove the `integrity` field from the corresponding entry in `package-lock.json`.
  The tarball is rebuilt locally on every build, so its hash changes each time; keeping `integrity` causes it to
  go stale and breaks `npm ci`/installs.

### Vue.js

- Use the Composition API.
- Keep components small and focused.

## Communication

- Use OpenAPI for defining RESTful APIs, ensuring clear documentation and consistency across services.
- Kafka is used to communicate to the Notification service. Ensure proper topic naming conventions and message schemas.

## Testing Standards

- Follow the pyramid testing strategy:
    - Unit tests at the base.
    - Integration tests for external dependencies (like mongoDB, Kafka).
    - Component tests for acceptance criteria, that verify the system behavior for an isolated microservice only. Every
      interaction with other microservices is mocked. All the rest is not mocked.
    - End-to-end tests for the entire system, verifying the behavior of all microservices together. It is implemented
      using BDD + Cucumber, and the tests are located in the `e2e-test/src/e2e/kotlin` directory.
- Unit, Integration, and Component tests are located respectively in the `service/src/test/`,
  `service/src/integrationTest/`, and `service/src/componentTest/` directories in each `service` module.

## DevOps & Infrastructure

- When dealing with Docker/Kubernetes files, keep SLO (Service Level Objectives) and SLI (Service Level Indicators) in
  mind for scaling.

## Database

- Every microservice has its own database.

## Agents & Special Tasks

- When writing agent-based architectures, refer to standard multi-agent patterns.

## Commenting & Documentation

- Always provide clear comments and documentation, especially for complex logic or architectural decisions.
- Never use emojis in code comments or documentation. Keep it professional and clear.
- Always write documentation and logging in English, regardless of the developer's native language, to maintain
  consistency across the codebase.

## Development Workflow and Building

- **Build System:** Gradle multi-project build orchestrating Kotlin across services. Configuration logic is centralized
  in the `build-logic` directory using Gradle convention plugins.
- **Git Hooks:** The project uses the `danilopianini.gradle-pre-commit-git-hooks` plugin.
    - **Pre-push:** Runs the `check` task, which includes tests, spotless formatting (`spotlessCheck`), and static
      analysis (`detekt`).
    - **Conventional Commits:** Commit messages must strictly follow the Conventional Commits specification.
- **Local Testing:** Docker compose files are set up under `config/` (`config/kafka/docker-compose.yml`,
  `config/mongodb/docker-compose.yml`, `config/service/docker-compose.yml`) for standing up local integrations or
  databases.

## Commands

- **Formatting:** Run `./gradlew spotlessApply` to fix formatting issues automatically before committing.
- **Linting:** Run `./gradlew detekt` to perform static code analysis based on rules in `config/detekt/detekt.yml`.
- **Testing:**:
    - Run `./gradlew test` to execute the full unit test suite.
    - For integration tests, use `./gradlew integrationTest`.
    - For component tests, use `./gradlew componentTest`.
    - For end-to-end tests, use `./gradlew e2eTest`.
