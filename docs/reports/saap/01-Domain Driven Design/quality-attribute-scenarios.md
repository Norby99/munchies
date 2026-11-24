# Quality Attribute Scenarios

The [Non-functional Requirements](non-functional-requirements.md) state which qualities the system must have. This
page makes the most important ones concrete as quality attribute scenarios: a stimulus arriving at a part of the
system under given conditions, the response the architecture must give, and how that response is measured.

Each scenario ends with its **evidence**, which says how far the response has been checked:

- **Measured**: observed in a run, with the figures reported in the [Scaling Benchmark](../03-Deployment/scaling-benchmark.md);
- **Tested**: checked by an automated test that runs in CI;
- **By design**: follows from how the system is built and configured, but was not measured.

| Scenario | Quality attribute | Requirement | Evidence |
|---|---|---|---|
| [QAS-1](#qas-1-latency-at-peak-load) | Performance | NFR-1, NFR-2 | Measured |
| [QAS-2](#qas-2-sudden-increase-of-load) | Scalability | NFR-3 | Measured |
| [QAS-3](#qas-3-load-drops-after-a-peak) | Scalability, availability | NFR-3 | Measured |
| [QAS-4](#qas-4-an-instance-stops-answering) | Availability | NFR-4, NFR-5 | By design |
| [QAS-5](#qas-5-the-message-broker-is-unavailable) | Resilience | NFR-6 | By design |
| [QAS-6](#qas-6-concurrent-changes-to-the-same-order) | Data integrity | NFR-11 | By design |
| [QAS-7](#qas-7-request-without-the-required-identity-or-role) | Security | NFR-7, NFR-8 | By design |
| [QAS-8](#qas-8-paying-another-customers-order) | Security | NFR-8 | By design |
| [QAS-9](#qas-9-a-service-changes-its-api) | Modifiability | NFR-15, NFR-16 | By design |
| [QAS-10](#qas-10-a-change-breaks-the-layering) | Maintainability | NFR-14 | Tested |
| [QAS-11](#qas-11-a-slow-request-is-investigated) | Observability | NFR-12 | Tested (trace propagation) |
| [QAS-12](#qas-12-a-single-service-is-redeployed) | Deployability | NFR-16, NFR-17 | By design |

## Performance and scalability

### QAS-1: Latency at peak load

| Part | Description |
|---|---|
| Source | Customers and managers using the system during a lunch or dinner peak |
| Stimulus | 1,000 requests per second, sustained for four minutes |
| Artifact | `gateway-service`, the entry point of every request |
| Environment | Normal operation on Kubernetes, autoscaling enabled, 2 to 8 replicas |
| Response | Every request is authenticated and answered |
| Response measure | 95th percentile of the latency below 800 ms; fewer than 1% of the requests fail |
| Evidence | **Measured**: p95 of 109 ms and no failed request in the final run of the benchmark |

### QAS-2: Sudden increase of load

| Part | Description |
|---|---|
| Source | Customers arriving together at the start of a peak |
| Stimulus | The load rises from 20 to 1,000 requests per second in one minute |
| Artifact | `gateway-service` and its `HorizontalPodAutoscaler` |
| Environment | Two replicas running, each able to serve about 700 requests per second |
| Response | The autoscaler adds replicas, and the new replicas take their share of the load as soon as they are ready |
| Response measure | The objectives of QAS-1 hold over the whole run; the time during which requests queue is bounded by the reaction time of the autoscaler |
| Evidence | **Measured**: from 2 to 6 replicas and then to 8; requests queued for about 21 seconds, and the latency objective was still met |

### QAS-3: Load drops after a peak

| Part | Description |
|---|---|
| Source | The end of a peak |
| Stimulus | The load falls from 1,000 to 20 requests per second while requests are still arriving |
| Artifact | `gateway-service`, the autoscaler and the Kubernetes `Service` in front of the replicas |
| Environment | Eight replicas running |
| Response | Replicas are removed one at a time; a replica leaves the `Service` before it stops and finishes the requests it is serving |
| Response measure | No request is lost because a replica was removed |
| Evidence | **Measured**: six scale-in steps, from 8 replicas to 2, with no failed request. Before graceful shutdown was added, one request was lost in the same test |

## Availability and resilience

### QAS-4: An instance stops answering

| Part | Description |
|---|---|
| Source | A fault inside a service instance, or the loss of its connection to MongoDB |
| Stimulus | The instance stops answering `/health` |
| Artifact | Any service deployed on Kubernetes |
| Environment | Normal operation, at least one other replica of the service ready |
| Response | The instance is taken out of the `Service` and receives no more traffic; requests go to the remaining replicas. An instance that has crashed is started again by its `Deployment` |
| Response measure | The instance is removed after three failed readiness probes, five seconds apart: about 15 seconds |
| Evidence | **By design**: from the probe configuration of the Helm chart. Only `gateway-service` has more than one replica at rest, and the services have no liveness probe, so an instance that hangs while still answering `/health` is not restarted |

### QAS-5: The message broker is unavailable

| Part | Description |
|---|---|
| Source | A failure of Kafka or of `notification-service` |
| Stimulus | A customer pays an order, or reserves a table, while events cannot be delivered |
| Artifact | `payment-service`, `table-reservation-service`, and their Kafka publishers |
| Environment | Degraded: the broker does not accept messages |
| Response | The payment or the reservation is stored and the customer receives the normal answer; the failure to publish is logged |
| Response measure | The outcome and the response time seen by the customer do not depend on the broker |
| Evidence | **By design**: the event is published after the local state is stored, and the use case does not wait for it or fail on it. The notification itself is lost: there is no outbox or retry |

### QAS-6: Concurrent changes to the same order

| Part | Description |
|---|---|
| Source | Two requests acting on the same order at the same moment, for example a customer cancelling it while it enters preparation |
| Stimulus | Both commands load the same version of the order and try to append their events |
| Artifact | The event store of `order-service` |
| Environment | Normal operation |
| Response | The first append succeeds; the second is rejected as a concurrent modification and changes nothing |
| Response measure | No update is lost and the stream of the order has no gap or duplicate sequence number |
| Evidence | **By design**: a unique index on order id and sequence number rejects the second append (optimistic concurrency) |

## Security

### QAS-7: Request without the required identity or role

| Part | Description |
|---|---|
| Source | An unauthenticated client, or a customer calling a route reserved to managers |
| Stimulus | A request to a protected route with no valid token, or with a token of the wrong role |
| Artifact | `gateway-service` |
| Environment | Normal operation |
| Response | The gateway rejects the request |
| Response measure | No service behind the gateway receives the request |
| Evidence | **By design**: the authentication and role middlewares run before the forwarding handler of every route that declares a role |

### QAS-8: Paying another customer's order

| Part | Description |
|---|---|
| Source | An authenticated customer |
| Stimulus | A payment request for an order placed by someone else, or with a customer id forged in the body |
| Artifact | `gateway-service` and `payment-service` |
| Environment | Normal operation |
| Response | The gateway overwrites the customer id with the one in the token; `payment-service` reads the order, sees that it belongs to another customer and refuses |
| Response measure | No charge is made and no payment is stored as completed |
| Evidence | **By design**: the ownership check comes before the call to the payment gateway |

## Modifiability and maintainability

### QAS-9: A service changes its API

| Part | Description |
|---|---|
| Source | A developer of a provider service |
| Stimulus | A change to a path, a required role or the fields of a request or response |
| Artifact | The `-shared` module of the provider, and every consumer built on it |
| Environment | Development time |
| Response | The consumers that no longer match the contract fail to compile or to build |
| Response measure | The incompatibility is found before any test runs and before anything is deployed |
| Evidence | **By design**: the gateway routes extend the API classes of the `-shared` modules, and consumers parse Kafka messages with the producers' deserializers. A provider whose implementation diverges from its own contract is not caught this way |

### QAS-10: A change breaks the layering

| Part | Description |
|---|---|
| Source | A developer |
| Stimulus | A domain class starts depending on the application or infrastructure layer, or a use case is placed outside `application.usecase` |
| Artifact | A Kotlin service that opted into the architecture rules |
| Environment | Development time |
| Response | The architecture test fails |
| Response measure | The change cannot be pushed or merged: the test runs in the `check` task, hence in the pre-push hook and in CI |
| Evidence | **Tested**: Konsist tests in `user-service`, `restaurant-service` and `order-service`. The Express.js services have no equivalent check |

## Observability

### QAS-11: A slow request is investigated

| Part | Description |
|---|---|
| Source | A developer or an operator |
| Stimulus | A request is reported as slow or failed |
| Artifact | The observability stack and the instrumented services |
| Environment | Normal operation on Kubernetes |
| Response | From a log line of the request, the whole trace is opened: every service the request went through, including the consumption of its Kafka event, with the time spent in each |
| Response measure | One trace id covers the request end to end, and each span links back to the log lines of that trace |
| Evidence | **Tested** for the propagation: component tests of `order-service` check that an incoming trace is continued, that the log lines carry its id and that the published Kafka message carries it. `scheduler-service` is not instrumented yet |

## Deployability

### QAS-12: A single service is redeployed

| Part | Description |
|---|---|
| Source | A developer releasing a change to one service |
| Stimulus | `./gradlew deploy -Pservice=<name>` |
| Artifact | The service, its namespace and its database |
| Environment | The rest of the system running |
| Response | Only that service is rebuilt and rolled out; the others keep running and keep finding it under the same DNS name |
| Response measure | No other service is rebuilt, reconfigured or restarted; the data of the service survives the redeployment |
| Evidence | **By design**: one namespace, one Helm release and one database per service, with persistent volumes kept on uninstall |
