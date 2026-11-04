# Availability

- Is there any __caching__ mechanism?
    * _where_? _why_?

- Is there any form of __load balancing__?
    * _where_? _why_?

- In case of __network partitioning__, how does the system behave?
    * _why_? _how_?

---

# Availability of Munchies

## Caching

No dedicated cache layer is used. Reads are served by indexed queries on a dedicated database, and a separate cache
would add an invalidation problem, in a system where order status must always be up to date.

Two mechanisms play a similar role in specific places:

| Mechanism           | Where           | Why                                                                    |
|---------------------|-----------------|------------------------------------------------------------------------|
| Order views         | `order-service` | They act as a cache of the event log, sparing a replay on each read    |
| Cart in the browser | Web client      | The draft of an order survives an expired session or a page reload     |

Order views exist because of the CQRS pattern applied to orders, where everything is driven by events: the state of an
order is only stored as a sequence of events, so a query would otherwise have to rebuild every order it involves from
scratch. To avoid this, the system keeps a view of each order, which is updated at every new event. Unlike a generic
cache, a view has no expiry and needs no invalidation: it is changed by the same events that change the order, and it
can always be rebuilt from the log.

The first candidate for a cache would be the catalogue, as it is read often, changes rarely and tolerates being
slightly stale.

## Load balancing

Load balancing between replicas is performed by Kubernetes. Callers address a service by its name, and Kubernetes
dynamically assigns incoming API calls to its replicas, taking into account only those that are ready. As replicas are
added, removed or replaced, the assignment follows with no change on the side of the callers.

| Where                        | How                                                                                    | Why                                                    |
|------------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------|
| Across replicas of a service | Kubernetes dynamically assigns API calls to the replicas that are ready                | Replicas are interchangeable, so no affinity is needed |
| Across topics                | Each topic has its own consumer                                                        | A busy topic does not slow down the others             |
| Over time                    | The broker buffers events, and the consumer takes them at its own pace                 | A burst of events does not overwhelm the consumer      |

### Autoscaling

Load balancing is paired with autoscaling, which follows the bursty pattern of meals (`NFR1`). Autoscaling is also
performed by Kubernetes, which monitors the CPU usage of each service and adjusts the number of its replicas with no
human intervention. The gateway and the user, restaurant and order services go from 1 to 4 replicas, targeting 60% of
CPU usage. Scaling out is fast, to absorb a peak as it begins, while scaling in removes one replica at a time, to avoid
oscillations.

Autoscaling is safe on these services because they are stateless: a new replica is immediately able to serve any
request.

## Network partitioning

The effects of a partition depend on where it occurs.

| Partition                    | Behaviour                                                                                                                                | Why                                                       |
|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| Client and gateway           | The client cannot use the system. Its cart is safe, and any command whose answer was lost may have been applied                          | The client has no offline mode for server operations      |
| Gateway and one service      | Requests to that service fail after the timeout. Other services keep answering                                                           | Partial availability: the failure stays in its context    |
| Service and its database     | That service refuses commands and queries, and leaves rotation                                                                           | Consistency is preferred to availability                  |
| Service and broker           | Writes succeed, but events are not published. `order-service` reports an error for a stored change, while `payment-service` only logs it | Notifications are not essential to the business operation |
| Replicas of the same service | No effect, as replicas do not talk to each other                                                                                         | Shared state lives in the database                        |

In CAP terms, each service is **CP**: it has a single database, and when that is unreachable it refuses to answer,
instead of returning stale data or accepting writes that could conflict. This is the right choice for orders and
payments, where a wrong answer costs more than a failed one.

The system as a whole behaves in a more available way, since each service fails on its own. A customer may be unable
to pay, and still browse the menus. The price is that states across services can diverge during a partition, and
nothing reconciles them (`NFR11`).

Lost answers are the most subtle case: a client that times out cannot tell whether its command was applied. Commands
are not idempotent, so repeating one blindly has an effect: a second "place order" creates another order, and a second
"advance" moves the order one step further. The client must read the state before retrying. Idempotency keys would
solve this on the server side, and are not part of the current design.
