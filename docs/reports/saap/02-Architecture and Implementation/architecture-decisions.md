# Architecture Decision Records

This page records the decisions that shaped the architecture of _Munchies_: the context in which each one was taken,
what was decided, which alternatives were set aside and what the decision costs. The other pages of the report
describe the result; this one explains why it looks that way.

| ADR | Decision | Status |
|---|---|---|
| [ADR-1](#adr-1-one-microservice-and-one-database-per-bounded-context) | One microservice and one database per bounded context | Accepted |
| [ADR-2](#adr-2-hexagonal-architecture-inside-every-service) | Hexagonal architecture inside every service | Accepted |
| [ADR-3](#adr-3-two-technology-stacks-with-contracts-shared-as-code) | Two technology stacks, with contracts shared as code | Accepted |
| [ADR-4](#adr-4-a-single-api-gateway-that-owns-authentication) | A single API Gateway that owns authentication | Accepted |
| [ADR-5](#adr-5-synchronous-rest-between-services-kafka-only-for-notifications) | Synchronous REST between services, Kafka only for notifications | Accepted |
| [ADR-6](#adr-6-event-sourcing-and-cqrs-only-for-the-order-aggregate) | Event sourcing and CQRS only for the `Order` aggregate | Accepted |
| [ADR-7](#adr-7-orders-are-placed-through-the-scheduler) | Orders are placed through the scheduler | Accepted |
| [ADR-8](#adr-8-service-discovery-delegated-to-the-platform) | Service discovery delegated to the platform | Accepted |
| [ADR-9](#adr-9-autoscaling-on-cpu) | Autoscaling on CPU | Accepted |

## ADR-1: One microservice and one database per bounded context

**Context.** The domain covers areas that change for different reasons and carry different loads: accounts,
restaurant configuration, orders, payments, scheduling, table reservations and notifications. Browsing menus and
placing orders peak at lunch and dinner, while restaurant configuration is rare.

**Decision.** Each bounded context is a separate microservice with its own MongoDB database. No service reads or
writes the database of another: data owned by another context is obtained through that context's REST API.

**Alternatives.** A modular monolith with one database would have been simpler to deploy and would have allowed
transactions across contexts. A shared database between microservices would have removed the remote calls but coupled
every service to the others' schema.

**Consequences.**

- Each service is built, deployed and scaled on its own, and its boundary in the code is also a boundary in the
  infrastructure (one namespace and one database per service).
- There are no transactions across services: a flow that spans two of them must order its steps and compensate on
  failure (see [Interactions](interactions.md)).
- A service that needs another context's data pays a remote call for it, and depends on that service being available.
- More components to operate: on a single-node cluster, memory limits how many replicas and databases can run.

## ADR-2: Hexagonal architecture inside every service

**Context.** Every service has domain rules that should not depend on the web framework, on MongoDB or on Kafka, and
the team works on two stacks. Without a common structure, each service would be organised differently and the domain
would end up mixed with infrastructure code.

**Decision.** Every backend service is organised in `domain`, `application` and `infrastructure` layers, with inbound
ports for the use cases and outbound ports for everything external. The folder names are the same in Kotlin and in
TypeScript. In the Kotlin services the dependency rule is enforced by architecture tests.

**Alternatives.** A classic layered architecture (controller, service, repository) is quicker to write, but lets the
domain depend on persistence types.

**Consequences.**

- Use cases are tested with mocked ports, and the same use cases run on in-memory adapters in the BDD tests and on
  MongoDB in production.
- A developer moving between services, or between stacks, finds the same layout.
- More files and indirection for simple operations: an interface and a class for each use case.
- The rule is checked automatically only in the Kotlin services; in the Express.js ones it relies on review.
- `gateway-service` and `notification-service` deliberately do not follow it: the first has no business logic, the
  second has no use cases to orchestrate.

See [Hexagonal Architecture](hexagonal-architecture.md).

## ADR-3: Two technology stacks, with contracts shared as code

**Context.** The services are written in Kotlin with Micronaut and in TypeScript with Express.js, and the frontend in
TypeScript with Vue. Services in different languages must agree on paths, payloads and Kafka messages, and a contract
written twice by hand drifts.

**Decision.** Every service publishes its API (paths, methods, required role, request and response classes) and its
Kafka messages in a `-shared` Kotlin Multiplatform module, compiled for the JVM and to JavaScript. Consumers build on
these definitions: the gateway routes extend the API classes, and the consumers of a topic use the producer's
deserializers.

**Alternatives.** A single stack would have removed the problem, at the cost of not using each technology where it
fits. OpenAPI-generated clients, or consumer-driven contract tests, would have kept the stacks independent, but with a
generation step or a separate test suite to maintain.

**Consequences.**

- A breaking change in a contract fails the build of its consumers, before any test runs.
- No contract is duplicated by hand across the two languages.
- The build is more complex: the JavaScript services depend on tarballs produced by the Kotlin build.
- A provider whose implementation diverges from its own published contract is not detected by the compiler, and is
  left to component and end-to-end tests.
- `scheduler-service` does not publish a shared contract, so its gateway routes are plain pass-throughs.

## ADR-4: A single API Gateway that owns authentication

**Context.** The frontend needs one address to talk to, and every service needs to know who is calling and with which
role. Checking tokens in every service would repeat the same logic in two languages.

**Decision.** `gateway-service` is the only entry point. It issues a JWT at login, stored in an `HttpOnly` cookie,
validates it on every request, enforces the role each route requires, and writes the caller's id into the request it
forwards. Services behind it never handle tokens.

**Alternatives.** Authentication in every service would keep each one secure on its own, but would duplicate the
token logic. A token kept by the frontend in local storage and sent as a header would be simpler to handle, but
readable by any script running in the page.

**Consequences.**

- Authentication and role checks are written once, and a client cannot act on behalf of another user by forging an id
  in the body.
- The services trust what the gateway forwards: they must not be reachable from outside the cluster, and a request
  that bypassed the gateway would not be authenticated.
- The gateway is on the path of every request, so it is the first service to saturate. It is the only one with a
  floor of two replicas, and the one the scaling benchmark loads.
- Ownership checks that need domain data (for example, that an order belongs to the paying customer) still belong to
  the service that owns the data.

## ADR-5: Synchronous REST between services, Kafka only for notifications

**Context.** Most interactions are requests from a user who waits for the answer: placing an order, paying, reserving
a table. Notifications are different: nobody waits for them, they may be slow, and their failure must not fail the
action that caused them.

**Decision.** Services call each other synchronously over REST. Kafka is used only to deliver events to
`notification-service`, the only event-driven service, with one topic per event. Each event carries everything the
consumer needs (event-carried state transfer), so `notification-service` never calls back into the producer.

**Alternatives.** A fully event-driven system, with services reacting to each other's events, would decouple them in
time, but every user-facing flow would become eventually consistent and harder to follow and to test. REST for
notifications too would have tied the response time of a payment to the delivery of an email.

**Consequences.**

- User-facing flows are easy to follow: a request, a chain of calls, an answer.
- A failure of the broker or of `notification-service` does not affect ordering, payment or reservation.
- Services are coupled in time: if `order-service` is down, orders cannot be placed and payments cannot be checked.
- Events are published after the local state is stored and without an outbox, so a notification can be lost if the
  broker is unavailable at that moment.

## ADR-6: Event sourcing and CQRS only for the `Order` aggregate

**Context.** An order goes through a lifecycle (placed, changed, paid, prepared, delivered or cancelled) driven by
different actors, and its history matters: when it was paid, when it entered the kitchen, what was changed. The other
aggregates (users, restaurants, menus, payments) are read and written as current state.

**Decision.** Only the `Order` aggregate is stored as an append-only stream of events, with optimistic concurrency on
the sequence number. A projector maintains a separate read model for the queries. Every other service persists its
current state with plain CRUD.

**Alternatives.** CRUD everywhere would be uniform and simpler, but would keep only the last state of an order. Event
sourcing everywhere would be uniform too, but would add replay, versioning and projections to aggregates that have no
use for their history.

**Consequences.**

- The full history of every order is available, and concurrent commands on the same order cannot overwrite each other.
- Queries read a denormalised view and never replay a stream.
- `order-service` is harder to understand than the others, and the events become a format that must stay readable
  over time.
- The read model is updated after the write, so a query issued immediately after a command may briefly see the
  previous state.

See Event Sourcing and CQRS in [Microservices Patterns](microservices-patterns.md#5-event-sourcing-order-service).

## ADR-7: Orders are placed through the scheduler

**Context.** A restaurant can prepare only so many items per time slot, and deliver only as far as its drivers can go.
Whether an order can be accepted therefore depends on the other orders of the same slot, which is not a rule of the
single order.

**Decision.** Customers place orders through `scheduler-service`, which sits between them and `order-service`. It
checks the capacity of the slot against the restaurant's capacity policy, plans the delivery trip, reserves the
capacity with a booking, and only then asks `order-service` to create the order. If that call fails, the booking is
marked as failed and the capacity is released.

**Alternatives.** Putting the capacity rules inside `order-service` would avoid one service and one remote call, but
would mix the lifecycle of an order with the planning of a kitchen and of delivery runs. Letting the frontend call
the two services in sequence would leave the consistency between booking and order to the client.

**Consequences.**

- `order-service` stays focused on the order and knows nothing about capacity or trips.
- The scheduler is the one place where the capacity policy is applied, and where an agent can adjust it (see the
  [Capacity Agent](../04-Agent-Based%20Architecture/capacity-agent.md)).
- Placing an order needs both services to be available.
- Bookings and trips are currently kept in memory: they are lost when the scheduler restarts, and the service cannot
  run with more than one replica.

## ADR-8: Service discovery delegated to the platform

**Context.** With autoscaling, the instances of a service change continuously, so a caller cannot be configured with
the address of an instance.

**Decision.** No service registry is used. Services are addressed by the DNS name of their Kubernetes `Service`,
passed in as an environment variable; Kubernetes registers the pods from their readiness probe and balances the
connections. In Docker Compose the same variables point to the Compose service names.

**Alternatives.** A dedicated registry such as Eureka or Consul, with a client library in every service, would allow
client-side load balancing and retries on another instance.

**Consequences.**

- No extra component to run, and no discovery code in any service, in either language.
- One notion of health: the readiness probe decides traffic, rollouts and new replicas.
- Load is balanced per connection and not per request, which kept new replicas idle in the first benchmark run until
  the gateway was changed to close its connections periodically.
- Discovery works only inside the cluster or the Compose network.

The full reasoning is in [Deployment on Kubernetes](../03-Deployment/kubernetes.md#service-discovery).

## ADR-9: Autoscaling on CPU

**Context.** The service level objectives are expressed on what the user sees: latency and error rate. The
autoscaler, however, can only act on metrics that Kubernetes can read.

**Decision.** The stateless services scale horizontally on average CPU utilisation, read from `metrics-server`, with
a target of 60% of the request, fast scale-up and gradual scale-down. The objectives are checked from outside, by the
load test.

**Alternatives.** Scaling on request rate or on latency, through a custom-metrics adapter (Prometheus Adapter or
KEDA), would follow the objectives more directly and react sooner, but adds a component and its configuration to a
cluster that is already short of memory.

**Consequences.**

- Nothing to install beyond `metrics-server`.
- For the gateway, whose work is CPU-bound, CPU saturates before latency degrades, and the benchmark confirms that
  the objectives are met.
- For the services that wait on MongoDB, CPU is a weaker signal: latency can grow while CPU stays low. They use the
  same autoscaler, but were not benchmarked.
- The reaction time is bounded by how often `metrics-server` reports, which is what remains of the overload at the
  start of a peak.

See [Autoscaling and service level objectives](../03-Deployment/kubernetes.md#autoscaling-and-service-level-objectives)
and the [Scaling Benchmark](../03-Deployment/scaling-benchmark.md).
