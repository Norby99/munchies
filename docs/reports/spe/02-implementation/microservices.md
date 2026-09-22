# Microservices

_Munchies_ is split into nine packages, each owning its own data and, where it has any business logic, structured
according to Domain-Driven Design, with a clear separation between a framework-agnostic domain, an application
layer orchestrating use cases, and an infrastructure layer of framework-specific adapters (REST controllers,
MongoDB repositories, Kafka producers/consumers, and clients for other services). The sections below go through
each package, stating its stack and what it is actually responsible for.

## User Service

**Stack:** Kotlin + Micronaut.

Manages accounts, authentication: registering a new user, logging in, updating a user's
information or password, verifying an email address, and deleting an account.

## Restaurant Service

**Stack:** Kotlin + Micronaut.

Manages restaurant data, menus and dish availability: creating and updating restaurants, and managing their menus,
categories and individual items.

## Order Service

**Stack:** Kotlin + Micronaut.

Handles order creation, processing and status tracking, for both delivery and takeaway orders: placing an order,
updating its items or its delivery/takeaway details, advancing or discarding its status, marking it as paid, and
retrieving order details.

## Gateway Service

**Stack:** Express.js.

Acts as the single entry point for every client request, routing it to the right microservice and issues JWT tokens for
authentication.
As an API Gateway it holds no business
logic of its own, which is why it is the one backend service without a ```domain``` or ```application``` layer: its
```infrastructure``` layer consists entirely of routing, authentication middleware and proxying to the other
services.

## Notification Service

**Stack:** Express.js.

Sends notifications to users. It never receives requests directly from clients or from the gateway: it
only reacts to Kafka events, such as a user registering or a payment succeeding, each handled by its own Kafka
consumer. This reactive, one-consumer-per-event-type design is why it follows an
agent-based architecture instead of the usual use-case-driven one, and, correspondingly, why it has no
```application``` layer of its own: each consumer under ```infrastructure/adapter/inbound/kafka``` acts as an
independent agent reacting directly to its event.

## Payment Service

**Stack:** Express.js.

Handles payment processing for an order. It validates and processes a payment against a fake payment gateway,
since there is no real money movement in this project, confirms the corresponding order with ```order-service```,
and publishes a payment-success event on Kafka. It is our reference implementation for the Hexagonal Architecture layout
in Express.js.

## Scheduler Service - not implemented

**Stack:** Express.js.

Manages delivery scheduling and logistics, acting as a middleware between the user and ```order-service```.

## Table Reservation Service - not implemented

**Stack:** Express.js.

Manages table reservations for restaurants.

## Frontend Service

**Stack:** Vue.js, using the Composition API.

Provides the user interface for both customers and restaurant staff, and is the only client of
```gateway-service```. It follows a standard Vue project layout rather than the backend's hexagonal one, organized
into ```views```, reusable ```components```, ```composables``` for shared reactive logic, Pinia ```stores``` for
state, and a ```router``` for navigation between pages.

## Communication between services

Services communicate over **synchronous REST** by default. Every service exposes its API described with OpenAPI, and
the endpoint paths and request/response types are defined once in its ```<service>-shared``` module and reused by
callers on both runtimes (see [Multiplatform](multiplatform.md)), so a caller cannot drift from the API it calls.
The two flows that matter most:

- The browser only ever talks to ```gateway-service```, which routes each request to the service that owns it and
  issues and verifies the session token itself.
- ```payment-service``` calls ```order-service``` to confirm an order once its payment has succeeded.

**Asynchronous messaging over Kafka is the exception**, kept for the few cases where two microservices are better
decoupled than chained together, so that the publisher neither waits on nor depends on the consumer being up. At the
moment both cases feed ```notification-service```: ```user-service``` publishes an email-confirmation event when a
user registers, and ```payment-service``` publishes a payment-success event. ```notification-service``` receives
messages only through Kafka. The events themselves, and how they fit between the bounded contexts, are described in
the [context map](../01-deliverables/domain-model.md#context-map-communication-between-contexts) of the Domain
Model.
