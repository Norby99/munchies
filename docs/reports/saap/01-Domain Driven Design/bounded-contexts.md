# Bounded Contexts

Starting from the pivotal points and the aggregates that emerged during the [Event Storming](event-storming.md), the
domain of _Munchies_ has been split into the following bounded contexts:

- **User**
- **Restaurant**
- **Table Reservation**
- **Order**
- **Schedule**
- **Delivery**
- **Payment**
- **Notification**

The contexts can be grouped into three areas:

- **Restaurant setup** (`Restaurant`, `Table Reservation`): everything a manager configures before customers can
  interact with a restaurant, and the bookings that depend on that configuration.
- **Ordering flow** (`Order`, `Schedule`, `Payment`): the heart of the system, where an order is scheduled, created,
  paid and prepared. Most of the policies discovered during the Event Storming live here.
- **Standalone contexts** (`User`, `Delivery`): identity on one side, the physical delivery run on the other.

`Notification` did not emerge as a context on the Event Storming board because it does not own any domain command: it
only reacts to events produced by the other contexts (see [Notification](#notification)).

### User

Manages user accounts, their profile and their role (`Customer` or `Manager`), and authenticates them.

```mermaid
classDiagram
    direction LR
    namespace UserAggregate {
        class User {
            <<AggregateRoot>>
            +UserId id
            +UserProfile profile
            +updateEmailAsVerified() User
        }
        class UserProfile {
            <<ValueObject>>
            +String username
            +Email email
            +UserRole role
        }
        class Email {
            <<ValueObject>>
            +String address
            +Boolean isVerified
        }
        class UserRole {
            <<Enumeration>>
            CUSTOMER
            MANAGER
        }
    }
    namespace CredentialsAggregate {
        class UserCredentials {
            <<AggregateRoot>>
            +UserId id
            +String passwordHash
            +String salt
            +Int loginAttempts
            +Long lockedUntil
            +Long lastLogin
        }
    }
    class UserEmailConfirmationNotification {
        <<DomainEvent>>
        +String userId
        +String confirmationKey
    }
    class UserRepository {
        <<Repository>>
    }
    class UserCredentialsRepository {
        <<Repository>>
        +incrementLoginAttempts(UserId)
        +resetLoginAttempts(UserId)
    }
    class PasswordHasher {
        <<Port>>
        +hash(password, salt) String
        +generateSalt() String
    }

    User *-- UserProfile
    UserProfile *-- Email
    UserProfile --> UserRole
    UserCredentials ..> User: same UserId
    User ..> UserEmailConfirmationNotification: emits
    UserRepository ..> User: persists
    UserCredentialsRepository ..> UserCredentials: persists
    UserCredentials ..> PasswordHasher: uses
```

### Restaurant

Manages the restaurant site and its configuration: details, menus, categories and menu items.

```mermaid
classDiagram
    direction LR
    namespace RestaurantAggregate {
        class Restaurant {
            <<AggregateRoot>>
            +RestaurantId id
            +UserId managerId
            +RestaurantDetails details
            +updateDetails(RestaurantDetails)
        }
        class RestaurantDetails {
            <<ValueObject>>
            +RestaurantName name
            +Address address
            +Phone phone
            +Email email
        }
    }
    namespace MenuAggregate {
        class Menu {
            <<AggregateRoot>>
            +MenuId id
            +RestaurantId restaurantId
            +MenuName name
            +Validity validity
            +createCategory(CategoryName, List~Variation~) Category
            +updateValidity(Validity)
            +isValid(LocalDateTime) Boolean
        }
        class Category {
            <<Entity>>
            +CategoryId id
            +CategoryName name
            +createItem() MenuItem
            +removeItem(MenuItemId)
        }
        class MenuItem {
            <<Entity>>
            +MenuItemId id
            +MenuItemDetails details
            +Money price
            +isValid(LocalDateTime) Boolean
        }
        class Variation {
            <<ValueObject>>
            +VariationName name
            +List~VariationOption~ options
        }
        class Validity {
            <<ValueObject>>
            +isValid(LocalDateTime) Boolean
            +combine(Validity) Validity
        }
        class Money {
            <<ValueObject>>
            +BigDecimal amount
        }
    }
    class RestaurantRepository {
        <<Repository>>
        +findAllByManagerId(UserId) List~Restaurant~
    }
    class MenuRepository {
        <<Repository>>
        +findAllByRestaurantId(RestaurantId) List~Menu~
    }

    Restaurant *-- RestaurantDetails
    Menu "1" *-- "*" Category
    Category "1" *-- "*" MenuItem
    Category *-- Variation
    MenuItem *-- Variation
    MenuItem *-- Money
    Menu *-- Validity
    MenuItem *-- Validity
    Menu ..> Restaurant: restaurantId
    RestaurantRepository ..> Restaurant: persists
    MenuRepository ..> Menu: persists
```

### Table Reservation

Manages the booking of a table in a restaurant for a given time slot.

```mermaid
classDiagram
    direction LR
    namespace TableReservationAggregate {
        class TableReservation {
            <<AggregateRoot>>
            +ReservationId id
            +RestaurantId restaurantId
            +CustomerId customerId
            +TimeSlot slot
            +Int numberOfGuests
            +ReservationStatus status
            +modify(TimeSlot, Int)
            +cancel()
        }
        class TimeSlot {
            <<ValueObject>>
            +DateTime start
            +DateTime end
        }
        class ReservationStatus {
            <<Enumeration>>
            REQUESTED
            ACCEPTED
            CANCELED
        }
    }
    class TableReservationAccepted {
        <<DomainEvent>>
    }
    class TableReservationModified {
        <<DomainEvent>>
    }
    class TableReservationCanceled {
        <<DomainEvent>>
    }
    class TableReservationRepository {
        <<Repository>>
    }

    TableReservation *-- TimeSlot
    TableReservation --> ReservationStatus
    TableReservation ..> TableReservationAccepted: emits
    TableReservation ..> TableReservationModified: emits
    TableReservation ..> TableReservationCanceled: emits
    TableReservationRepository ..> TableReservation: persists
```

### Order

Manages the lifecycle of an order, whatever its fulfilment type (delivery, takeaway, dine-in), from its creation to its
completion.

```mermaid
classDiagram
    direction TB
    namespace OrderAggregate {
        class Order {
            <<AggregateRoot>>
            +OrderId id
            +RestaurantId restaurantId
            +CustomerId customerId
            +OrderStatus status
            +List~OrderItem~ items
            +Boolean payed
            +nextStatus() AdvanceStatusResult
            +pay() PayResult
            +cancel() CancelResult
            +updateItems(List~OrderItem~) UpdateResult
            +apply(OrderEvent) Order
        }
        class DeliveryOrder {
            <<Entity>>
            +DeliveryInfo deliveryInfo
            +updateInfo() UpdateResult
        }
        class TakeawayOrder {
            <<Entity>>
            +TakeawayInfo takeawayInfo
            +updateInfo() UpdateResult
        }
        class DineInOrder {
            <<Entity>>
            +TableInfo tableInfo
        }
        class OrderItem {
            <<ValueObject>>
            +MenuItemId menuItemId
            +Int quantity
        }
        class DeliveryInfo {
            <<ValueObject>>
            +Long estimatedDeliveryTime
            +String deliveryAddress
            +String bellName
            +String customerPhone
        }
        class TakeawayInfo {
            <<ValueObject>>
            +Long pickupTime
            +String customerName
        }
        class TableInfo {
            <<ValueObject>>
            +Int tableNumber
            +Int numberOfGuests
        }
        class OrderStatus {
            <<Enumeration>>
            PENDING
            PREPARING
            READY
            ON_THE_WAY
            COMPLETED
            CANCELLED
        }
    }
    class OrderEvent {
        <<DomainEvent>>
        +OrderId orderId
        +Long occurredAt
    }
    class OrderPlaced {
        <<DomainEvent>>
    }
    class OrderStatusAdvanced {
        <<DomainEvent>>
    }
    class OrderItemsUpdated {
        <<DomainEvent>>
    }
    class OrderPaid {
        <<DomainEvent>>
    }
    class OrderCancelled {
        <<DomainEvent>>
    }
    class OrderFactory {
        <<Factory>>
        +createDelivery() OrderCreationResult
        +createTakeaway() OrderCreationResult
        +createDineIn() OrderCreationResult
        +fromHistory(List~OrderEvent~) Order
    }
    class OrderEventStore {
        <<Repository>>
        +load(OrderId) List~OrderEvent~
        +append(OrderId, expectedVersion, List~OrderEvent~)
    }
    class OrderViewRepository {
        <<Repository>>
        +findById(OrderId) OrderView
        +save(OrderView)
    }
    class OrderNotificationPublisher {
        <<Port>>
        +publishStatusChanged(Order)
    }

    Order <|-- DeliveryOrder
    Order <|-- TakeawayOrder
    Order <|-- DineInOrder
    Order "1" *-- "*" OrderItem
    Order --> OrderStatus
    DeliveryOrder *-- DeliveryInfo
    TakeawayOrder *-- TakeawayInfo
    DineInOrder *-- TableInfo
    OrderEvent <|-- OrderPlaced
    OrderEvent <|-- OrderStatusAdvanced
    OrderEvent <|-- OrderItemsUpdated
    OrderEvent <|-- OrderPaid
    OrderEvent <|-- OrderCancelled
    Order ..> OrderEvent: emits / applies
    OrderFactory ..> Order: creates
    OrderEventStore ..> OrderEvent: stores
    OrderViewRepository ..> Order: projection of
    OrderNotificationPublisher ..> Order: publishes status of
```

### Schedule

Computes the available time slots for pickup and delivery and plans the delivery runs, assigning drivers to them. It
acts as a middleware between the customer and the `Order` context.

```mermaid
classDiagram
    direction LR
    namespace ScheduleAggregate {
        class Schedule {
            <<AggregateRoot>>
            +ScheduleId id
            +RestaurantId restaurantId
            +Date day
            +List~TimeSlot~ availableSlots
            +List~DeliveryRun~ runs
            +generateTimeTable()
            +scheduleDeliveries()
            +assignDrivers()
        }
        class DeliveryRun {
            <<Entity>>
            +RunId id
            +DriverId driverId
            +List~OrderId~ orders
            +TimeSlot departure
        }
        class TimeSlot {
            <<ValueObject>>
            +DateTime start
            +DateTime end
            +Int capacity
        }
    }
    class AvailableTimeTableGenerated {
        <<DomainEvent>>
    }
    class DeliveriesScheduled {
        <<DomainEvent>>
    }
    class DriversAssigned {
        <<DomainEvent>>
    }
    class ScheduleRepository {
        <<Repository>>
    }

    Schedule "1" *-- "*" TimeSlot
    Schedule "1" *-- "*" DeliveryRun
    DeliveryRun *-- TimeSlot
    Schedule ..> AvailableTimeTableGenerated: emits
    Schedule ..> DeliveriesScheduled: emits
    Schedule ..> DriversAssigned: emits
    ScheduleRepository ..> Schedule: persists
```

### Delivery

Tracks the physical delivery run performed by a driver.

```mermaid
classDiagram
    direction LR
    namespace DeliveryAggregate {
        class Delivery {
            <<AggregateRoot>>
            +DeliveryId id
            +RunId runId
            +DriverId driverId
            +List~OrderId~ orders
            +DeliveryStatus status
            +depart()
            +arrive(OrderId)
            +returnToRestaurant()
        }
        class DeliveryStatus {
            <<Enumeration>>
            ASSIGNED
            DEPARTED
            ARRIVED
            RETURNED
        }
    }
    class DriverDeparted {
        <<DomainEvent>>
    }
    class DriverArrived {
        <<DomainEvent>>
    }
    class DriverReturned {
        <<DomainEvent>>
    }
    class DeliveryRepository {
        <<Repository>>
    }

    Delivery --> DeliveryStatus
    Delivery ..> DriverDeparted: emits
    Delivery ..> DriverArrived: emits
    Delivery ..> DriverReturned: emits
    DeliveryRepository ..> Delivery: persists
```

### Payment

Handles the payment of an order, either online through an external payment gateway or offline at the restaurant.

```mermaid
classDiagram
    direction LR
    namespace PaymentAggregate {
        class Payment {
            <<AggregateRoot>>
            +PaymentId id
            +OrderId orderId
            +PaymentStatus status
            +Number amount
            +Currency currency
            +PaymentMethod method
            +Date payedAt
            +complete() Payment
            +fail() Payment
            +cancel() Payment
        }
        class PaymentStatus {
            <<Enumeration>>
            PENDING
            COMPLETED
            FAILED
            CANCELLED
        }
        class PaymentMethod {
            <<Enumeration>>
            CARD
            CASH
            MOBILE_PAYMENT
            BANK_TRANSFER
            OTHER
        }
    }
    class PaymentSuccessNotification {
        <<DomainEvent>>
        +String paymentId
        +String orderId
        +Int amount
        +Currency currency
    }
    class PaymentRepository {
        <<Repository>>
        +findById(PaymentId) Payment
        +findByOrderId(OrderId) Payment
        +save(Payment)
    }
    class PaymentGateway {
        <<Port>>
        +process() PaymentExecutionResult
    }
    class OrderServiceClient {
        <<Port>>
        +markOrderAsPaid(orderId) Result
    }
    class PaymentNotificationPublisher {
        <<Port>>
        +publishPaymentSuccess(Payment)
    }

    Payment --> PaymentStatus
    Payment --> PaymentMethod
    Payment ..> PaymentSuccessNotification: emits
    PaymentRepository ..> Payment: persists
    Payment ..> PaymentGateway: charged through
    Payment ..> OrderServiceClient: notifies Order
    PaymentNotificationPublisher ..> PaymentSuccessNotification: publishes
```

### Notification

Sends notifications (email/SMS) to customers and restaurant staff. It owns no aggregate and accepts no command: it is
an event-driven agent that only reacts to the events published by the other contexts, and its model is the set of
event payloads it consumes.

```mermaid
classDiagram
    direction LR
    class NotificationAgent {
        <<Agent>>
        +onUserEmailConfirmation(UserEmailConfirmationNotification)
        +onOrderStatusChanged(OrderStatusChangedNotification)
        +onPaymentSuccess(PaymentSuccessNotification)
    }
    class UserEmailConfirmationNotification {
        <<PublishedLanguage>>
        +String userId
        +String confirmationKey
    }
    class OrderStatusChangedNotification {
        <<PublishedLanguage>>
        +String orderId
        +String restaurantId
        +String customerId
        +String status
    }
    class PaymentSuccessNotification {
        <<PublishedLanguage>>
        +String paymentId
        +String orderId
        +Int amount
        +Currency currency
    }

    UserEmailConfirmationNotification <.. NotificationAgent: consumes
    OrderStatusChangedNotification <.. NotificationAgent: consumes
    PaymentSuccessNotification <.. NotificationAgent: consumes
```
