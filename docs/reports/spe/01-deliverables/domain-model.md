# Domain Model

The backend of _Munchies_ is divided into microservices that follows the Domain-Driven Design, inside a Hexagonal
layout.
Each microservice has the same folder structure and naming regardless of technological stack.

This section describes that structure, its class hierarchy, and how the layers depend on one another, using
[
```order-service```](https://github.com/Norby99/munchies/tree/master/order-service/src/main/kotlin/com/munchies/order) (
implemented in Kotlin)
and [```payment-service```](https://github.com/Norby99/munchies/tree/master/payment-service/src/main/ts) (implemented in
Express).

## Layers and dependency direction

As mentioned above, each microservice follows the Hexagonal layout, with it's ```infrastructure```,  ```application```
and ```domain``` layers:

```mermaid
flowchart LR
    subgraph infra["infrastructure"]
        direction TB
        controller["adapter/inbound/web/controller"]
        mongo["adapter/outbound/mongo"]
        kafka["adapter/outbound/kafka"]
        client["adapter/outbound/&lt;other-service&gt;"]
    end

    subgraph app["application"]
        direction TB
        inbound["port/inbound (one interface per use case)"]
        usecase["usecase (implements the inbound port)"]
    end

    subgraph dom["domain"]
        direction TB
        model["model (entities, value objects)"]
        factory["factory (aggregate construction)"]
        outbound["port (outbound interfaces)"]
    end

    controller -->|" calls "| inbound
    inbound -.->|" implemented by "| usecase
    usecase --> model
    usecase --> factory
    usecase -->|" depends on "| outbound
    mongo -.->|" implements "| outbound
    kafka -.->|" implements "| outbound
    client -.->|" implements "| outbound
    classDef domain fill: #9956e0, stroke: #333, stroke-width: 2px, color: #fff
    classDef application fill: #4a7fd6, stroke: #333, stroke-width: 2px, color: #fff
    classDef infrastructure fill: #e07b39, stroke: #333, stroke-width: 2px, color: #fff
```

Solid arrows are plain calls; dashed arrows represent a dependency from another class/interface.

## Package structure

Every service follows the same folder layout under its module root (```<service>/src/main/kotlin/...``` for Kotlin
services, ```<service>/src/main/ts``` for Express.js ones):

```
domain/
  model/        entities and value objects
  factory/      aggregate construction logic (only where non-trivial)
  port/         outbound interfaces (repository, external clients, publishers)
application/
  usecase/                    one class per use case
  port/inbound/               one inbound port interface per use case
  port/inbound/command/       input Command objects (optional, for non-trivial inputs)
infrastructure/
  adapter/inbound/web/controller/     REST controllers
  adapter/inbound/web/config/         DI wiring, OpenAPI configuration
  adapter/outbound/mongo/document/    Mongo document classes
  adapter/outbound/mongo/repository/  repository implementation
  adapter/outbound/mongo/factory/     domain <-> document mapping
  adapter/outbound/kafka/             Kafka producers/consumers
  adapter/outbound/<other-service>/   clients for other microservices' REST APIs
  adapter/dto/factory/                DTO <-> Command mapping
```

## Class hierarchy: the `Order` aggregate

Here is a high level representation of the domain model of the system.

```mermaid
classDiagram
    class User {
        +UserId id
        +UserProfile profile
    }
    class UserProfile {
        +String username
        +Email email
        +UserRole role
    }
    class UserCredentials {
        +UserId id
        +String passwordHash
    }
    class UserRole {
        <<enumeration>>
        CUSTOMER
        MANAGER
    }

    class Restaurant {
        +RestaurantId id
        +UserId managerId
        +RestaurantDetails details
    }
    class Menu {
        +MenuId id
        +RestaurantId restaurantId
        +MenuName name
    }
    class Category {
        +CategoryId id
        +CategoryName name
    }
    class MenuItem {
        +MenuItemId id
        +MenuItemDetails details
        +Money price
    }

    class Order {
        <<abstract>>
        +OrderId id
        +RestaurantId restaurantId
        +CustomerId customerId
        +OrderStatus status
        +List~OrderItem~ items
        +Boolean payed
    }
    class DeliveryOrder {
        +DeliveryInfo deliveryInfo
    }
    class TakeawayOrder {
        +TakeawayInfo takeawayInfo
    }
    class DineInOrder {
        +TableInfo tableInfo
    }
    class OrderItem {
        +MenuItemId menuItemId
        +Int quantity
    }
    class OrderStatus {
        <<enumeration>>
        PENDING
        PREPARING
        READY
        ON_THE_WAY
        COMPLETED
        CANCELLED
    }

    class Payment {
        +PaymentId id
        +OrderId orderId
        +PaymentStatus status
        +Number amount
        +Currency currency
        +PaymentMethod method
    }

    User "1" *-- "1" UserProfile: has
    User "1" *-- "0..1" UserCredentials: secures
    UserProfile --> UserRole: has
    Restaurant "1" *-- "*" Menu: offers
    Menu "1" *-- "*" Category: groups
    Category "1" *-- "*" MenuItem: contains
    Order <|-- DeliveryOrder
    Order <|-- TakeawayOrder
    Order <|-- DineInOrder
    Order "1" *-- "*" OrderItem: contains
    Order --> OrderStatus: has
    Restaurant ..> User: managerId
    Order ..> Restaurant: restaurantId
    Order ..> User: customerId
    OrderItem ..> MenuItem: menuItemId
    Payment ..> Order: orderId
```
