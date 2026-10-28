---
name: cqrs
description: Practical guide and checklist for implementing CQRS (Command Query Responsibility Segregation) in a Munchies backend service (Kotlin + Micronaut or Express.js) — splitting the write side (commands, validated against the aggregate) from the query side (one or more read-optimized projections), mapped onto the project's domain/application/infrastructure hexagonal layout. Composes naturally with the event-sourcing skill (the event store as command side, a projection as query side) but does not require it — a plain CRUD aggregate can grow a CQRS read side too. Use this whenever the user wants to add a read model, projection, or materialized view; asks about separating reads from writes; mentions "query side", "read side", "denormalized view", or a query that's awkward to answer from the current write model; or wants to add CQRS explicitly — even if they never use that exact term.
---

# CQRS implementation guide

Based on this project's course material (SAP: microservices architecture). Assumes the base hexagonal layering
from [[ddd-structure]] — read that first if you haven't; this skill only covers what changes when a service adds a
CQRS read side. If the aggregate in question is also event-sourced, read [[event-sourcing]] first: that skill owns
the command-side details (event store, `process()`/`apply()`), and this skill picks up from "how do I answer a
query I can't answer from that write model."

## When this applies

CQRS is an opt-in pattern for a specific pain point: the write model (however it's persisted) is shaped for
validating and applying one aggregate's commands, and some query genuinely doesn't fit that shape — cross-aggregate
lookups, filtering/sorting on fields that aren't the aggregate id, or a view that joins/denormalizes data a client
needs in one round trip. Don't reach for CQRS just because it's an interesting pattern:

- Most Munchies services should keep a single Mongo collection serving both reads and writes (`findById`/`save`
  style) — that's simpler and is what `payment-service` and the rest of the system do today. Splitting command and
  query models is a real complexity cost: a second schema to maintain, a projection mechanism to build, and an
  eventual-consistency window to reason about.
- The concrete application in this project is `order-service`'s `Order` aggregate (see `order-service/CLAUDE.md`):
  layered on its event-sourced write side ([[event-sourcing]]), whose event store can't efficiently answer "find
  orders by status" or "find orders for restaurant X" without folding the whole stream, which is exactly the kind
  of query a CQRS read side exists for.
- Don't add a CQRS read side to any other service's aggregate without the same justification: a real query that the
  current write-side collection genuinely can't serve well (not "it might be slow one day").

## Core split: command side vs query side

- **Command side** — owns validation and state transitions for the aggregate. In this project that's either a
  plain CRUD-on-Mongo repository (the default, see [[ddd-structure]]) or an event-sourced aggregate
  ([[event-sourcing]]). It is the single source of truth. Nothing about CQRS changes how it works — you are adding
  a second thing that reads from it, not modifying it to serve queries better.
- **Query side** — one or more **projections**: denormalized, read-optimized representations built for a specific
  query shape, kept up to date as the command side changes. A projection is disposable and rebuildable from the
  command side at any time; never treat it as a second source of truth, and never write application logic that
  mutates a projection directly outside the sync mechanism below.

You can have more than one projection off the same command side if different queries need different shapes (e.g.
one collection indexed for "orders by restaurant", another for "orders by customer") — that's normal CQRS, not
over-engineering, as long as each one is justified by an actual query.

## How the query side stays in sync

Pick the mechanism based on what the command side already is:

| Command side | Sync mechanism |
|---|---|
| Event-sourced aggregate ([[event-sourcing]]) | An in-process subscriber (or Kafka consumer, only if the projection lives in another service) upserts the projection from each newly appended event. This is the natural fit — the event stream is already the change feed. |
| Plain CRUD-on-Mongo aggregate | No event stream to subscribe to by default. Either (a) have the use case that mutates the aggregate also upsert the projection in the same request, after the write succeeds, or (b) introduce a narrow domain event for this purpose alone (published in-process, not necessarily on Kafka) if more than one use case needs to trigger the same projection update. Don't retrofit full Event Sourcing onto an aggregate just to get a change feed for one projection — that's a much bigger decision, see [[event-sourcing]]'s "When this applies". |

Either way, the projection update happens **after** the command side's write is durable, never before, and never
as part of the same transaction/write as the command side (Mongo has no cross-collection transaction to lean on
here, and coupling them defeats the point of separating them).

## Layer mapping (Munchies-specific)

| Concern | Location | Notes |
|---|---|---|
| Read model / projection shape | `domain/model/` | A plain data shape (not the aggregate) representing what a query needs — e.g. `OrderSummaryView`. Framework-agnostic, same as any other domain model. |
| Query port | `domain/port/` | An outbound port for reading the projection, e.g. `OrderSummaryQuery` with methods matching the actual queries needed (`findByRestaurant(id)`, `findByStatus(status)`) — not a generic `findAll`/`findOne`. Keep it separate from the command side's port (`EventStore`, or the plain `OrderRepository`); a controller or use case that only queries should depend on this port, not the write-side one. |
| Query use case | `application/usecase/` | One class per query, implementing an inbound port from `application/port/inbound/`, same convention as command use cases. Trivial single-parameter queries may skip a Command object, per the project's usual rule. |
| Projection sync (event-sourced case) | `application/` or `infrastructure/adapter/inbound/` | An in-process subscriber to the event store's append step, or — only if cross-service — a Kafka consumer in `infrastructure/adapter/inbound/kafka/`. Its job is exactly one thing: turn a new event into an upsert against the projection. |
| Projection sync (plain-CRUD case) | `application/usecase/` | The existing mutating use case calls the query port's upsert after its own write succeeds, or a narrow in-process domain event triggers it — see table above. |
| Projection storage adapter | `infrastructure/adapter/outbound/mongo/` | Implements the query port against its own Mongo collection (`document/`, `repository/`, `factory/` split, same as any other Mongo adapter). A separate collection from the command side's, even though both live in the same service's database. |
| Query controller | `infrastructure/adapter/inbound/web/controller/` | A read-only REST endpoint depending on the query inbound port, same as any other controller — never depends on the command-side port. |

## Checklist: adding a CQRS read side

1. Confirm there's a real query the command side can't serve well — name it explicitly (e.g. "list orders by
   restaurant and status, paginated"). If you can't name the query, you don't need this yet.
2. Design the projection's shape around that query, not around the aggregate's internal structure — denormalize
   freely (embed restaurant name, not just id, if the query needs it displayed).
3. Add the query port in `domain/port/` and its Mongo adapter in `infrastructure/adapter/outbound/mongo/`, as a
   separate collection from the command side.
4. Wire the sync mechanism from the table above (event subscriber for an event-sourced command side; in-use-case
   upsert or narrow domain event for a plain-CRUD one).
5. Add the query use case and controller, depending only on the query port.
6. Backfill: if the command side already has existing data, run a one-off rebuild of the projection from current
   state (or by replaying the event store, if event-sourced) before relying on it in production.

## Pitfalls to watch for

- **Treating the projection as authoritative.** It must always be rebuildable from the command side from scratch.
  If you can't regenerate it (bug, schema change, corruption) by replaying/re-deriving from the command side, it
  has quietly become a second source of truth, which defeats the pattern.
- **Reading your own write.** A use case that just mutated the command side must not turn around and read the
  projection synchronously expecting to see its own change — the projection is eventually consistent by design,
  even in the same-service, same-request sync case, if that sync is fire-and-forget. If a flow genuinely needs
  read-your-writes, return the result from the command-side write itself, don't round-trip through the projection.
- **Over-splitting.** Adding a projection for a query that a well-chosen Mongo index on the existing collection
  would already serve is pure overhead. CQRS pays off when the read shape is genuinely different from the write
  shape (denormalized, cross-aggregate, or differently indexed), not for every query that's merely "not by id".
- **Cross-service projections and this project's Kafka rule.** Per root `CLAUDE.md`, Kafka in this system is
  reserved for talking to `notification-service` — don't repurpose it as a general projection-sync bus between
  arbitrary services. A same-service projection should sync in-process; if a genuinely different service needs its
  own read model derived from another service's data, that's a bigger integration decision than this skill covers,
  not a default choice.

## Relation to the other pattern skills

- [[event-sourcing]] — the natural command-side pairing: the event store is already a change feed, so the
  projection sync mechanism is a straightforward subscriber. Read that skill first when the aggregate in question
  is (or will be) event-sourced.
- [[event-driven]] — unrelated in scope: EDA in this project is confined to `notification-service` reacting to
  Kafka for user notifications. Don't use `notification-service`'s Kafka topics as a projection-sync mechanism, and
  don't read "CQRS uses events to sync" as license to add Kafka consumers to a service outside
  `notification-service` — see the pitfall above.

## order-service: the concrete application

`order-service`'s `Order` aggregate is where this pattern applies (see `order-service/CLAUDE.md`), layered on top
of its Event Sourcing write side: the event store is the command side, and a Mongo projection (or several,
depending on which queries the API needs) is the query side, synced by subscribing to newly appended `Order`
events. Apply the checklist above once a real query need is identified for it.
