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

## Language & Framework Rules

### Kotlin / Micronaut

- Prefer idiomatic Kotlin (coroutines, extension functions, sealed classes).
- Use Micronaut's dependency injection and declarative syntax.

### Express.js

- Structure routes and controllers cleanly, adhering to DDD where applicable.
- Use asynchronous operations and robust error handling.

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
    - **Pre-commit:** Enforces spotless formatting (`spotlessCheck`) and static analysis (`detekt`).
    - **Pre-push:** Runs all tests (`test`).
    - **Conventional Commits:** Commit messages must strictly follow the Conventional Commits specification.
- **Local Testing:** Docker compose is set up in `config/docker/docker-compose.yml` for standing up local integrations
  or databases.

## Commands

- **Formatting:** Run `./gradlew spotlessApply` to fix formatting issues automatically before committing.
- **Linting:** Run `./gradlew detekt` to perform static code analysis based on rules in `config/detekt/detekt.yml`.
- **Testing:**:
    - Run `./gradlew test` to execute the full unit test suite.
    - For integration tests, use `./gradlew integrationTest`.
    - For component tests, use `./gradlew componentTest`.
    - For end-to-end tests, use `./gradlew e2eTest`.
