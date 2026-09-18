---
name: event-sourcing
description: Practical guide and checklist for implementing Event Sourcing on an aggregate in a Munchies backend service (Kotlin + Micronaut or Express.js) — persisting state as an append-only event stream instead of CRUD, with command/apply separation, replay, snapshots, and optional CQRS projections, mapped onto the project's domain/application/infrastructure hexagonal layout. Use this whenever the user asks to add or refactor an aggregate to use event sourcing, mentions an event store, wants to replay or rebuild aggregate state from its history, asks about snapshots or CQRS read models in a microservice, wants a reliable audit trail of every state change, or wants to publish fine-grained domain events to another service — even if they never say "event sourcing" explicitly.
---

# Event Sourcing implementation guide

Based on this project's course material (SAP: microservices architecture). Assumes the base hexagonal layering
from [[ddd-structure]] — read that first if you haven't; this skill only covers what changes when an aggregate is
event-sourced.

## When this applies

Most Munchies aggregates should stay plain CRUD-on-Mongo (load current state, mutate, save current state) — that's
simpler and is what `order-service` and `payment-service` do today. Reach for event sourcing only when the domain
actually needs one of:

- A full audit trail / history of every state change (not just the current snapshot).
- The ability to reconstruct past state or replay "what happened" for an aggregate.
- A reliable, ordered stream of fine-grained domain events that another microservice genuinely needs to consume
  (beyond the coarse "thing happened" events already published today, e.g. `payment-success`).

Don't event-source an aggregate just because it's an interesting pattern — it's a real mindset shift (business logic
gets rewritten around commands/events/apply instead of direct field mutation) and adds a query-side problem you now
have to solve (see CQRS below).

## Core split: `process()` vs `apply()`

An event-sourced aggregate splits command handling into two method kinds instead of one mutating method:

- **`process(command)`** — validates business rules against current in-memory state, **does not mutate anything**,
  and returns the list of new domain events that should result (or throws if the command is invalid). This is where
  business rule validation lives.
- **`apply(event)`** — takes one event and mutates internal state to reflect it. Because an event is a fact that
  already happened, `apply()` **must never fail** — no validation, no exceptions, just state transition.

Every event is named in the past tense (`OrderPlaced`, `TicketAccepted`) and must be self-contained: it carries
every field needed to reconstruct the transition, since it will outlive the code that created it.

## Layer mapping (Munchies-specific)

| Concern | Location | Notes |
|---|---|---|
| Aggregate root | `domain/model/` | Has a no-arg/default constructor for replay, a `process()` per command, an `apply()` per event type. |
| Event types | `domain/model/` | Kotlin: a `sealed class`/`sealed interface` hierarchy under a base `DomainEvent`. Express.js: a small class hierarchy or discriminated union under a `DomainEvent` base — see `references/`. |
| Rebuilding from history | `domain/factory/` | Only if non-trivial (e.g. combines a snapshot + tail events). Trivial replay can live directly in the use case. |
| Event store port | `domain/port/` | An `EventStore` (or `<Aggregate>Repository`) interface: `append(aggregateId, expectedVersion, events)` and `load(aggregateId): List<DomainEvent>`. Keep it separate from any cross-service publisher port. |
| Cross-service publisher port | `domain/port/` | Only add this if another microservice needs to consume the stream — same shape as `payment-service`'s `payment-notification-publisher` port. Don't conflate it with the event store port: the store is the source of truth, publication is a side effect of appending. |
| Orchestration | `application/usecase/` | Loads events → replays into aggregate → calls `process()` → applies the returned events in memory → appends them to the store → (optionally) publishes. One class per use case, same as any other Munchies use case. |
| Event store adapter | `infrastructure/adapter/outbound/mongo/` | Implements the `EventStore` port against an append-only Mongo collection (schema below). `document/`, `repository/`, `factory/` split stays the same as any other Mongo adapter. |
| Cross-service publication adapter | `infrastructure/adapter/outbound/kafka/` | Only when the publisher port above exists. This project deliberately does **not** follow the course's fully event-driven mesh: only `notification-service` is event-driven, and only for the narrow set of events that actually need to become a user-facing notification (the `payment-service` → `notification-service` `payment-success` topic is the pattern to replicate). Don't add Kafka publishing to a service just because "the slides show it" — the Mongo event store above is your event store; Kafka is only for the specific notification-bound events. |
| CQRS read model, if needed | `infrastructure/adapter/outbound/mongo/` (or a separate service) | See CQRS section below. |

**Single writer principle**: whichever service owns an aggregate's event stream is its only producer. Never let a
second service append events for an aggregate it doesn't own — if another service needs to react, it consumes
published events and maintains its own state, it doesn't write into your stream.

## Event store schema (MongoDB)

Model the store as one append-only collection per service (or per aggregate type if volumes justify it):

```
events {
  _id:            ObjectId
  aggregateType:  string   // e.g. "Order", "Ticket"
  aggregateId:    string   // the aggregate's id
  sequence:       long     // 0, 1, 2, ... strictly increasing per aggregateId
  eventType:      string   // e.g. "OrderPlaced"
  eventVersion:   int      // schema version of the payload, see "Event schema evolution" below
  payload:        object   // the event's own fields, JSON-serialized
  occurredAt:     ISODate
}
```

- Create a **unique index on `(aggregateId, sequence)`**. Appending with the expected next `sequence` and letting
  the unique constraint reject a race is your optimistic concurrency control — no separate version field needed.
- Never update or delete a document in this collection (aside from the GDPR mitigation below). It's append-only by
  construction, not just by convention.
- Index `aggregateId` for stream loads; add `eventType` if you'll ever need to scan across aggregates by event kind.

## Checklists

**Creating a new aggregate**
1. Instantiate the aggregate root via its default constructor (empty state).
2. Call `process(command)` to validate and produce the new events.
3. `apply()` each new event to the in-memory instance.
4. Append the new events to the event store starting at `sequence = 0`.

**Updating an existing aggregate**
1. Load the aggregate's event stream from the store (or the latest snapshot + events after it — see Snapshots).
2. Instantiate the aggregate root via its default constructor (or restore the snapshot's state).
3. `apply()` each loaded event in order to reconstruct current state.
4. Call `process(command)` with the requested change to produce new events.
5. `apply()` the new events to update in-memory state.
6. Append the new events to the store, starting at the next `sequence`.

**Wiring it into the service (first time)**
1. Identify the actual domain events for the aggregate — an EventStorming-style pass over "what state transitions
   matter" works well, don't just guess from the CRUD fields.
2. Split the aggregate's existing mutating methods into `process()`/`apply()` pairs.
3. Add the `EventStore` port in `domain/port/` and, only if needed, a publisher port.
4. Implement the Mongo adapter (and Kafka adapter, only if the publisher port exists) in `infrastructure/`.
5. If any query needs something other than "fetch by aggregate id", add a CQRS projection (next section) — don't
   try to answer it by folding the whole event stream on every read.

## Snapshots

Once an aggregate's stream gets long enough that replaying it on every load is a real cost (long-lived aggregates,
not the common case in this project), add snapshotting:

- Periodically (e.g. every N events) serialize the aggregate's current state as a snapshot document keyed by
  `(aggregateId, version)` in a separate `snapshots` collection.
- Loading becomes: fetch the latest snapshot ≤ requested version, restore state from it, then load and `apply()`
  only the events after that snapshot's version.
- Don't add this speculatively — it's an optimization for a real replay-latency problem, not a required part of the
  pattern.

## CQRS: only when you actually need cross-attribute queries

An event store is indexed by aggregate id — it answers "give me this aggregate's history" efficiently, but not
"find all orders with total > 50" without folding every event. If a use case needs that kind of query:

- Split reads out: the event-sourced side stays the command side (handles `process()`/`apply()`/append).
- Add a **projection**: a consumer (in-process subscriber, or a Kafka consumer if cross-service) that upserts each
  new event into a read-optimized Mongo collection/table shaped for the query you actually need.
- Keep the projection eventually consistent by design — don't have a use case read its own just-appended event back
  out of the projection synchronously.
- If only same-service queries are needed, the projection can just be another Mongo collection updated in the same
  use case, no Kafka involved. Only cross the service boundary with Kafka if another service needs the read model.

## Pitfalls to watch for

- **Event schema evolution**: events are kept forever, so a later field rename/add/remove breaks old payloads.
  Carry an `eventVersion` in the schema above and **upcast** old payloads to the current shape when loading, before
  handing them to `apply()` — never mutate the stored payload itself.
- **GDPR / right to be forgotten vs. immutability**: you can't delete personal data out of an immutable log the
  normal way. For fields that must be erasable, encrypt them per-user with a key stored outside the event store
  (crypto-shredding); "deleting" the data means deleting the key, which makes historical payloads permanently
  unreadable without touching the log itself.
- **At-least-once delivery**: anything consuming the stream (a projection, a Kafka consumer) can see the same event
  more than once. Make handlers idempotent, or track the highest applied `sequence`/offset and skip anything at or
  below it.
- **Replay cost**: if load latency creeps up as streams grow, that's the signal to add snapshots — don't add them
  before there's a real aggregate with a real long history.

## Notification-service: the planned first real use

`notification-service` is the intended first consumer of this pattern in the project: it will receive
notification-bound events over Kafka and persist them into its own append-only collection exactly as described
above (received event = the thing being event-sourced, `eventType`/`payload`/`occurredAt` etc.), before converting
them into user notifications and, eventually, pushing them to `frontend-service` over WebSocket. As of writing,
`notification-service` has only its Kafka consumers/producers and a thin controller — no `domain/` or Mongo adapter
yet — so this is where the layer mapping above gets applied for real, not just Kafka consumption with no
persistence.

## Code skeletons

Full worked examples matching this project's conventions:
- `references/kotlin-micronaut.md` — sealed-class events, aggregate, `EventStore` port, Mongo adapter, use case (Kotlin + Micronaut, `order-service`-style).
- `references/express.md` — TypeScript event hierarchy, aggregate, port, Mongo + Kafka adapters, use case (Express.js, `payment-service`-style).
