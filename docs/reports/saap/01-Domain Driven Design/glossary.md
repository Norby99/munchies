# Glossary

The first step of knowledge crunching is agreeing on a shared vocabulary for the domain: the ubiquitous
language every team member uses for the same concept.

## Global Concepts

| Term              | Definition                                                                                                         |
|-------------------|--------------------------------------------------------------------------------------------------------------------|
| System            | The Munchies platform as a whole                                                                                   |
| User              | A registered person using the system, as a customer or as a manager                                                |
| User Profile      | The username, email and role of a user                                                                             |
| Customer          | A user who browses restaurants, orders food and reserves tables                                                    |
| Manager           | A user who creates and runs one or more restaurants. A manager can do everything a customer can                    |
| Restaurant        | An establishment owned by exactly one manager, identified by name, address, phone and email                        |
| Menu              | A named listing of categories and items belonging to one restaurant                                                |
| Category          | A named grouping of menu items inside a menu, which can define variations that apply to all of its items           |
| Menu Item         | A single orderable dish: name, description, price and optional variations                                          |
| Variation         | A customisation option with a price (e.g. size, spice level), defined by the manager on a category or a menu item. Orders do not carry variations yet |
| Validity          | The time window during which a menu or a menu item is offered (e.g. a seasonal menu)                               |
| Table             | A table of a restaurant, with a number and a number of seats                                                       |
| Capacity Policy   | The settings that decide how many orders a restaurant can take: opening hours, slot length, preparation lead time, booking window, kitchen output and delivery capacity |
| Time Slot         | A fixed interval of the day (e.g. 19:00-19:15) in which an order is due; every order is booked in exactly one slot  |
| Booking           | The reservation of kitchen capacity, and for a delivery of a place on a trip, made for an order in its time slot   |
| Order             | A customer's request to buy a set of items from one restaurant, fulfilled by delivery, takeaway or dine-in         |
| Order Item        | A menu item and its quantity, requested in an order, at the menu price of the moment the order is placed         |
| Order Status      | The position of an order in its lifecycle                                                                          |
| Delivery Trip     | A run of one driver that leaves the restaurant, serves the delivery orders of one slot in order and comes back     |
| Driver            | The person who carries a delivery trip; their position is shown to the customer while the order is on the way      |
| Payment           | The record of money changing hands for one order                                                                   |
| Table Reservation | A customer's booking of a table of a restaurant, for an arrival time and a number of guests                       |
| Notification      | A message that tells a user about something that happened in another bounded context                             |

## Actions

### User

| Term                     | Definition                                                                                 |
|--------------------------|--------------------------------------------------------------------------------------------|
| Registering              | The action performed by a person to create an account, choosing the role, and become a user |
| Logging in / out         | The action performed by a user to open or close their session                              |
| Verifying an email       | The action performed by a user to confirm ownership of the email address on their profile  |
| Updating a user profile  | The action performed by a user to change their username or email                           |
| Changing the password    | The action performed by a user to replace their password, giving the current one           |
| Deleting an account      | The action performed by a user to permanently remove their account                         |

### Restaurant

| Term                             | Definition                                                                                         |
|----------------------------------|----------------------------------------------------------------------------------------------------|
| Creating a restaurant            | The action performed by a manager to register a new restaurant under their account                 |
| Updating restaurant details      | The action performed by a manager to change their restaurant's name, address, phone or email       |
| Removing a restaurant            | The action performed by a manager to permanently remove a restaurant                               |
| Setting the location             | The action performed by a manager to place their restaurant on the map, where its deliveries start |
| Creating, updating, removing a menu | The actions performed by a manager on the menus of their restaurant, including their validity  |
| Creating, updating, removing a category | The actions performed by a manager on the categories of a menu, including their variations  |
| Creating, updating, removing a menu item | The actions performed by a manager on the items of a category: details, price, variations and validity |
| Configuring the tables           | The action performed by a manager to set the tables of their restaurant and their seats            |
| Configuring the capacity policy  | The action performed by a manager to set how many orders their restaurant can take, and when       |
| Searching restaurants            | The action performed by a customer to find restaurants by name or address                          |

### Order

| Term                          | Definition                                                                                      |
|-------------------------------|-------------------------------------------------------------------------------------------------|
| Checking availability         | The action performed by a customer to see the time slots of a day that can still be booked      |
| Placing an order              | The action performed by a customer to submit a new order to a restaurant, in a time slot        |
| Updating an order             | The action performed by a customer to change the items, or the delivery or takeaway details, of a pending order |
| Cancelling an order           | The action performed by a customer to cancel a still-pending order                              |
| Starting the preparation      | The action performed by the system to move an order to preparation when its slot approaches     |
| Advancing order status        | The action performed by a manager to move an order to the next stage of its lifecycle           |
| Tracking an order             | The action performed by a customer to follow the status of their order and, on the way, its driver |

### Payment

| Term                 | Definition                                                                    |
|----------------------|-------------------------------------------------------------------------------|
| Paying an order      | The action performed by a customer to pay the total of one of their orders    |
| Processing a payment | The action performed by the system to charge the customer for an order        |
| Completing a payment | The action performed by the system when a payment succeeds                    |
| Failing a payment    | The action performed by the system when a payment cannot be completed         |

### Table Reservation

| Term                   | Definition                                                                              |
|------------------------|-----------------------------------------------------------------------------------------|
| Reserving a table      | The action performed by a customer to book a table for an arrival time and a party      |
| Modifying a reservation | The action performed by a customer to change the time or the number of guests          |
| Cancelling a reservation | The action performed by a customer to release their table                             |

## Bounded-Context Glossary

### User Bounded Context

| Term                    | Definition                                                             |
|-------------------------|------------------------------------------------------------------------|
| User                    | A person who is registered and uses the system                         |
| Role                    | `CUSTOMER` or `MANAGER`, chosen at registration and never changed      |
| Customer                | A user with the customer role                                          |
| Manager                 | A user with restaurant-administration privileges, superset of Customer |
| User Profile            | Username, email and role of a user                                     |
| Credentials             | The password hash of a user, kept apart from the profile               |
| Confirmation Key        | The key sent at registration to verify the email address              |
| Registering             | Creating an account                                                    |
| Logging in              | Authenticating to enter the system                                     |
| Verifying an email      | Confirming ownership of the profile's email address                    |
| Updating a user profile | Changing username or email                                             |
| Changing the password   | Replacing the password, giving the current one                         |
| Deleting an account     | Permanently removing an account                                        |

### Restaurant Bounded Context

| Term                            | Definition                                                                         |
|---------------------------------|------------------------------------------------------------------------------------|
| Restaurant                      | An establishment owned by one manager                                              |
| Location                        | The position of the restaurant on the map, the origin of its delivery trips        |
| Menu                            | A named listing of categories and items for one restaurant                         |
| Category                        | A grouping of menu items inside a menu                                             |
| Menu Item                       | A single orderable dish, with price and variations                                 |
| Variation                       | A customisation option on an item or category                                      |
| Validity                        | The time window in which a menu or item is offered                                 |
| Table                           | A table of the restaurant, with its number and seats                               |
| Capacity Policy                 | The restaurant's limits on orders, read by the Schedule context                    |
| Opening Hours                   | The opening and closing time within which slots are offered                        |
| Slot Length                     | The duration of a time slot, in minutes                                            |
| Preparation Lead Time           | How long before its slot an order enters the kitchen                               |
| Booking Window                  | How many days ahead customers can order                                            |
| Kitchen Output                  | How many items the kitchen can take on in each slot                                |
| Delivery Capacity               | How many trips can leave per slot, how far a trip may go and how long a detour a new stop may add |
| Creating a restaurant           | Registering a new restaurant                                                       |
| Updating restaurant details     | Changing a restaurant's name, address, phone or email                              |
| Removing a restaurant           | Permanently removing a restaurant                                                  |
| Managing menus, categories and items | Creating, updating and removing them                                          |
| Configuring the tables          | Setting the tables of the restaurant                                               |
| Configuring the capacity policy | Setting the limits above                                                           |
| Searching restaurants           | Finding restaurants by name or address                                             |

### Order Bounded Context

| Term                   | Definition                                                                                                                                                  |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Order                  | A request to buy items from one restaurant                                                                                                                  |
| Delivery Order         | An order delivered to an address                                                                                                                            |
| Takeaway Order         | An order the customer collects in person, under a name                                                                                                      |
| Dine-in Order          | An order served at a table inside the restaurant                                                                                                            |
| Order Item             | A menu item and its quantity, with the unit price frozen when the order is placed                                                                          |
| Order Total            | The amount of an order, priced from the restaurant's menus when the order is placed                                                                        |
| Order Status           | `PENDING → PREPARING → READY → COMPLETED` (delivery orders pass through `ON_THE_WAY` between `READY` and `COMPLETED`), or `CANCELLED` (from `PENDING` only) |
| Order Event            | A change of an order (placed, items updated, paid, status advanced, cancelled), stored in the order's history                                              |
| Placing an order       | Submitting a new order                                                                                                                                      |
| Updating an order      | Changing a pending order's items or its delivery or takeaway details                                                                                       |
| Advancing order status | Moving an order to its next lifecycle stage                                                                                                                 |
| Paying an order        | Marking an order as paid                                                                                                                                    |
| Cancelling an order    | Cancelling a still-pending order                                                                                                                            |

### Schedule Bounded Context

| Term                  | Definition                                                                                          |
|-----------------------|-----------------------------------------------------------------------------------------------------|
| Time Slot             | An interval of the day, as long as the slot length, in which an order is due                         |
| Available Slot        | A slot that is within opening hours and the booking window, at least the preparation lead time ahead, with capacity left |
| Capacity Unit         | The measure of kitchen work: one item of an order takes one unit of its slot                        |
| Booking               | The capacity an order takes in its slot, and its place on a delivery trip                          |
| Booking Status        | `CONFIRMED` (order placed, waiting for preparation), `PREPARING`, `CANCELLED`, or `FAILED` (the order could not be placed, its capacity is released) |
| Kitchen Load          | The capacity booked in each slot of a day, against the capacity available                          |
| Admission Quota       | The kitchen output of a slot as tuned by the capacity agent, within the manager's bounds            |
| Checking availability | Listing the available slots of a day                                                                |
| Scheduling an order   | Booking capacity in a slot, planning the delivery trip, and placing the order                       |
| Starting preparation  | Moving an order to `PREPARING` at the start of its slot minus the preparation lead time             |

### Delivery Bounded Context

| Term          | Definition                                                                                                  |
|---------------|-------------------------------------------------------------------------------------------------------------|
| Delivery Trip | A run from the restaurant through the stops of one slot and back, carried by one driver                      |
| Stop          | The address of one delivery order on a trip                                                                 |
| Detour        | The extra kilometres a new stop adds to a trip; a stop that adds too much starts a new trip instead          |
| Route         | The path of a trip along real streets, fixed when the driver leaves                                         |
| Departure     | The moment the driver leaves, with the first order of the trip that goes `ON_THE_WAY`                       |
| Driver Position | Where the driver is along the route; simulated, since no real driver device reports it                  |
| Tracking      | Showing the customer the driver position of their order while it is on the way                            |

### Payment Bounded Context

| Term                                    | Definition                                                    |
|-----------------------------------------|---------------------------------------------------------------|
| Payment                                 | The record of money changing hands for one order              |
| Payment Method                          | How the customer pays: card, cash, mobile payment, bank transfer or other |
| Payment Status                          | `PENDING → COMPLETED`/`FAILED`/`CANCELLED`                    |
| Payment Gateway                         | The external provider that charges the customer; simulated in the current system |
| Processing a payment                    | Charging a customer for an order                              |
| Completing/Failing/Cancelling a payment | A payment's terminal outcomes                                 |

### Table Reservation Bounded Context

| Term                     | Definition                                                                                 |
|--------------------------|--------------------------------------------------------------------------------------------|
| Table Reservation        | A customer's booking of a table for an arrival time and a number of guests                 |
| Arrival Time             | When the party arrives; always on a 15-minute grid (19:00, 19:15, ...)                     |
| Reservation Duration     | How long a table stays booked; the same for every reservation, set in the configuration    |
| Party                    | The number of guests of a reservation                                                      |
| Dining Room              | The tables of a restaurant, read from the Restaurant context                               |
| Free Table               | A table with enough seats for the party and no other reservation at that time              |
| Reservation Status       | `REQUESTED → ACCEPTED`, then possibly `CANCELED`; only an accepted reservation holds a table |
| Reserving a table        | Booking a free table for a party                                                           |
| Modifying a reservation  | Changing the time or the party, keeping the table if it still fits                         |
| Cancelling a reservation | Releasing the table                                                                        |

### Notification Bounded Context

| Term                                | Definition                                                                              |
|-------------------------------------|-----------------------------------------------------------------------------------------|
| Notification                        | An asynchronous message describing something that happened in another bounded context   |
| Email Confirmation Notification     | Sent when a user registers, with the key to verify the email address                    |
| Payment Success Notification        | Sent when the payment of an order completes                                             |
| Order Status Changed Notification   | Sent when an order moves to a new status                                                |
| Table Reservation Notification      | Sent when a reservation is accepted, modified or cancelled                              |

See [Domain Model](domain-model.md) for how these concepts are actually implemented as entities, value objects and
aggregates, and how the bounded contexts above integrate with each other.
