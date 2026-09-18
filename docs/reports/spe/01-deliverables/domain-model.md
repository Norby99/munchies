# Domain Model

## Bounded contexts

Each microservice *is* a bounded context — one Kotlin/TypeScript module, one dedicated MongoDB database (see [Microservices](../02-implementation/microservices.md)), one team-sized area of the [ubiquitous language](glossary.md). Nothing reaches across a context boundary except through its published HTTP API or an asynchronous event.

### User context

Owns user registration, authentication and profile management. The `User` aggregate and the `CUSTOMER`/`MANAGER` role hierarchy live here.

### Restaurant context

Owns restaurant identity and menu administration: the `Restaurant` and `Menu` aggregates, and everything nested inside a menu (categories, items, variations, validity windows).

### Order context

Owns the order lifecycle across its three fulfilment shapes (delivery, takeaway, dine-in) — the `Order` aggregate and its state machine.

### Payment context

Owns the payment lifecycle for an order — the `Payment` aggregate, modeled independently of `Order` rather than as a field on it.

### Notification context

Owns delivering notifications to users. Unlike the others it has **no inbound HTTP API of its own** — it exists purely to react to events published by other contexts (see the context map below).

### Table Reservation context

Owns table booking. Present in the codebase but incomplete (see [Microservices](../02-implementation/microservices.md)) — excluded from the diagrams and the deployment.

```mermaid
flowchart LR
    subgraph User["User context"]
        U["User<br/>(CUSTOMER / MANAGER)"]
    end
    subgraph Restaurant["Restaurant context"]
        R["Restaurant"] --> M["Menu"] --> C["Category"] --> MI["MenuItem"]
    end
    subgraph Order["Order context"]
        O["Order<br/>(Delivery / Takeaway / DineIn)"]
    end
    subgraph Payment["Payment context"]
        P["Payment"]
    end
    subgraph Notification["Notification context"]
        N["email / push notifications"]
    end

    U -. manages .-> R
    O -. references restaurantId .-> R
    O -. references customerId .-> U
    O -. paid via .-> P
    P -. references orderId .-> O
    U == "UserEmailConfirmationNotification" ==> N
    P == "PaymentSuccessNotification" ==> N
```

The dotted lines are **references by ID only** (`RestaurantId`, `CustomerId`, `OrderId` — plain value objects, never a foreign key or a shared table) — a hard requirement of the database-per-service split. The double lines are the real asynchronous integrations, detailed in the context map below.

## Context map: events between contexts

Contexts don't call each other synchronously to stay decoupled — they communicate through domain events, following a **Published Language** shape defined once in `commons` (see [Tactical building blocks](#tactical-building-blocks)) and specialized per context.

### User context

**Outbound events**

- `UserEmailConfirmationNotification` — emitted when a user registers, carrying what's needed to send a confirmation email.

### Restaurant context

No published events at this time.

### Order context

No published events at this time — `order-service` is called synchronously by the gateway and by `payment-service` (to confirm an order once its payment succeeds), rather than publishing events of its own.

### Payment context

**Outbound events**

- `PaymentSuccessNotification` — emitted when a payment for an order completes.

### Notification context

**Inbound events**

- `UserEmailConfirmationNotification` (from the User context) — consumed to send the email-verification message.
- `PaymentSuccessNotification` (from the Payment context) — consumed to notify the customer their payment went through.

The Notification context is a pure event sink: it has two independent Kafka consumers, one per inbound event type, and publishes nothing of its own. Concretely — `user-service` publishes via a `@KafkaClient` ([`EmailConfirmationClient`](https://github.com/Norby99/munchies/blob/master/user-service/src/main/kotlin/com/munchies/user/infrastructure/adapter/outbound/kafka/EmailConfirmationClient.kt)); `notification-service` consumes it independently ([`KafkaUserEmailConfirmationNotificationConsumer`](https://github.com/Norby99/munchies/blob/master/notification-service/src/main/ts/infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer.ts)). Neither context calls the other directly or needs the other to be running at the moment the event is published — user registration finishes successfully even if the Notification context is temporarily down, which is exactly the point of modeling this as an event rather than a request/response call.

## Tactical building blocks

Rather than reinvent Entity/Value-Object/Aggregate/Repository per context, they're defined once in [`commons`](https://github.com/Norby99/munchies/blob/master/commons/src/commonMain/kotlin/com/munchies/commons/DDD.kt) — a Kotlin Multiplatform module compiled to both JVM and JS (see [Multiplatform](../02-implementation/multiplatform.md)) — and every bounded context extends them:

```kotlin
open class EntityId<Id>(open val value: Id) { /* equality by value */ }
open class Entity<Id : EntityId<*>>(open val id: Id) { /* equality by id */ }
open class AggregateRoot<Id : EntityId<*>>(id: Id) : Entity<Id>(id)
interface Factory<E : Entity<*>>
interface Repository<Id : EntityId<*>, E : Entity<Id>> { fun findById(id: Id): E?; fun save(entity: E); /* ... */ }
```

The event-publishing shape from the context map above is defined the same way, generically:

```kotlin
interface Notification
interface NotificationObserver<N : Notification> { fun update(event: N) }
interface NotificationSubject<N : Notification, O : NotificationObserver<N>> {
  fun attach(observer: O); fun detach(observer: O); fun emit(event: N)
}
```

— a textbook Observer pattern, `@JsExport`'d so the same contract shape is usable from a Kotlin service or a TypeScript one, which is how `UserEmailConfirmationNotification` and `PaymentSuccessNotification` both end up structured identically despite living in different languages.

## Aggregates, entities and value objects

| Bounded context | Aggregate root(s) | Notable entities | Notable value objects |
| --- | --- | --- | --- |
| User | `User` | — | `UserId`, `UserProfile`, `Email` (with verification state), `UserRole` |
| Restaurant | `Restaurant`, `Menu` | `Category`, `MenuItem` (inside `Menu`) | `RestaurantId`, `RestaurantName`, `Address`, `Phone`, `Email`, `Money`, `Validity`, `Variation` |
| Order | `Order` (sealed: `DeliveryOrder` / `TakeawayOrder` / `DineInOrder`) | — | `OrderId`, `RestaurantId`, `CustomerId`, `OrderItem`, `OrderStatus`, `DeliveryInfo` |
| Payment | `Payment` | — | `PaymentId`, `Currency`, `PaymentMethod`, `PaymentStatus` |

Three patterns worth calling out, because they're not just "an entity with fields":

### Polymorphic aggregate with an invariant-guarded lifecycle — `Order`

`Order` is a `sealed class` with three concrete shapes (`DeliveryOrder`, `TakeawayOrder`, `DineInOrder`), so the compiler — not a runtime `when` with a default branch — guarantees every order-fulfilment path is handled. State transitions aren't free-form field assignment:

```kotlin
fun cancel(): CancelResult {
  if (status != OrderStatus.PENDING) return CancelResult.Failure.InvalidTransition
  return CancelResult.Success(copyWithStatus(OrderStatus.CANCELLED))
}
```

An order can only be cancelled while `PENDING` — the invariant lives inside the aggregate itself, not in a controller or a service class that could forget to check it. Every mutation (`nextStatus()`, `pay()`, `updateItems()`) returns a sealed `Result` type (`Success`/`Failure`) instead of throwing, so callers are forced by the type system to handle rejection instead of relying on a try/catch they might omit.

### Construction through a validating factory — `User`

```kotlin
class User private constructor(override val id: UserId, val profile: UserProfile) : Entity<UserId>(id) {
  companion object {
    val factory: UserFactory = DefaultUserFactory()
  }
}
```

The constructor is `private` — the *only* way to obtain a `User` is `User.factory.create(...)`, which validates the nested `UserProfile` (non-empty username, non-empty email) and returns a `Success`/`Failure` result. It is not possible to hold a reference to an invalid `User` anywhere in the codebase; the type system rules it out. `Restaurant` and `Menu` follow the same private-constructor-plus-companion-factory shape.

### Composite value object — `Validity`

```kotlin
sealed interface Validity {
  fun isValid(date: LocalDateTime): Boolean
  fun combine(other: Validity): Validity = And(this, other)
  // variants: Period, Weekly, Yearly, Hours, Always, And
}
```

A menu item's availability window is expressed by composing simple rules (`Validity.hours(11, 14).combine(Validity.weekly(listOf(6, 7)))` — "weekend lunch only") rather than a single flat `startDate`/`endDate`/`daysOfWeek` struct with implicit AND semantics. This is the textbook Composite pattern applied to a value object: `And` is itself a `Validity` holding two `Validity`s.

!!! note "Not every service names its layers the same way"
    `order-service`, `user-service` and `payment-service` use `domain/model` + `domain/port`; `restaurant-service` uses `domain/aggregate` + `domain/valueobject` + `domain/repository`. Both are valid DDD vocabulary for the same concepts, and the [Konsist architecture tests](../02-implementation/microservices.md) don't care which subfolder names are used — they check the *dependency direction* between domain/application/infrastructure, not folder naming. Worth flagging as a real inconsistency rather than presenting the codebase as more uniform than it is.
