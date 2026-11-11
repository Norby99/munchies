# Functional Requirements

Functional requirements describe the behaviour the system offers to satisfy the
[Business Requirements](business-requirements.md). They are grouped by bounded context, using the terms defined in the
[Glossary](glossary.md). The **Covers** column links each requirement to the business requirements it serves, and the
**User story** column to the scenario in [User Stories](User%20Stories.md) that specifies it as acceptance criteria,
where one exists.

Unless stated otherwise, every requirement applies to a logged-in user. "Customer" also includes managers, who can do
everything a customer can.

## User

| ID   | Requirement                                                                                                           | Covers | User story                          |
|------|-----------------------------------------------------------------------------------------------------------------------|--------|-------------------------------------|
| FR-1 | A person can register with a username, an email and a password, and receives a personal authentication token.         | -      | User registers to the system        |
| FR-2 | A registered user can log in with their credentials and receives a personal authentication token.                     | -      | User logs in to the system          |
| FR-3 | A user confirms ownership of their email address through the confirmation sent at registration.                       | BR-10  | -                                   |
| FR-4 | A user can update their username, email and password; any other field of the profile cannot be changed by the user.   | -      | User updates its profile            |
| FR-5 | A user can permanently delete their account.                                                                          | -      | -                                   |
| FR-6 | Every user has a role, customer or manager. Restaurant management is reserved to managers.                           | BR-7   | -                                   |

## Restaurant

| ID    | Requirement                                                                                                                       | Covers     | User story                                      |
|-------|-----------------------------------------------------------------------------------------------------------------------------------|------------|-------------------------------------------------|
| FR-7  | A manager can create a restaurant, identified by name, address, phone and email, which they own.                                  | BR-7, BR-8 | -                                               |
| FR-8  | A manager can update the details of their restaurant and remove it.                                                               | BR-7       | -                                               |
| FR-9  | A manager can set the business hours of their restaurant.                                                                         | BR-7       | -                                               |
| FR-10 | A manager can create, update and remove the menus of their restaurant, each with an optional validity (e.g. a seasonal menu).     | BR-7       | -                                               |
| FR-11 | A manager can organize a menu into categories, and define variations on a category that apply to all of its items.                | BR-4, BR-7 | -                                               |
| FR-12 | A manager can create, update and remove menu items, each with name, description, price, optional variations and optional validity. | BR-4, BR-7 | -                                               |
| FR-13 | A manager can mark a dish as unavailable, and available again; an unavailable dish cannot be ordered.                             | BR-7       | Restaurant staff marks a dish as unavailable    |
| FR-14 | A customer can find the restaurants closest to a given location.                                                                  | BR-1       | -                                               |
| FR-15 | A customer can open a restaurant's menu and see its dishes, with their details, variations and availability.                      | BR-1, BR-4 | Client browses a restaurant menu                |

## Order and scheduling

| ID    | Requirement                                                                                                                                         | Covers     | User story                  |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------|------------|-----------------------------|
| FR-16 | A customer can place an order for a set of items from one restaurant, choosing the quantity and the variations of each item.                         | BR-1, BR-4 | Client places a delivery order |
| FR-17 | When placing an order, the customer chooses how it is fulfilled: delivery to an address, takeaway, or dine-in.                                       | BR-2       | Client places a delivery order |
| FR-18 | When placing an order, the customer chooses when it should be ready or delivered, among the time slots the restaurant still has available.           | BR-3       | -                           |
| FR-19 | The system offers a time slot only if the restaurant is open and the kitchen still has capacity for it.                                             | BR-9       | -                           |
| FR-20 | A customer can change the items of an order, and cancel it, while it is still pending.                                                              | BR-1       | -                           |
| FR-21 | An order moves through `PENDING → PREPARING → READY → COMPLETED`; delivery orders pass through `ON_THE_WAY` between `READY` and `COMPLETED`. Only a pending order can be cancelled. | BR-2 | - |
| FR-22 | A customer can follow the status of their order as it advances.                                                                                     | BR-10      | Client tracks the order status |

## Payment

| ID    | Requirement                                                                                          | Covers | User story              |
|-------|------------------------------------------------------------------------------------------------------|--------|-------------------------|
| FR-23 | A customer can pay for an order they placed.                                                         | BR-6   | Client pays for an order |
| FR-24 | When the payment completes, the order is marked as paid and confirmed.                               | BR-6   | Client pays for an order |
| FR-25 | When the payment fails or is cancelled, the order stays unpaid and is not confirmed.                 | BR-6   | -                       |

## Notification

| ID    | Requirement                                                                                     | Covers | User story                                     |
|-------|-------------------------------------------------------------------------------------------------|--------|------------------------------------------------|
| FR-26 | When a user registers, they receive a notification asking them to confirm their email address.   | BR-10  | -                                              |
| FR-27 | When a payment completes, the customer receives a payment confirmation notification.             | BR-10  | Client is notified of a successful payment     |
| FR-28 | When the status of an order changes, the customer receives a notification.                       | BR-10  | -                                              |

## Table reservation

| ID    | Requirement                                                                            | Covers     | User story              |
|-------|----------------------------------------------------------------------------------------|------------|-------------------------|
| FR-29 | A customer can reserve a table at a restaurant for a given time, if a table is free.   | BR-5       | Client reserves a table |

## Coverage

Every business requirement except BR-11, which is a quality driver, is covered by at least one functional requirement:

| Business requirement | Functional requirements          |
|----------------------|----------------------------------|
| BR-1                 | FR-14, FR-15, FR-16, FR-20       |
| BR-2                 | FR-17, FR-21                     |
| BR-3                 | FR-18                            |
| BR-4                 | FR-11, FR-12, FR-15, FR-16       |
| BR-5                 | FR-29                            |
| BR-6                 | FR-23, FR-24, FR-25              |
| BR-7                 | FR-6 to FR-13                    |
| BR-8                 | FR-7                             |
| BR-9                 | FR-19                            |
| BR-10                | FR-3, FR-22, FR-26, FR-27, FR-28 |
| BR-11                | See [Non-functional Requirements](non-functional-requirements.md) |
