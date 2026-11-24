# Conclusions

_Munchies_ was designed from its domain outward: the requirements and the Event Storming produced the bounded
contexts, each context became a microservice with its own database and a hexagonal structure, and the whole system
was deployed on Kubernetes.

## What the architecture delivered

- **One structure, two languages.** The Kotlin and the Express.js services share the same layering, folder names and
  contracts. The `-shared` Kotlin Multiplatform modules turn the contract between two services into a compile-time
  check: a breaking change to an API or to a Kafka message stops the build of every consumer.
- **Complexity only where it pays off.** Event sourcing and CQRS are confined to the `Order` aggregate, the only one
  whose history matters, and Kafka to notifications, the only interaction no caller waits for. Everything else is
  plain CRUD behind synchronous REST, which keeps the user-facing flows easy to follow and to test.
- **Notifications that reach the screen.** When an order changes status, or a table reservation is booked, modified
  or canceled, the customer and the restaurant staff see it within moments, without reloading the page. The owning
  service publishes the event on Kafka, `notification-service` turns it into a live notification, and pushes it over
  a WebSocket that `gateway-service` authenticates and relays. Because every event carries its full state and the
  gateway forwards the user's identity with the connection, `notification-service` never has to call another service.
- **Scaling that was measured, not assumed.** The service level objectives were stated first and then checked under
  load. The first configuration missed two of the three; the analysis of that run led to three changes, and the same
  test on the final configuration met all of them, with a 95th percentile of 109 ms at 1,000 requests per second and
  no request lost while scaling in.
- **A system that can be observed.** Metrics, logs and traces are collected from every instrumented service and
  linked to each other, so a request can be followed across services and through Kafka starting from a single log
  line.
- **An autonomous component on top of the platform.** The capacity agent shows that the kitchen model built for
  another course can drive a decision of the system, the number of orders accepted per time slot, and adapt it as the
  conditions of the kitchen change.

## What it cost

- **Operating many small services.** Nine services, their databases, a broker and the monitoring stack do not fit
  comfortably on one laptop. Memory, rather than architecture, decided several choices: a single-node cluster,
  single-replica databases, and an autoscaling ceiling chosen from what the node could hold.
- **Two stacks to keep aligned.** The shared contracts removed the duplication between languages, at the price of a
  more complex build, and some guarantees exist on one stack only: the architecture tests enforce the dependency rule
  in the Kotlin services, not in the Express.js ones.
- **Consistency without transactions.** With a database per service, every flow that spans two services had to be
  ordered step by step. Where this was designed explicitly, as when an order is placed, a failure releases what was
  reserved. Where it was not, as after a payment, the two services can disagree.

## Limitations

Each limitation is discussed in the chapter it belongs to; these are the ones that matter most for the architecture
as a whole:

- **Payment and order can diverge.** If `order-service` cannot be reached after a charge, the payment stays completed
  and the order stays unpaid, with no retry and no compensation.
- **Notifications can be lost.** Events are published without an outbox, so an event published while the broker is
  down is lost. Live notifications are not stored either: they reach only the users connected at that moment, and a
  user who comes back later finds the current state on screen, not the notifications they missed.
- **Only one notification channel exists.** The WebSocket push covers orders and table reservations; no email or SMS
  is sent. The email-confirmation and payment-success events are received and counted, but delivered to nobody.
- **Two services keep their state in memory.** The bookings and delivery trips of `scheduler-service` do not survive a
  restart, and the open connections of `notification-service` live in its process; both therefore run as a single
  replica. The scheduler also exposes no metrics and is not traced.
- **Scaling was verified on one service.** Only `gateway-service` was benchmarked, on an endpoint it answers by
  itself. The services that wait on MongoDB use the same autoscaler, driven by a CPU signal that reflects their load
  less faithfully.
- **Availability is partial.** Only the gateway keeps more than one replica at rest and shuts down gracefully; the
  services have no liveness probe, and the databases and the broker run as single instances.
- **The capacity agent is a prototype.** It runs against a simulated kitchen; inside the system, the scheduler still
  applies a temporary random quota in its place.

## Lessons learned

- **The platform does not balance load the way one expects.** Kubernetes balances connections, not requests: in the
  first benchmark the new replicas were ready and idle while the original one was overloaded. Tuning the autoscaler
  would never have revealed this; only the load test did.
- **A health check is only as good as what it checks.** A readiness probe on an endpoint that ignores the database
  reports a service as ready when it cannot answer. What "healthy" means had to be made explicit.
- **Boundaries are cheap to draw and expensive to cross.** Splitting the domain into contexts was the easy part. The
  effort went into what happens between them: who owns a piece of data, which service calls which, and what happens
  when the second step of a flow fails.
- **Self-contained events make new consumers cheap.** Adding the WebSocket push did not touch any producer: every
  event already carried what the screen needed, so the whole change stayed inside `notification-service`, the gateway
  and the frontend.
- **Stating the objective first changes the work.** With a number to meet, scaling became a sequence of measurements
  and corrections instead of a configuration that merely looked reasonable.

## Future work

- Make the payment flow reliable, with an outbox for the events and a retried or compensated update of the order.
- Complete the notifications: store them so that users who were offline can read them later, add an email channel,
  and share the WebSocket rooms (e.g. through a Redis adapter) so that `notification-service` can run on more than one
  replica.
- Give the scheduler a database and instrument it, then connect the capacity agent to it, so that the quota it
  computes is the one the system applies.
- Balance load per request with a layer 7 proxy, and scale on request rate instead of CPU.
- Extend the benchmark to the services that depend on MongoDB, and the architecture tests to the Express.js services.
