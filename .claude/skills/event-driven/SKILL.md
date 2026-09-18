---
name: event-driven
description: Guide to Munchies' event-driven architecture (EDA) — the agent-based, Kafka-consuming pattern used ONLY by notification-service to react to domain events and turn them into user notifications (and, later, WebSocket pushes to frontend-service). Distinct from the event-sourcing skill, which is about how one service persists its own aggregate. Use this whenever the user wants to add a new notification-bound event/topic, touch notification-service's Kafka consumers or its NotificationController, add a Kafka producer in another service that targets notification-service, or asks anything about "event-driven architecture", "agents", "choreography vs orchestration", or which parts of Munchies should react to Kafka — even without the words "event-driven" or "EDA".
---

# Event-Driven Architecture guide (notification-service only)

This is a different pattern from [[event-sourcing]]: event sourcing is how a single aggregate/service persists its
*own* state as an event log; event-driven architecture (EDA) is the *system-wide* style of how independent services
communicate and react to each other's events. Read the scope rule below before applying anything here to a service
other than `notification-service`.

## Scope in this project — read this first

Per the user's explicit decision, EDA in Munchies is **confined to `notification-service`**, not applied broadly
the way the course slides present it:

- `notification-service` is the only service that behaves as a full event-driven **agent**: an always-on process
  that consumes Kafka topics and reacts to them (per CLAUDE.md: "Recives messages only via Kafka. Follows the
  agent-based architecture").
- Every other service (`order-service`, `payment-service`, `user-service`, `restaurant-service`, etc.) stays a
  plain REST hexagonal service per [[ddd-structure]] and CLAUDE.md's communication rule (REST everywhere, Kafka
  only as the exception for talking to notification-service). A service may add **one narrow, single-topic Kafka
  producer** when one of its domain events is genuinely notification-bound (e.g. `payment-service`'s
  payment-success producer, `user-service`'s email-confirmation producer) — that alone does not make it
  "event-driven"; it's just an outbound side-channel.
- Don't add Kafka **consumers**, a polling/agent loop, or reactive processing to any service other than
  `notification-service`. Don't add a Kafka **producer** for an event that isn't actually going to become a user
  notification — REST stays the default for everything else.

## Payload style: always event-carried state transfer

The course categorizes event-driven messaging into four styles: event notification (minimal payload, forces a
callback query), event-carried state transfer (full payload, no callback needed), event sourcing (covered in
[[event-sourcing]]), and CQRS (a query-side consequence of the other three, not itself a wire format).

For every event crossing into `notification-service`, use **event-carried state transfer**: the payload must carry
everything `NotificationController` needs to build the notification, exactly like the existing
`PaymentSuccessNotification` (id, orderId, amount, currency) and the user email-confirmation event already do.
Never make notification-service call back into the source service to fetch more data — that reintroduces the
coupling EDA exists to remove, and defeats the point of decoupling notification-service from the rest of the
system.

Choreography vs. orchestration (the course's other big EDA topic, for coordinating multi-step sagas across
services) doesn't apply here: nothing in this project routes a multi-step workflow through notification-service, so
don't introduce a mediator/orchestrator pattern for it unless a real saga shows up.

## Every call in the agent loop must be asynchronous

This is core to the course's own model of the consumer as a non-blocking loop (the labs build it on Vert.x's
non-blocking event loop over Kafka clients, precisely so one slow call can't stall the whole agent). A Kafka
consumer's message handler shares its execution context across every message and, in this project, across every
topic `notification-service` subscribes to — a blocking call inside it (a synchronous DB write, a blocking HTTP
call out to another service, sync filesystem I/O, anything that stalls the event loop instead of yielding) stalls
processing for everything else notification-service is supposed to be handling concurrently. That defeats the
entire reason to make this one service event-driven in the first place.

Rule for this project: every step of notification-service's pipeline — the `eachMessage` handler, every
`NotificationController.handle*` method, the future event-store write ([[event-sourcing]]), and the future
WebSocket push — must be `async`/return a `Promise` and use the non-blocking API of whatever client library is
involved (Mongo driver, Kafka client, WS library), never a synchronous blocking call. The existing consumers
already follow this (`async run()`, `await consumer.subscribe(...)`) — keep new code consistent with it, and when
`NotificationController.handle*` methods stop being stub `console.log` calls and start doing real work (persisting,
dispatching), make them `async` too.

## notification-service's agent-based architecture (the reference pattern)

The existing code is already the concrete instance of the course's "agent" pattern — extend it, don't reinvent it:

- Each Kafka topic gets **its own consumer class**, one per event type, extending the generated `*Observer` base
  from that event's `-shared` module — see
  `notification-service/src/main/ts/infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer.ts`
  and `KafkaUserNotificationConsumer.ts`. Each consumer is, conceptually, one autonomous agent reacting to one
  stream.
- Every consumer is wired independently in the composition root
  (`notification-service/src/main/ts/index.ts`'s `main()`): built, `connect()`ed, `run()`. Consumers don't know
  about each other.
- Every consumer forwards what it receives into the single shared
  `infrastructure/adapter/inbound/web/controller/controller.ts`'s `NotificationController`, one `handle*` method
  per event type. That's the "process" step, currently a stub that logs — this is where persisting the event
  (event-sourcing, see [[event-sourcing]]) and turning it into an actual notification will go.

Mapped onto the course's `poll -> process -> produce -> commit` loop:

| Step | In this codebase |
|---|---|
| poll | `kafkajs`'s `consumer.run({ eachMessage })` — handled by the library, not hand-written |
| process | `NotificationController.handle*(event)` — must be `async`, no blocking calls |
| produce | not implemented yet — will become the WebSocket push to `frontend-service` |
| commit | `kafkajs`'s consumer-group offset management (`fromBeginning`, auto-commit) |

## Checklist: adding a new notification-bound event type

1. **Producing service**: define the event/notification type in its `-shared` module. Add exactly one narrow Kafka
   producer for it, called from the relevant use case after the domain change happens:
   - Kotlin + Micronaut: a declarative `@KafkaClient` interface, one `@Topic`-annotated method — see
     `user-service/src/main/kotlin/com/munchies/user/infrastructure/adapter/outbound/kafka/EmailConfirmationClient.kt`.
   - Express.js: a `KafkaXxxPublisher` class implementing a `domain/port` interface — see
     `payment-service/src/main/ts/infrastructure/adapter/outbound/kafka/KafkaPaymentNotificationPublisher.ts`.
   Don't build a generic "publish everything" producer — one topic, one narrow purpose.
2. **notification-service**: add a consumer class for the new topic (same shape as the two existing ones), wire it
   into `index.ts`'s `main()`.
3. Add an `async handle*` method to `NotificationController` for the new event type — no blocking calls inside it.
4. Once event-sourcing lands in `notification-service` (see [[event-sourcing]]), persist the received event via the
   event store as part of `handle*`, before/while converting it into a notification.
5. Stop there. Don't add an inbound Kafka side to the producing service, and don't make the producing service react
   to anything notification-service does.

## Why the rest of Munchies stays request/response (course's own criteria, applied here)

The course itself lists when a service should *not* be fully event-driven — this is why every service besides
`notification-service` stays REST:

- Most Munchies use cases are synchronous, data-driven lookups or writes that need an immediate, deterministic
  result (place an order, get a menu, reserve a table) — exactly the case the course says to keep request/response.
- These are small services; the operational cost of a broker, offset tracking, and schema evolution isn't worth
  paying where nothing needs the decoupling.
- Pure event-driven systems are harder to test and debug. This project only takes on that cost at the one boundary
  where it actually pays off: fire-and-forget notification fan-out, which is what `notification-service` is for.
