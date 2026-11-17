# Interaction

## Communication overview

| From              | To                                                    | Channel      | Style             | When                             | What                                                  |
|-------------------|-------------------------------------------------------|--------------|-------------------|----------------------------------|-------------------------------------------------------|
| Web client        | `gateway-service`                                     | REST         | Request-response  | On every user action             | Commands and queries                                  |
| `gateway-service` | `user-service`, `restaurant-service`, `order-service` | REST         | Request-response  | On every authorized request      | The same command or query, plus the caller's identity |
| `payment-service` | `order-service`                                       | REST         | Request-response  | After a completed payment        | Mark the order as paid                                |
| `user-service`    | Broker                                                | Asynchronous | Publish-subscribe | After an email is verified       | `UserEmailConfirmationNotification`                   |
| `payment-service` | Broker                                                | Asynchronous | Publish-subscribe | After a completed payment        | `PaymentSuccessNotification`                          |
| `order-service`   | Broker                                                | Asynchronous | Publish-subscribe | After a status change            | `OrderStatusChangedNotification`                      |
| Broker            | `notification-service`                                | Asynchronous | Publish-subscribe | As soon as an event is available | All the events above                                  |

The rule for choosing a channel follows from who needs the answer:

- **Synchronous**, when the caller cannot go on without the result. A customer placing an order must know right away
  whether it was accepted.
- **Asynchronous**, when the producer does not depend on the outcome. A payment is valid whether or not the email has
  been sent, so a slow or absent consumer must not affect it (`NFR6`).

Asynchronous communication is limited to notifications: other services do not exchange events, in order to keep
workflows explicit and easy to follow.

## Interaction patterns

| Pattern                      | Where                                    | Why                                                                                                                              |
|------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| API Gateway                  | Client towards services                  | A single entry point, with authentication handled once (`FR-A7`, `NFR12`)                                                        |
| Request-response             | All REST calls                           | Immediate outcome for commands and queries                                                                                       |
| Publish-subscribe            | Producers towards `notification-service` | Producers and consumer do not know each other, and do not need to be up at the same time                                         |
| Event-carried state transfer | Integration events                       | The event holds everything needed, so the consumer never calls back the producer, and a failure of the latter does not affect it |
| Shared contract              | Between each service and its callers     | Paths and payloads are defined once by the owning service, so callers cannot drift from them (`NFR18`)                           |
| Optimistic concurrency       | Concurrent commands on the same order    | The loser gets a conflict and can retry, with no locks held across requests (`NFR8`)                                             |

## Main interactions

### Login and authenticated requests

Credentials are only checked by `user-service`, while tokens are only handled by the gateway. Downstream services
receive requests that are already authenticated.

```mermaid
sequenceDiagram
    participant C as Web client
    participant G as gateway-service
    participant U as user-service
    participant O as order-service
    C ->> G: Login (credentials)
    G ->> U: Check credentials
    U -->> G: User id and role
    G ->> G: Sign token
    G -->> C: Session cookie
    C ->> G: Place order (cookie)
    G ->> G: Verify token and role
    G ->> O: Place order (caller's id)
    O -->> G: Created order
    G -->> C: Created order
```

### Placing an order

A command is accepted once its event is stored. The read model is updated in the same request, but its failure does
not fail the command, since the event is already durable.

```mermaid
sequenceDiagram
    participant G as gateway-service
    participant O as order-service
    participant E as Event log
    participant V as Order views
    G ->> O: Place order
    O ->> O: Validate items
    O ->> E: Append OrderPlaced
    E -->> O: Stored
    O ->> V: Project event
    O -->> G: Created order
```

### Advancing an order

```mermaid
sequenceDiagram
    participant G as gateway-service
    participant O as order-service
    participant E as Event log
    participant B as Broker
    participant N as notification-service
    G ->> O: Advance status
    O ->> E: Load and replay events
    E -->> O: Events, version n
    O ->> O: Compute next status
    O ->> E: Append OrderStatusAdvanced at version n
    alt Version n already taken
        E -->> O: Conflict
        O -->> G: 409 Conflict
    else Stored
        E -->> O: Stored
        O ->> B: OrderStatusChangedNotification
        O -->> G: Success
        B -->> N: OrderStatusChangedNotification
        N ->> N: Handle notification
    end
```

### Processing a payment

The payment is made durable first. The two following steps are independent of each other, and a failure of either
does not change the result of the payment. No compensation exists yet (`NFR11`).

```mermaid
sequenceDiagram
    participant Caller
    participant P as payment-service
    participant S as Payment provider
    participant O as order-service
    participant B as Broker
    Caller ->> P: Process payment
    P ->> S: Charge
    alt Rejected
        S -->> P: Refused
        P ->> P: Store payment as FAILED
        P -->> Caller: Payment rejected
    else Accepted
        S -->> P: Accepted
        P ->> P: Store payment as COMPLETED
        P ->> O: Mark order as paid
        P ->> B: PaymentSuccessNotification
        P -->> Caller: Payment completed
    end
```
