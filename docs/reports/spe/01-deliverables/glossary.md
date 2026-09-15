# Glossary

This glossary establishes the ubiquitous language used throughout the project and this report. It first lists the
domain concepts shared across the whole system, then the actions that can be performed on them, grouped by domain
area, and finally a glossary specific to each bounded context (microservice), listing the terms and entities that
belong to it.

## Global Concepts

| Term              | Definition                                                                                    |
|-------------------|-----------------------------------------------------------------------------------------------|
| User              | A person registered in the system, either a Customer or a Manager.                            |
| Customer          | A User who browses restaurants, places Orders, pays for them and books Table Reservations.    |
| Manager           | A User who administers a Restaurant: its details, its Menu, and the Orders placed against it. |
| Restaurant        | A food establishment registered in the system, owned by a Manager, offering a Menu.           |
| Menu              | The set of Categories and Menu Items a Restaurant offers to Customers.                        |
| Category          | A named grouping of Menu Items within a Menu (e.g. "Starters", "Desserts").                   |
| Menu Item (Dish)  | An individual item a Restaurant offers, with a name, description, price and availability.     |
| Order             | A Customer's request to purchase one or more Menu Items from a Restaurant.                    |
| Delivery Order    | An Order to be delivered to the Customer's address.                                           |
| Takeaway Order    | An Order the Customer will collect in person from the Restaurant.                             |
| Dine-in Order     | An Order placed for consumption at a table inside the Restaurant.                             |
| Payment           | A monetary transaction charged against an Order.                                              |
| Delivery          | The logistics process of getting a Delivery Order from the Restaurant to the Customer.        |
| Table Reservation | A Customer's booking of a table at a Restaurant for a given date, time and party size.        |
| Notification      | A message sent to a User in reaction to something that happened elsewhere in the system.      |

## Actions

### Users

| Term                          | Definition                                                                                |
|-------------------------------|-------------------------------------------------------------------------------------------|
| Registering                   | The action of creating a new User account with a username, an email address and a role.   |
| Logging in                    | The action of authenticating with an email/username and password to access the system.    |
| Verifying an email address    | The action of confirming ownership of the email address associated with a User's account. |
| Updating a User's information | The action of changing a User's profile details (e.g. username).                          |
| Updating a User's password    | The action of changing the password associated with a User's credentials.                 |
| Deleting an account           | The action of permanently removing a User and their credentials from the system.          |

### Restaurants

| Term                               | Definition                                                                                   |
|------------------------------------|----------------------------------------------------------------------------------------------|
| Creating a Restaurant              | The action performed by a Manager to register a new Restaurant.                              |
| Updating a Restaurant              | The action of changing a Restaurant's name, address, phone number or email.                  |
| Managing a Menu                    | The action of creating, updating or removing a Menu's Categories, Menu Items and Variations. |
| Setting a Menu Item's availability | The action of defining the time window during which a Menu Item can be ordered.              |

### Orders

| Term                                      | Definition                                                                                 |
|-------------------------------------------|--------------------------------------------------------------------------------------------|
| Placing an Order                          | The action performed by a Customer to create a new Delivery, Takeaway or Dine-in Order.    |
| Updating an Order's items                 | The action of changing the Menu Items and quantities on an Order.                          |
| Updating a Delivery/Takeaway Order's info | The action of changing type-specific details (e.g. delivery address, pickup time).         |
| Advancing an Order's status               | The action of moving an Order forward to the next state in its lifecycle.                  |
| Paying an Order                           | The action of marking an Order as paid, once its Payment has completed.                    |
| Discarding an Order                       | The action of cancelling an Order that has not yet started being prepared.                 |
| Retrieving Order details                  | The action of fetching a single Order, or the list of a Customer's or Restaurant's Orders. |

### Payments

| Term                               | Definition                                                                                          |
|------------------------------------|-----------------------------------------------------------------------------------------------------|
| Processing a Payment               | The action of validating and charging a Payment for an Order through a payment gateway.             |
| Confirming an Order as paid        | The action of notifying order-service that the Order associated with a completed Payment was paid.  |
| Publishing a payment-success event | The action of emitting the Kafka event that lets notification-service react to a completed Payment. |

### Scheduling & Delivery

| Term                  | Definition                                                                              |
|-----------------------|-----------------------------------------------------------------------------------------|
| Scheduling a delivery | The action of assigning delivery logistics to a Delivery Order once it has been placed. |

### Table Reservations

| Term              | Definition                                                          |
|-------------------|---------------------------------------------------------------------|
| Reserving a table | The action performed by a Customer to book a table at a Restaurant. |

### Notifications

| Term                   | Definition                                                                 |
|------------------------|----------------------------------------------------------------------------|
| Sending a notification | The action of delivering a message to a User in reaction to a Kafka event. |

## Bounded-Contexts Glossary

### User Bounded-Context

| Term             | Definition                                                                                      |
|------------------|-------------------------------------------------------------------------------------------------|
| User             | Aggregate identifying a person registered in the system by a User Id and a User Profile.        |
| User Profile     | Value object holding a User's username, email address and role.                                 |
| User Credentials | Entity holding a User's password hash, salt, failed login-attempt count and lock-out timestamp. |
| User Role        | Enumeration of a User's permission level: ```CUSTOMER``` or ```MANAGER```.                      |
| Email            | Value object wrapping an email address together with whether it has been verified.              |
| Account Lockout  | Temporary suspension of login attempts after three consecutive authentication failures.         |

### Restaurant Bounded-Context

| Term       | Definition                                                                                                   |
|------------|--------------------------------------------------------------------------------------------------------------|
| Restaurant | Aggregate root representing a food establishment, owned by a Manager, with a name, address, phone and email. |
| Menu       | Aggregate grouping the Categories and Menu Items a Restaurant offers.                                        |
| Category   | Entity grouping a set of Menu Items under a common name within a Menu.                                       |
| Menu Item  | Entity representing an orderable dish, with a name, description, price and Validity.                         |
| Variation  | Value object representing an optional customization of a Menu Item (e.g. size, topping).                     |
| Validity   | Value object describing the time window during which a Menu Item can be ordered.                             |
| Money      | Value object representing a monetary amount and its currency.                                                |

### Order Bounded-Context

| Term           | Definition                                                                                                                                                  |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Order          | Sealed aggregate representing a Customer's purchase request against a Restaurant.                                                                           |
| Delivery Order | ```Order``` subtype carrying delivery information (address, estimated time, bell name, phone).                                                              |
| Takeaway Order | ```Order``` subtype carrying pickup information (pickup time, customer name).                                                                               |
| Dine-in Order  | ```Order``` subtype carrying table information (table number, number of guests).                                                                            |
| Order Item     | Value object representing one line of an Order: a Menu Item and its quantity.                                                                               |
| Order Status   | Enumeration of an Order's lifecycle state: ```PENDING```, ```PREPARING```, ```READY```, ```ON_THE_WAY``` (delivery only), ```COMPLETED```, ```CANCELLED```. |

### Payment Bounded-Context

| Term                  | Definition                                                                                                         |
|-----------------------|--------------------------------------------------------------------------------------------------------------------|
| Payment               | Aggregate representing a monetary transaction charged against an Order.                                            |
| Payment Status        | Enumeration of a Payment's state: ```PENDING```, ```COMPLETED```, ```FAILED```, ```CANCELLED```.                   |
| Payment Method        | The means used to pay for an Order (e.g. card).                                                                    |
| Payment Gateway       | The external system that authorizes or rejects a Payment; a fake gateway stands in for a real one in this project. |
| Payment-Success Event | The Kafka event published once a Payment completes, consumed by notification-service.                              |

### Scheduler Bounded-Context

| Term              | Definition                                                                                                                                        |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| Delivery Schedule | The logistics plan (timing, assignment) for getting a Delivery Order to a Customer, managed as middleware between the Customer and order-service. |

### Table Reservation Bounded-Context

| Term              | Definition                                                                                   |
|-------------------|----------------------------------------------------------------------------------------------|
| Table Reservation | A Customer's booking of a table at a Restaurant for a given date, time and number of guests. |

### Notification Bounded-Context

| Term                         | Definition                                                                                  |
|------------------------------|---------------------------------------------------------------------------------------------|
| Notification                 | A message sent to a User in reaction to a Kafka event published by another bounded context. |
| User Notification            | A Notification triggered by a user-related event (e.g. registration).                       |
| Payment-Success Notification | A Notification triggered when notification-service consumes a Payment-Success Event.        |
