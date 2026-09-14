---
name: ddd-structure
description: Checklist for placing and reviewing code in the domain/application/infrastructure layers of Munchies' backend services (Kotlin + Micronaut and Express.js), with order-service and payment-service as the reference layouts. Use when adding or moving a use case, controller, repository, port, or domain model in a backend service, or when reviewing such a change for layering violations.
---

# DDD / Hexagonal layering checklist

Reference implementations: `order-service` (Kotlin + Micronaut) and `payment-service` (Express.js) — see CLAUDE.md,
"Service Internal Structure". Consult this whenever you add, move, or review code inside `domain/`, `application/`,
or `infrastructure/` in a backend service.

## Placement rules

1. **Domain purity**: nothing under `domain/` may import the web framework (Micronaut/Express), the MongoDB driver,
   DTOs, or anything from `infrastructure/`. If a domain class needs external behavior, define an interface in
   `domain/port/` and let infrastructure implement it.
2. **Entities & value objects** go in `domain/model/`. Non-trivial construction logic for aggregates goes in
   `domain/factory/`, not in constructors or in the use case.
3. **Outbound dependencies** (repository, external service used by the domain, message publisher) are interfaces in
   `domain/port/`, never referenced directly from infrastructure call sites without going through the interface.
4. **One use case, one class**: `application/usecase/` has one class per use case, implementing a matching interface
   in `application/port/inbound/`. Controllers and other inbound adapters depend on the inbound port interface,
   never on the concrete use case class.
5. **Commands are optional**: add an `application/port/inbound/command/` Command object only when the use case takes
   more than a couple of parameters. For trivial use cases (e.g. get-by-id), passing parameters directly to the port
   method is fine — do not force a Command object for its own sake.
6. **Controllers stay thin**: `infrastructure/adapter/inbound/web/controller/` classes translate HTTP <-> Command
   (or plain parameters) and call the inbound port; no business logic there.
7. **Persistence adapters** under `infrastructure/adapter/outbound/mongo/` implement a `domain/port/` interface.
   Keep `document/` (Mongo document classes), `repository/` (the implementation), and `factory/` (domain <->
   document mapping) separate — don't build documents inline inside the repository.
8. **Other outbound adapters** (Kafka producers in `adapter/outbound/kafka/`, clients for other microservices in
   `adapter/outbound/<other-service>/`) implement a `domain/port/` interface the same way persistence adapters do.
9. **DTO/domain mapping** lives in `infrastructure/adapter/dto/factory/`, not inline in controllers or use cases.

## Known deviations (do not flag these as violations)

- `restaurant-service` uses a different, more fine-grained domain split (`aggregate/`, `repository/`,
  `valueobject/`) that predates this convention. Don't replicate it elsewhere; when touching it, stay locally
  consistent rather than silently migrating it, unless the task is explicitly to realign it.
- `gateway-service` has no `domain/`/`application/` by design: it is an API Gateway with no business logic, only
  routing/auth middleware and proxying (`infrastructure/adapter/middleware/`).
- `notification-service` has no `application/` by design: it is an agent-based, event-driven consumer that reacts
  to Kafka messages directly in `infrastructure/adapter/inbound/kafka/`.
- `table-reservation-service` has not implemented this structure yet (stub controller only); it should adopt it,
  following `payment-service`, as business logic is added — flag missing layers there as "not yet built", not as a
  violation.
- `frontend-service` (Vue.js) is out of scope for this checklist entirely.

## When reviewing a diff

Flag it if:
- A `domain/` file imports anything from `infrastructure/` or a framework package.
- A controller (or other inbound adapter) calls a `usecase` class directly instead of its inbound port interface.
- A new outbound integration (persistence, Kafka, another service's API) is called directly from `application/` or
  `domain/` without a port interface in `domain/port/`.
- A Command object is added for a use case with a single simple parameter (unnecessary indirection), or,
  conversely, a use case with several parameters skips the Command and takes them positionally.
- New business logic is added to `gateway-service` or `notification-service` outside their expected shape (e.g. a
  use case sneaking into `gateway-service`, which should stay logic-free) — worth a comment, not necessarily a
  blocker, since these two are intentional exceptions to the layered pattern itself.
