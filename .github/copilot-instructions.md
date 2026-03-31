# Copilot Instructions for Muchies Project

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

## Communicatio

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

