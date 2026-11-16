# Conclusions

_Munchies_ was designed starting from its domain: the requirements and the Event Storming led to the bounded contexts,
and each context became a microservice with its own database, structured as a hexagonal architecture and deployed on
Kubernetes. This chapter looks back at what that architecture delivered, what it cost, and what remains open.

## What the architecture delivered

- **The same structure in two languages.** The Kotlin and the Express.js services share the layering, the folder
  names and the contracts. The `-shared` Kotlin Multiplatform modules made the contract between services a
  compile-time matter: a breaking change in an API or in a Kafka message stops the build of its consumers.
- **Complexity only where it pays.** Event sourcing and CQRS are confined to the `Order` aggregate, the one whose
  history matters, and Kafka to notifications, the one interaction nobody waits for. Everything else is plain CRUD
  behind synchronous REST, which kept the user-facing flows easy to follow and to test.
- **Scaling that was measured, not assumed.** The service level objectives were stated first and then checked under
  load. The first configuration missed two of the three; the analysis of that run led to three changes, and the same
  test on the final configuration met all of them, with a 95th percentile of 109 ms at 1,000 requests per second and
  no request lost while scaling in.
- **A system that can be observed.** Metrics, logs and traces are collected from every instrumented service and
  linked to each other, so a request can be followed across services and through Kafka from a single log line.
- **An autonomous component on top of the platform.** The capacity agent shows that the kitchen model built for
  another course can drive a decision of the system, the number of orders accepted per time slot, and adapt it as the
  conditions of the kitchen change.

## What it cost

- **Operating many small services.** Nine services, their databases, a broker and the monitoring stack do not fit
  comfortably on one laptop. Memory decided several choices that are not architectural: a single-node cluster,
  single-replica databases, an autoscaling ceiling chosen from what the node could hold.
- **Two stacks to keep aligned.** The shared contracts removed duplication between languages, but at the price of a
  more complex build, and some guarantees exist on one stack only: the architecture tests check the dependency rule in
  the Kotlin services, not in the Express.js ones.
- **Consistency without transactions.** With a database per service, every flow that spans two services had to be
  ordered step by step. Where this was done deliberately, as when placing an order, a failure releases what was
  reserved. Where it was not, as after a payment, the two services can disagree.

## Limitations

The limitations are discussed where they belong; the ones that matter most for the architecture as a whole are:

- **Payment and order can diverge.** If `order-service` cannot be reached after a charge, the payment stays completed
  and the order stays unpaid, with no retry or compensation.
- **Notifications can be lost and are not delivered yet.** Events are published without an outbox, so one published
  while the broker is down is lost; and `notification-service` receives and counts the events, but the channels that
  would deliver them (email, push to the frontend) are not implemented.
- **The scheduler keeps its state in memory.** Bookings and delivery trips do not survive a restart, which also
  prevents the service from running with more than one replica; it is not deployed on Kubernetes and not traced.
- **Scaling was verified on one service.** Only `gateway-service` was benchmarked, on an endpoint it answers by
  itself. The services that wait on MongoDB use the same autoscaler, on a CPU signal that is weaker for them.
- **Availability is partial.** Only the gateway has more than one replica at rest and a graceful shutdown; the
  services have no liveness probe, and the databases and the broker run as single instances.
- **The capacity agent is a prototype.** It runs against a simulated kitchen and does not yet write its quota back to
  the system.

## Lessons learned

- **The platform does not balance load the way one expects.** Kubernetes balances connections, not requests: in the
  first benchmark the new replicas were ready and idle while the original one was overloaded. No amount of
  configuration of the autoscaler would have shown this; only the load test did.
- **A health check is only as good as what it checks.** A readiness probe on an endpoint that ignores the database
  reports a service as ready when it cannot answer. The meaning of "healthy" had to be made explicit.
- **Boundaries are cheap to draw and expensive to cross.** Splitting the domain into contexts was the easy part.
  The effort went into what happens between them: who owns a piece of data, which service calls which, and what
  happens when the second step of a flow fails.
- **Stating the objective first changes the work.** With a number to meet, the scaling work became a sequence of
  measurements and corrections instead of a configuration that merely looked reasonable.

## Future work

- Make the payment flow reliable, with an outbox for the events and a retried or compensated update of the order.
- Deliver notifications, starting from a WebSocket push to the frontend through the gateway.
- Give the scheduler a database, deploy and instrument it, and connect the capacity agent to it so that the quota it
  computes is the one the system applies.
- Balance load per request with a layer 7 proxy, and scale on request rate instead of CPU.
- Extend the benchmark to the services that depend on MongoDB, and the architecture tests to the Express.js services.
