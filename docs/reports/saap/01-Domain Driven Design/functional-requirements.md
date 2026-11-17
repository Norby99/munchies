# Functional Requirements

Functional requirements describe the behaviour the system offers to satisfy the
[Business Requirements](business-requirements.md). They are grouped by bounded context, using the terms defined in the
[Glossary](glossary.md). The **Covers** column links each requirement to the business requirements it serves, and the
**User story** column to the scenario in [User Stories](User%20Stories.md) that specifies it as acceptance criteria,
where one exists. The [Use Cases](use-cases.md) show the same behaviour from the point of view of the actors.

Unless stated otherwise, every requirement applies to a logged-in user. "Customer" also includes managers, who can do
everything a customer can.

## User

| ID   | Requirement                                                                                                            | Covers | User story                            |
|------|------------------------------------------------------------------------------------------------------------------------|--------|---------------------------------------|
| FR-1 | A person can register with a username, an email, a password and a role, customer or manager, and is logged in at once. | -      | User registers to the system          |
| FR-2 | A registered user can log in with their credentials, and log out.                                                      | -      | User logs in to the system            |
| FR-3 | A user confirms ownership of their email address with the key sent to them at registration.                            | BR-10  | User verifies their email address     |
| FR-4 | A user can see their profile and change their username and email; the role cannot be changed.                          | -      | User updates their profile            |
| FR-5 | A user can change their password, giving the current one.                                                              | -      | User changes their password           |
| FR-6 | A user can permanently delete their account.                                                                           | -      | -                                     |
| FR-7 | Restaurant management, order handling and the reservations of a restaurant are reserved to the manager who owns it.    | BR-7   | -                                     |

## Restaurant

| ID    | Requirement                                                                                                                                      | Covers     | User story                                    |
|-------|--------------------------------------------------------------------------------------------------------------------------------------------------|------------|-----------------------------------------------|
| FR-8  | A manager can create a restaurant, identified by name, address, phone and email, which they own.                                                 | BR-7, BR-8 | Manager creates a restaurant                  |
| FR-9  | A manager can update the details of their restaurant and remove it.                                                                              | BR-7       | -                                             |
| FR-10 | A manager can set the position of their restaurant on the map, from which its deliveries start.                                                  | BR-2, BR-7 | Manager configures deliveries                 |
| FR-11 | A manager can create, update and remove the menus of their restaurant, each with an optional validity (e.g. a seasonal menu).                    | BR-7       | Manager builds a menu                         |
| FR-12 | A manager can organise a menu into categories, and define variations on a category that apply to all of its items.                               | BR-4, BR-7 | Manager builds a menu                         |
| FR-13 | A manager can create, update and remove menu items, each with name, description, price, optional variations and optional validity.              | BR-4, BR-7 | Manager builds a menu                         |
| FR-14 | A manager can configure the tables of their restaurant, each with a number and a number of seats.                                                | BR-5, BR-7 | Manager configures the tables                 |
| FR-15 | A manager can set the capacity policy of their restaurant: opening and closing time, slot length, preparation lead time and how many days ahead customers can order. | BR-3, BR-7 | Manager configures the kitchen capacity |
| FR-16 | Within the capacity policy, a manager sets the kitchen output: how many items the kitchen can take on in each slot.                              | BR-9       | Manager configures the kitchen capacity       |
| FR-17 | Within the capacity policy, a manager sets the delivery capacity: how many delivery trips can leave in each slot, how many kilometres a trip may cover and how long a detour a new stop may add. | BR-2, BR-9 | Manager configures deliveries |
| FR-18  | A manager can draw delivery zones on the map; when a restaurant has zones, a delivery joins an existing trip only if all its stops and the new address lie in a common zone. Zones do not limit where the restaurant delivers. | BR-2, BR-9 | - |
| FR-19 | A customer can search the restaurants by name or address, or list all of them.                                                                   | BR-1       | Customer searches for a restaurant            |
| FR-20 | A customer can open a restaurant and see its menus, with their categories, items, variations and prices.                                         | BR-1, BR-4 | Customer browses a restaurant menu            |

## Order and scheduling

| ID    | Requirement                                                                                                                                                         | Covers      | User story                                |
|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|-------------------------------------------|
| FR-21 | A customer can see, for a given day, the time slots of a restaurant that can still be booked, with how much capacity each one has left.                              | BR-3        | Customer chooses a time slot              |
| FR-22 | A customer can place an order for a set of items from one restaurant, choosing the quantity of each item and a time slot; each item is priced from the menu when the order is placed. | BR-1, BR-3 | Customer places a delivery order     |
| FR-23 | When placing an order, the customer chooses how it is fulfilled: delivery to an address located on the map, takeaway under a name, or dine-in at a table.            | BR-2        | Customer places a delivery order          |
| FR-24 | An order is accepted only in a slot that is within opening hours, at least the preparation lead time ahead, within the booking window, and that has kitchen capacity left for its items; otherwise it is refused and the customer chooses another slot. | BR-9 | Customer cannot order in a full slot |
| FR-25 | A delivery order is accepted only if it fits a trip of its slot: it joins an existing trip within the allowed detour and kilometres, or starts a new trip if the slot has trips left; otherwise it is refused. | BR-2, BR-9 | - |
| FR-26 | While an order is pending, the customer can change its items, change its delivery or takeaway details, or cancel it.                                                | BR-1        | Customer cancels a pending order          |
| FR-27 | An order moves through `PENDING → PREPARING → READY → COMPLETED`; delivery orders pass through `ON_THE_WAY` between `READY` and `COMPLETED`. Only a pending order can be cancelled. | BR-2 | - |
| FR-28 | The system moves an order to `PREPARING` when its preparation should start: the start of its slot minus the preparation lead time of the restaurant.                | BR-9, BR-12 | Preparation starts automatically          |
| FR-29 | A manager can see the orders of their restaurants and move each one to its next status.                                                                             | BR-12       | Manager advances an order                 |
| FR-30 | A manager can see the load of each slot of a day: the capacity booked against the capacity available.                                                               | BR-9, BR-12 | Manager checks the kitchen load           |
| FR-31 | A customer can list their orders and follow the status of each one.                                                                                                | BR-10       | Customer tracks the order status          |
| FR-32 | While a delivery order is on the way, the customer can follow the position of its driver on a map.                                                                  | BR-10       | Customer follows the driver               |

## Payment

| ID    | Requirement                                                                                                                                  | Covers | User story                     |
|-------|----------------------------------------------------------------------------------------------------------------------------------------------|--------|--------------------------------|
| FR-33 | A customer can pay an order they placed, choosing the payment method; the amount is the order total computed from the menu prices.            | BR-6   | Customer pays for an order     |
| FR-34 | When the payment completes, the order is marked as paid.                                                                                     | BR-6   | Customer pays for an order     |
| FR-35 | A payment is refused, and nothing is charged, for an order of another customer or an order already paid; a rejected payment leaves the order unpaid. | BR-6 | Customer cannot pay an order twice |

## Table reservation

| ID    | Requirement                                                                                                                                         | Covers      | User story                              |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------|-------------|-----------------------------------------|
| FR-36 | A customer can reserve a table at a restaurant for an arrival time on a 15-minute grid and a number of guests; the system assigns a free table with enough seats for a fixed duration. | BR-5 | Customer reserves a table |
| FR-37 | A reservation is refused when no table of the restaurant can seat the party, or when every such table is taken at that time.                        | BR-5        | Customer cannot reserve a full restaurant |
| FR-38 | A customer can change the time or the number of guests of their reservation, or cancel it.                                                          | BR-5        | Customer modifies a reservation          |
| FR-39 | A customer can list their reservations.                                                                                                             | BR-5        | -                                       |
| FR-40 | A manager can list the reservations of their restaurants.                                                                                           | BR-5, BR-12 | Manager sees the reservations           |

## Notification

| ID    | Requirement                                                                                     | Covers | User story                                 |
|-------|-------------------------------------------------------------------------------------------------|--------|--------------------------------------------|
| FR-41 | When a user registers, they are notified with the key to confirm their email address.            | BR-10  | User verifies their email address          |
| FR-42 | When a payment completes, the customer is notified.                                             | BR-10  | Customer is notified of a successful payment |
| FR-43 | When the status of an order changes, the customer is notified.                                  | BR-10  | -                                          |
| FR-44 | When a reservation is accepted, modified or cancelled, the customer is notified.                | BR-10  | -                                          |

## Coverage

Every business requirement except BR-11, which is a quality driver, is covered by at least one functional requirement:

| Business requirement | Functional requirements                  |
|----------------------|------------------------------------------|
| BR-1                 | FR-19, FR-20, FR-22, FR-26               |
| BR-2                 | FR-10, FR-17, FR-18, FR-23, FR-25, FR-27 |
| BR-3                 | FR-15, FR-21, FR-22                      |
| BR-4                 | FR-12, FR-13, FR-20                      |
| BR-5                 | FR-14, FR-36 to FR-40                    |
| BR-6                 | FR-33, FR-34, FR-35                      |
| BR-7                 | FR-7 to FR-15                            |
| BR-8                 | FR-8                                     |
| BR-9                 | FR-16, FR-17, FR-18, FR-24, FR-25, FR-28, FR-30 |
| BR-10                | FR-3, FR-31, FR-32, FR-41 to FR-44       |
| BR-11                | See [Non-functional Requirements](non-functional-requirements.md) |
| BR-12                | FR-28, FR-29, FR-30, FR-40               |
