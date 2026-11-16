# Fault-Tolerance

## Replication

| What               | Replication                                            | Why                                                                                        |
|--------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Stateless services | 1 to 4 replicas, with no coordination between them     | Scaling and redundancy come at no consistency cost, as replicas hold no state              |
| Databases          | None: one instance per service, on a persistent volume | `IR3` limits resources. The volume protects against a restart, but not against a lost disk |
| Broker             | None: one node, on a persistent volume                 | Same as above. Events survive a restart of the broker                                      |

Replication is applied only where it needs no consistency protocol. When a service runs a single replica, as it does
at low load, the redundancy is only temporary: the failure of that replica interrupts the service until it is
restarted.

Order data has a form of internal redundancy: views can always be rebuilt from the event log. Losing a view loses no
information.

Replicating the databases and the broker is the first step towards production. Since services do not depend on the
number of database or broker nodes, this would only change the deployment, and not the design.

## Heart-beating, timeouts and retries

| Mechanism                   | Between                                    | How it works                                                                                                                                                    | Why                                                                                                     |
|-----------------------------|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Readiness probe             | Platform and each service                  | Every 5 seconds the platform calls the health endpoint, and sends traffic only to replicas answering it. On the user, restaurant and order services, the check also covers the database | A replica that is starting or has lost its database receives no requests (`NFR3`)                       |
| Liveness probe              | Platform and databases                     | The platform pings each database every 15 seconds, and restarts it if it does not answer                                                                        | A stuck database is replaced with no human intervention                                                 |
| Consumer heartbeat          | `notification-service` and broker          | The broker tracks consumers in each group, and reassigns the work of a silent one                                                                               | Provided by the broker protocol                                                                         |
| Request timeout             | Gateway and services                       | Each upstream call fails after 10 seconds                                                                                                                       | A stuck service does not hold gateway resources forever (`NFR4`)                                        |
| Producer and client retries | Producers and consumers towards the broker | The broker clients retry transient errors with their default policy                                                                                             | Short broker unavailability stays invisible                                                             |
| Topic creation retry        | `notification-service`, on startup         | Up to 5 attempts, 2 seconds apart                                                                                                                               | Right after a broker restart, leader election can make the first attempts fail                          |
| Client retry                | Web client and user                        | REST calls are not retried automatically. On a `409`, the user can repeat the action on fresh data                                                              | Automatic retries are only safe on idempotent commands, and they would add load on a struggling service |

REST calls between services are deliberately not retried, since most commands are not idempotent: repeating one after a
timeout could apply it twice.

## Error handling

### Failure of a component

| Component failing        | What happens                                                                                                                                            | What keeps working                                 |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| One replica of a service | The platform stops sending it traffic and replaces it. Requests running on it fail                                                                      | Everything, if other replicas exist                |
| `gateway-service`        | Clients cannot reach the system until a replica is ready. No data is lost, as it holds none                                                             | Nothing from outside                               |
| `user-service`           | Login and account features fail. Sessions stay valid, since tokens are verified by the gateway alone                                                    | Browsing and ordering, for users already logged in |
| `restaurant-service`     | The catalogue is not available                                                                                                                          | Order features, which do not call it               |
| `order-service`          | Order features fail. A completed payment cannot reach its order, and the failure is logged                                                              | Login, browsing, catalogue administration          |
| `notification-service`   | Events wait in the broker, and are consumed when it comes back (`NFR6`)                                                                                 | Everything, with delayed notifications             |
| Database of a service    | That service fails, and its health check takes it out of rotation                                                                                       | The other services                                 |
| Broker                   | Producers fail to publish. Orders are stored, but the caller of a status change gets an error. Payments are unaffected, since publishing is best-effort | Everything else                                    |

Failures stay confined because services call each other as little as possible: browsing does not need orders, and
orders do not need the catalogue.

### Failure of a request

- **Business refusals** are expected outcomes, returned to the client as specific errors. Domain operations return
  typed results, instead of throwing, so each use case has to handle each outcome.
- **Upstream errors** are returned by the gateway with their original status and body, so the client always sees the
  real cause.
- **An unreachable service** is reported by the gateway as an error response, rather than as a dropped connection.
- **A malformed event** is logged and skipped, so that it cannot block its topic.
- **A failed read model update** is logged and does not fail the command, as the event is already durable.
- **A failed step after a payment** is logged and does not undo the payment. Each step runs regardless of the other.

### Isolating consumers

Each topic has its own consumer and consumer group, and starting one is independent of the others: a failure on one
topic is logged, and does not stop the rest of the agent.
