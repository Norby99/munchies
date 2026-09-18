# Glossary

The ubiquitous language shared between the domain and the code — every term below names a class, enum, or field somewhere in the codebase, not just a concept on paper.

| Term | Meaning | Where it lives |
| --- | --- | --- |
| **Customer** | An end user who browses restaurants, places orders and books tables. The default `UserRole`. | [`UserRole.CUSTOMER`](https://github.com/Norby99/munchies/blob/master/user-service/src/main/kotlin/com/munchies/user/domain/model/UserRole.kt) |
| **Manager** | A user who owns and administers one restaurant: its details, menus, categories and items. `MANAGER` visibility is a superset of `CUSTOMER` — a manager can do everything a customer can, plus restaurant administration. | [`UserRole.MANAGER`](https://github.com/Norby99/munchies/blob/master/user-service/src/main/kotlin/com/munchies/user/domain/model/UserRole.kt) |
| **Restaurant** | An establishment owned by exactly one manager, identified by name, address, phone and email. Aggregate root for restaurant identity/details. | [`Restaurant`](https://github.com/Norby99/munchies/blob/master/restaurant-service/src/main/kotlin/com/munchies/restaurant/domain/aggregate/Restaurant.kt) |
| **Menu** | A named, orderable listing of `Category` → `MenuItem`, scoped to one restaurant. A restaurant can have more than one (e.g. a lunch menu and a dinner menu), each independently time-bounded by its own `Validity`. | [`Menu`](https://github.com/Norby99/munchies/blob/master/restaurant-service/src/main/kotlin/com/munchies/restaurant/domain/aggregate/Menu.kt) |
| **Category** | A grouping of menu items inside a `Menu` (e.g. "Starters"), itself entity-identified so items can be added, moved or removed without touching the rest of the menu. | `Category`, same file as `Menu` |
| **Menu Item** | A single orderable dish: name, description, `Money` price, optional `Variation`s, and its own `Validity` window (an item can be limited-time even inside an always-available menu). | `MenuItem`, same file as `Menu` |
| **Variation** | A named customization option on a menu item or category (e.g. size, spice level). | [`Variation`](https://github.com/Norby99/munchies/blob/master/restaurant-service/src/main/kotlin/com/munchies/restaurant/domain/valueobject/menu/Variation.kt) |
| **Validity** | *When* something is orderable — a composable rule (`Period`, `Weekly`, `Yearly`, `Hours`, or `Always`, combinable with `.combine()`) attached to a `Menu` or `MenuItem`. | [`Validity`](https://github.com/Norby99/munchies/blob/master/restaurant-service/src/main/kotlin/com/munchies/restaurant/domain/valueobject/menu/Validity.kt) |
| **Order** | A customer's request to buy a set of `OrderItem`s from one restaurant. Comes in three shapes — see below. Tracks its own `OrderStatus` and whether it's `payed`. | [`Order`](https://github.com/Norby99/munchies/blob/master/order-service/src/main/kotlin/com/munchies/order/domain/model/Order.kt) (sealed) |
| **Delivery Order** | An `Order` fulfilled by delivery to an address, carrying `DeliveryInfo` (estimated time, address, bell name, contact phone). | [`DeliveryOrder`](https://github.com/Norby99/munchies/blob/master/order-service/src/main/kotlin/com/munchies/order/domain/model/DeliveryOrder.kt) |
| **Takeaway Order** | An `Order` the customer collects in person. | `TakeawayOrder`, same package |
| **Dine-In Order** | An `Order` placed at a table inside the restaurant. | `DineInOrder`, same package |
| **Order Status** | The order's position in its lifecycle: `PENDING → PREPARING → READY → ON_THE_WAY → COMPLETED`, with `CANCELLED` reachable only from `PENDING`. | [`OrderStatus`](https://github.com/Norby99/munchies/blob/master/order-service/src/main/kotlin/com/munchies/order/domain/model/OrderStatus.kt) |
| **Payment** | The record of money changing hands for an order: amount, `Currency`, `PaymentMethod`, and its own lifecycle (`PENDING → COMPLETED`/`FAILED`/`CANCELLED`). Modeled independently of `Order` — a separate bounded context. | [`Payment`](https://github.com/Norby99/munchies/blob/master/payment-service/src/main/ts/domain/model/Payment.ts) |
| **Table Reservation** | A customer's booking of a restaurant table for a given time (bounded context present in the codebase but incomplete — see [Microservices](../02-implementation/microservices.md)). | `table-reservation-service` |
| **Notification** | An asynchronous message about something that happened elsewhere in the system (e.g. "a user registered") that another bounded context reacts to — the mechanism behind cross-context integration. See [Domain Model](domain-model.md#context-integration-domain-events-over-kafka). | [`Notification`](https://github.com/Norby99/munchies/blob/master/commons/src/commonMain/kotlin/com/munchies/commons/domain/port/Notification.kt) |

## Shared building blocks (not domain terms, but load-bearing vocabulary)

These aren't part of the *business* language, but every bounded context is built out of them, so they're worth defining once:

| Term | Meaning |
| --- | --- |
| **Entity** | Something with a persistent identity (`EntityId`) that survives changes to its other fields — equality is by ID, not by value. |
| **Value Object** | Something defined entirely by its data — equality is structural (`Money(10) == Money(10)` regardless of instance). No identity of its own. |
| **Aggregate Root** | The single entry point into a cluster of entities/value objects that must change together under one consistency boundary (e.g. you don't reach a `MenuItem` except through its `Menu`). |
| **Factory** | Encapsulates validated construction of an entity, often returning a `Success`/`Failure` result instead of throwing, so invalid domain objects can never exist. |

See [Domain Model](domain-model.md) for how these are actually implemented and where each bounded context uses them.
