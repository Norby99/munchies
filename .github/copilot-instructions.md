# Copilot Instructions for Munchies Project

## Project Overview

"Munchies" is a restaurant management system focused on order delivery scheduling and restaurant logistics. It uses a
microservices architecture.

## Tech Stack

- **Frontend**: Vue.js
- **Backend**:
    - Express.js
    - Kotlin + Micronaut
- **Database**: MongoDB
- **Messaging/Communication intra microservices**: Kafka
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

- Kafka is used for inter-service communication. Ensure proper topic naming conventions and message schemas.
- Use OpenAPI for defining RESTful APIs, ensuring clear documentation and consistency across services.

## Testing Standards

- **Behavior-Driven Development (BDD)**: Write feature files using Gherkin format and implement steps using Cucumber (
  for automated acceptance tests).
- Implement thorough Unit and Integration tests for all microservices.

## DevOps & Infrastructure

- When dealing with Docker/Kubernetes files, keep SLO (Service Level Objectives) and SLI (Service Level Indicators) in
  mind for scaling.

## Agents & Special Tasks

- When writing agent-based architectures, refer to standard multi-agent patterns.

## Commenting & Documentation
- Always provide clear comments and documentation, especially for complex logic or architectural decisions.
- Never use emojis in code comments or documentation. Keep it professional and clear.
- Always write comments and logging in English, regardless of the developer's native language, to maintain consistency across the codebase.

## Development Workflow and Building
- **Build System:** Gradle multi-project build orchestrating Kotlin across services. Configuration logic is centralized in the `build-logic` directory using Gradle convention plugins.
- **Git Hooks:** The project uses the `danilopianini.gradle-pre-commit-git-hooks` plugin.
    - **Pre-commit:** Enforces spotless formatting (`spotlessCheck`) and static analysis (`detekt`).
    - **Pre-push:** Runs all tests (`test`).
    - **Conventional Commits:** Commit messages must strictly follow the Conventional Commits specification.
- **Local Testing:** Docker compose is set up in `config/docker/docker-compose.yml` for standing up local integrations or databases.

## Commands
- **Formatting:** Run `./gradlew spotlessApply` to fix formatting issues automatically before committing.
- **Linting:** Run `./gradlew detekt` to perform static code analysis based on rules in `config/detekt/detekt.yml`.
- **Testing:** Run `./gradlew test` to execute the full unit test suite.
- **Deployment:** Deployment utilities are available in `scripts/deploy.sh` and `scripts/undeploy.sh`.
