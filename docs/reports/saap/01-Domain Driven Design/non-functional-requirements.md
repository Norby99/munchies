# Non-functional Requirements

Non-functional requirements describe the qualities the system must have while delivering the
[Functional Requirements](functional-requirements.md). Most of them refine business requirement BR-11 (stay responsive
during peaks, with a cost that follows the load); the others come from the choice of a microservices architecture,
whose benefits only materialise if services stay independent, observable and easy to change.

Targets are numeric only where the project defines and measures them: the service level objectives, which the
[Scaling Benchmark](../03-Deployment/scaling-benchmark.md) checks under load. The other requirements are qualitative,
and the **Addressed by** column points to the part of the report that shows how the architecture meets them.

## Performance and scalability

| ID    | Requirement                                                                                                                          | Target                                         | Addressed by                                                                                                     |
|-------|--------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| NFR-1 | Requests are answered quickly, including during peaks.                                                                               | p95 latency below 800 ms                       | [Autoscaling and SLOs](../03-Deployment/kubernetes.md#autoscaling-and-service-level-objectives)                  |
| NFR-2 | Requests succeed, including during peaks.                                                                                            | Error rate below 1%                            | [Autoscaling and SLOs](../03-Deployment/kubernetes.md#autoscaling-and-service-level-objectives)                  |
| NFR-3 | Each service scales horizontally and independently of the others, adding capacity when load grows and releasing it when load drops. | No requests lost because of scaling            | [Scaling Benchmark](../03-Deployment/scaling-benchmark.md#against-the-service-level-objectives)                  |

## Availability and resilience

| ID    | Requirement                                                                                                                                               | Addressed by                                                                                         |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| NFR-4 | The entry point of the system is not a single point of failure.                                                                                           | [Autoscaling and SLOs](../03-Deployment/kubernetes.md#autoscaling-and-service-level-objectives) (gateway replica floor) |
| NFR-5 | A failed instance is replaced automatically, and an instance receives traffic only when it reports itself as healthy, including its database connection. | [Deployment on Kubernetes](../03-Deployment/kubernetes.md), [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (health check API) |
| NFR-6 | A failure in delivering notifications does not affect ordering or payment: notifications are decoupled from the business flow that triggers them.          | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (event-driven architecture) |

## Security

| ID    | Requirement                                                                                                                            | Addressed by                                                                                         |
|-------|----------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| NFR-7 | Every request to a protected feature is authenticated with a personal token, checked once at the single entry point of the system.      | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (API Gateway) |
| NFR-8 | Access is authorized by role: only managers can manage restaurants, and only the owner of a resource can change it.                     | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (API Gateway) |
| NFR-9 | Passwords are never stored in clear text, and the internal services are not reachable from outside the system except through the gateway. | [Deployment on Kubernetes](../03-Deployment/kubernetes.md)                                          |

## Data

| ID     | Requirement                                                                                                                       | Addressed by                                                                                         |
|--------|-----------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| NFR-10 | Each service owns its data: no service reads or writes another service's database.                                                | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (database per service) |
| NFR-11 | The full history of every order, each change of its items and status, is retained and can be reconstructed at any point in time.  | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (event sourcing, CQRS) |

## Observability

| ID     | Requirement                                                                                                                                     | Addressed by                                                                                         |
|--------|-------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| NFR-12 | The behaviour of every service can be monitored through metrics, logs and traces, and a single request can be followed across all the services it touches. | [Microservices Patterns](../03-Architecture%20and%20Implementation/microservices-patterns.md) (observability), [Deployment on Kubernetes](../03-Deployment/kubernetes.md#observability) |
| NFR-13 | The service level indicators of NFR-1 and NFR-2 are measured continuously, not only during benchmarks.                                         | [Autoscaling and SLOs](../03-Deployment/kubernetes.md#autoscaling-and-service-level-objectives)       |

## Maintainability and deployability

| ID     | Requirement                                                                                                                                          | Addressed by                                                                                         |
|--------|------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| NFR-14 | The domain logic of each service is independent of frameworks and infrastructure, and the dependency rule between layers is verified automatically. | [Hexagonal Architecture](../03-Architecture%20and%20Implementation/hexagonal-architecture.md#verification) |
| NFR-15 | Every service is tested at each level of the test pyramid, and its interactions with other services are covered by contracts.                         | [Testing](../02-Implementation/testing.md)                                                           |
| NFR-16 | Each service can be built, deployed and replaced independently, and can use the technology stack that best fits it.                                  | [Hexagonal Architecture](../03-Architecture%20and%20Implementation/hexagonal-architecture.md), [Docker Compose](../03-Deployment/docker-compose.md) |
| NFR-17 | The same artefacts run in development and in production, with all environment-specific settings outside the code.                                   | [Docker Compose](../03-Deployment/docker-compose.md), [Deployment on Kubernetes](../03-Deployment/kubernetes.md) |
| NFR-18 | The public API of every service is documented with OpenAPI.                                                                                          | [OpenAPI specs](https://norby99.github.io/munchies/openapi/index.html)                                |
