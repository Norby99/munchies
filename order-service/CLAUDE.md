# order-service

Follows the general Munchies conventions in the root `CLAUDE.md` (Kotlin + Micronaut, hexagonal layout per
`ddd-structure`, REST inbound API, narrow Kafka producer to `notification-service`). This file only covers what's
specific to `order-service`.

## Event Sourcing on the `Order` aggregate

`order-service` is the one microservice in Munchies that implements the **Event Sourcing** pattern. The `Order`
aggregate is persisted as an append-only event log instead of the plain CRUD-on-Mongo style used by every other
aggregate in the system:

- State-changing operations are split into `process(command) -> events` (validates business rules against current
  in-memory state, never mutates, returns the events that should result) and `apply(event)` (mutates state to
  reflect an event that already happened; must never fail). `Order` is rebuilt by replaying its events, not loaded
  as a single current-state document.
- Events are appended to their own MongoDB collection via an `EventStore`-shaped outbound port in `domain/port/`,
  implemented in `infrastructure/adapter/outbound/mongo/`. This is separate from, and replaces, the plain
  `OrderRepository` CRUD port used as the reference pattern by other services.
- Every event is named in the past tense (e.g. `OrderPlaced`, `OrderStatusAdvanced`) and is self-contained: it
  carries every field needed to reconstruct the transition, since it outlives the code that created it.
- See the `event-sourcing` skill for the full pattern: layer mapping, event store schema, snapshots, event schema
  evolution, and when (not) to add a CQRS projection.

## This does not make order-service event-driven

Event Sourcing here is only about how `order-service` persists its *own* aggregate. It does not change
`order-service`'s external shape:

- `order-service` stays a plain REST/hexagonal service for its inbound API — it does not gain Kafka consumers or
  become an agent-based service.
- Its existing outbound Kafka producer to `notification-service` (`OrderStatusChanged`, see root CLAUDE.md's
  Communication section) is unaffected by this change; whether it publishes from the persisted event or is called
  imperatively after the aggregate update is an implementation detail, not an architectural one.
- Event-Driven Architecture (EDA) remains confined to `notification-service` only — see the `event-driven` skill.
  Don't read the presence of Event Sourcing here as license to make `order-service` consume or react to Kafka
  events from other services.
