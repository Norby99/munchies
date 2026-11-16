# Concept

Features marked as _(planned)_ are part of the product scope, but are not available yet. The status of each one is
tracked in the [requirements](../02-requirements/requirements.md).

## Product Type

_Munchies_ is a distributed, cloud-native food ordering and restaurant management platform developed as a microservice-oriented system. The product is composed of two primary layers:

1. **Web Application with GUI (Frontend Service)**:
   - A single-page web application (SPA) built with **Vue.js 3** (using the Composition API, Pinia for client-side state management, and Vue Router).
   - Serves as the primary touchpoint for both end customers (viewing restaurants and menus, placing orders, choosing a delivery or pickup time, tracking fulfillment) and restaurant managers (administrating restaurants, menus and dishes).

2. **Web Services & Distributed Microservices Backend**:
   - A suite of autonomous, independently scalable backend microservices built with **Kotlin + Micronaut** (`user-service`, `restaurant-service`, `order-service`) and **Node.js / Express.js with TypeScript** (`gateway-service`, `payment-service`, `notification-service`). `scheduler-service` and `table-reservation-service` complete the suite _(planned)_.
   - An **API Gateway** (`gateway-service`) exposing unified, contract-first **RESTful APIs** documented using **OpenAPI / Swagger** specifications, managing authentication, session cookies, and request routing.
   - An asynchronous event-driven subsystem using **Apache Kafka** for decoupled, non-blocking notification events (email confirmation, payment success, order status change). Delivering these notifications to users is _(planned)_.
   - Containerized orchestration managed via **Docker**, **Docker Compose**, and **Kubernetes** (with Helm charts).

---

## User Roles

The system recognizes two distinct registered roles with hierarchical access levels:

1. **Customer (`CUSTOMER`)**:
   - Standard end-user of the platform.
   - Can register, authenticate, manage profile credentials, and verify email via one-time keys (OTK).
   - Can browse the closest restaurants _(planned)_, and view restaurant menus, categories, dish items, and variations.
   - Can place orders (Delivery, Takeaway, or Dine-In) choosing when they are fulfilled, review carts, update or cancel pending orders, and follow their status. Following the status is only available through the API, while its view in the interface is _(planned)_.
   - Can pay for orders _(planned)_ and receive notifications _(planned)_.
   - Can reserve tables for dining in _(planned)_.

2. **Manager (`MANAGER`)**:
   - Administrative restaurant owner/operator (a superset of the Customer role).
   - Can create, update, and manage one or more restaurant profiles (name, address, phone, email), including business hours _(planned)_.
   - Can design and organize menus, group items into categories, customize menu item options and prices, and configure complex temporal availability/validity rules (e.g., seasonal, weekly, daily hours).
   - Can list the orders of their restaurants and advance their status across fulfillment cycles. These features are only available through the API, while their views in the interface are _(planned)_.
   - Can inspect kitchen load and manage capacity _(planned)_.

---

## Use Case Collection

### 1. Where are the users?
- **Customers**: Ubiquitous and mobile. They access the platform remotely from their homes, workplaces, or on-the-go while commuting, as well as on-site inside the restaurant (for dine-in table ordering).
- **Managers / Restaurant Staff**: Primarily located on-site at the restaurant establishment (kitchen, front desk, manager office) or working remotely when configuring menus, pricing, and operating logistics.

### 2. When and how frequently do they interact with the system?
- **Customers**:
  - *Frequency*: Periodic and bursty, strongly correlating with peak meal times (lunch: 12:00–14:30, dinner: 18:30–22:00, weekends, and holidays).
  - *Interaction Pattern*: Short, focused sessions ranging from 3 to 10 minutes to browse menus, select item variations, select a delivery/pickup time slot, and check out. This is followed by passive or active status check-ins during order tracking until delivery or pickup completion.
- **Managers**:
  - *Frequency*: Continuous during business hours for operational monitoring (following incoming orders, advancing order states from pending/prep to ready). Periodic or scheduled during off-peak hours for catalog administration (updating dish availability, editing seasonal menus, altering prices and categories).

### 3. How do they interact with the system? Which devices are they using?
- **Interaction Means**:
  - Web browsers communicating with the API Gateway, over HTTPS _(planned)_.
  - Asynchronous alerts delivered via email or pushed through notifications _(planned)_.
- **Devices**:
  - *Customers*: Primarily mobile smartphones and tablets through responsive web viewports, as well as desktop/laptop browsers.
  - *Managers / Staff*: Desktop computers, laptops, and dedicated point-of-sale (POS) / kitchen display tablets.

### 4. Does the system need to store users' data? Which? Where?

Yes, the system stores sensitive operational, domain, and profile data. Following the **Database-per-Service** microservice pattern, each service with persistent state maintains its own isolated database instance:

- **What data is stored?**
  - **User & Authentication Data** (`user-service`): User identifiers (UUID), usernames, email addresses, salted password hashes, email verification statuses, failed login attempts, account lock expiry, and authorization roles (`CUSTOMER`, `MANAGER`).
  - **Catalog & Restaurant Data** (`restaurant-service`): Restaurant profiles (name, address, telephone, email), manager associations, menus, validity schedules (rules for recurring days, hours, and date ranges), categories, dishes/items, and variation configurations with price modifiers.
  - **Orders & Event Streams** (`order-service`): Order aggregate records including order IDs, associated restaurant IDs, customer IDs, fulfillment types (`DELIVERY`, `TAKEAWAY`, `DINE_IN`), destination details (delivery address, bell name, contact phone, or table number and guest count), timestamps (delivery slot, pickup time), selected items with quantities, and lifecycle states (`PENDING`, `PREPARING`, `READY`, `ON_THE_WAY`, `COMPLETED`, `CANCELLED`). *Note: `order-service` persists the `Order` aggregate via an append-only event-sourcing log.*
  - **Scheduling & Capacity Data** (`scheduler-service`) _(planned)_: Slot capacity allocations, bookings, fulfillment schedules, and kitchen load metrics.
  - **Payment Transaction Records** (`payment-service`): Payment identifiers, associated order IDs, currency, payment method, transaction amounts, and payment status (`PENDING`, `COMPLETED`, `FAILED`, `CANCELLED`).
  - **Notifications** (`notification-service`): No persistent data. Events for user notifications (email confirmation, payment success, order status change) are held by the message broker until they are consumed.
  - **Table Reservations** (`table-reservation-service`) _(planned)_: Reservation times, guest counts, and table allocation records.

- **Where is it stored?**
  - Stored in persistent, containerized **MongoDB** database instances provisioned per microservice in isolated namespaces/containers, ensuring loose coupling and independent bounded-context persistence.
  - Transient session authentication is maintained via secure `httpOnly` JWT cookies, kept in the browser and verified at the API Gateway boundary.
  - The cart being composed is kept in the browser, until the order is placed.
