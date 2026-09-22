# Glossary

The first step of knowledge crunching is agreeing on a shared, unambiguous vocabulary for the domain: the ubiquitous language every team member, and every class/field in the code, uses for the same concept. This glossary lists the terms identified for Munchies, first as a flat list of concepts and actions, then regrouped per bounded context.

## Global Concepts

| Term | Definition                                                                                                                                                                                  |
| --- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| System | The Munchies platform as a whole                                                                                                                                                            |
| User | A person using the system that is registered and can be a customer or a manager                                                                                                             |
| User Profile | User's informations like username, email or role                                                                                                                                            |
| Customer | A user who browses restaurants, orders food and books tables                                                                                                                                |
| Manager | A user who creates and administers one or more restaurants. It can do all customer's actions                                                                                                |
| Restaurant | An establishment, owned by exactly one manager, identified by name, address, phone and email                                                                                                |
| Menu | A named, orderable listing of categories and items belonging to one restaurant                                                                                                              |
| Category | A named grouping of menu items inside a menu. It can have variations appliable to all sub items by customers                                                                                |
| Menu Item | A single orderable dish: name, description, price, and optional variations                                                                                                                  |
| Variation | A customization option with a price appliable on a menu item (e.g. size, spice level). <br/>It can be configured on a category or menu item by the manager and can be applied by a customer |
| Validity | The time window during which a menu or menu item is orderable (e.g. seasonal menus or menu items)                                                                                           |
| Order | A customer's request to buy a set of items from one restaurant, fulfilled by delivery, takeaway or dine-in                                                                                  |
| Order Item | A menu item and the quantity of it requested in an order                                                                                                                                    |
| Order Status | The order's position in its fulfilment lifecycle                                                                                                                                            |
| Payment | The record of money changing hands for one order                                                                                                                                            |
| Table Reservation | A customer's booking of a restaurant table for a given time                                                                                                                                 |
| Notification | A message describing something that happened in another bounded context                                                                                                                     |
## Actions

### User

| Term | Definition |
| --- | --- |
| Registering | The action performed by a person to create an account and become a user |
| Logging in | The action performed by a user to authenticate and enter the system |
| Verifying an email | The action performed by a user to confirm ownership of the email address on their profile |
| Updating a user profile | The action performed by a user to change their username, email or password |
| Deleting an account | The action performed by a user to permanently remove their account |

### Restaurant

| Term | Definition |
| --- | --- |
| Creating a restaurant | The action performed by a manager to register a new restaurant under their account |
| Updating restaurant details | The action performed by a manager to change their restaurant's name, address, phone or email |
| Removing a restaurant | The action performed by a manager to permanently remove a restaurant |
| Creating a menu | The action performed by a manager to add a new menu to their restaurant |
| Updating a menu | The action performed by a manager to change a menu's name or validity |
| Removing a menu | The action performed by a manager to remove a menu from their restaurant |
| Creating a category | The action performed by a manager to add a category to one of their menus |
| Updating a category | The action performed by a manager to change a category's name or variations |
| Removing a category | The action performed by a manager to remove a category from a menu |
| Creating a menu item | The action performed by a manager to add an orderable item to a category |
| Updating a menu item | The action performed by a manager to change a menu item's details, price, variations or validity |
| Removing a menu item | The action performed by a manager to remove an item from a category |

### Order

| Term | Definition |
| --- | --- |
| Placing an order | The action performed by a customer to submit a new order to a restaurant |
| Updating order items | The action performed by a customer to change the items of a pending order |
| Advancing order status | The action performed by the system to move an order to the next stage of its lifecycle |
| Paying an order | The action performed by a customer to mark an order as paid |
| Cancelling an order | The action performed by a customer to cancel a still-pending order |

### Payment

| Term | Definition |
| --- | --- |
| Processing a payment | The action performed by the system to charge a customer for an order |
| Completing a payment | The action performed by the system when a payment succeeds |
| Failing a payment | The action performed by the system when a payment cannot be completed |

## Bounded-Context Glossary

### User Bounded Context

| Term | Definition |
| --- | --- |
| User | A person who is registered and uses the system |
| Customer | The default role of a registered user |
| Manager | A user with restaurant-administration privileges, superset of Customer |
| User Profile | Username, email and role of a user |
| Registering | Creating an account |
| Logging in | Authenticating to enter the system |
| Verifying an email | Confirming ownership of the profile's email address |
| Updating a user profile | Changing username, email or password |
| Deleting an account | Permanently removing an account |

### Restaurant Bounded Context

| Term | Definition |
| --- | --- |
| Restaurant | An establishment owned by one manager |
| Menu | A named listing of categories and items for one restaurant |
| Category | A grouping of menu items inside a menu |
| Menu Item | A single orderable dish, with price and variations |
| Variation | A customization option on an item or category |
| Validity | The time window in which a menu or item is orderable |
| Creating a restaurant | Registering a new restaurant |
| Updating restaurant details | Changing a restaurant's name, address, phone or email |
| Removing a restaurant | Permanently removing a restaurant |
| Creating a menu | Adding a new menu to a restaurant |
| Updating a menu | Changing a menu's name or validity |
| Removing a menu | Removing a menu from a restaurant |
| Creating a category | Adding a category to a menu |
| Updating a category | Changing a category's name or variations |
| Removing a category | Removing a category from a menu |
| Creating a menu item | Adding an orderable item to a category |
| Updating a menu item | Changing a menu item's details, price, variations or validity |
| Removing a menu item | Removing an item from a category |

### Order Bounded Context

| Term | Definition |
| --- | --- |
| Order | A request to buy items from one restaurant |
| Delivery Order | An order fulfilled by delivery to an address |
| Takeaway Order | An order the customer collects in person |
| Dine-in Order | An order placed at a table inside the restaurant |
| Order Item | A menu item and its requested quantity |
| Order Status | `PENDING → PREPARING → READY → COMPLETED` (delivery orders pass through `ON_THE_WAY` between `READY` and `COMPLETED`), or `CANCELLED` (from `PENDING` only) |
| Placing an order | Submitting a new order |
| Updating order items | Changing a pending order's items |
| Advancing order status | Moving an order to its next lifecycle stage |
| Paying an order | Marking an order as paid |
| Cancelling an order | Cancelling a still-pending order |

### Payment Bounded Context

| Term | Definition |
| --- | --- |
| Payment | The record of money changing hands for one order |
| Payment Method | How the payment was made (e.g. card) |
| Payment Status | `PENDING → COMPLETED`/`FAILED`/`CANCELLED` |
| Processing a payment | Charging a customer for an order |
| Completing/Failing/Cancelling a payment | A payment's terminal outcomes |

### Notification Bounded Context

| Term | Definition |
| --- | --- |
| Notification | An asynchronous message describing something that happened in another bounded context |
| Email Confirmation Notification | The notification sent when a user registers, prompting email verification |
| Payment Success Notification | The notification sent when a payment for an order completes |

### Table Reservation Bounded Context

| Term | Definition |
| --- | --- |
| Table Reservation | A customer's booking of a restaurant table for a given time |

*Present in the codebase but incomplete; see [Microservices](../02-implementation/microservices.md).*

See [Domain Model](domain-model.md) for how these concepts are actually implemented as entities, value objects and aggregates, and how the bounded contexts above integrate with each other.
