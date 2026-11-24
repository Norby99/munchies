# User Stories

The user stories are written as Gherkin scenarios, grouped by feature. Each one is the acceptance criterion of one or
more [Functional Requirements](functional-requirements.md), which list the scenario that specifies them.

## Account

```gherkin
Feature: Authentication and Account Management

  Scenario: User registers to the system
    Given I am not registered
    When I register with a username, an email, a password and a role
    Then I should be logged in
    And I should be able to query my info

  Scenario: User logs in to the system
    Given I am registered
    And I am not logged in
    When I log in with valid credentials
    Then I should be logged in
    And I should be able to access the features of my role

  Scenario: User verifies their email address
    Given I have just registered
    When I enter the confirmation key I was sent
    Then my email address should be marked as verified

  Scenario: User updates their profile
    Given I am a logged user
    When I change my username and my email
    Then my profile should be updated
    And my role should not change

  Scenario: User changes their password
    Given I am a logged user
    When I change my password giving the current one
    Then I should be able to log in only with the new password
```

## Restaurant management

```gherkin
Feature: Restaurant Management

  Scenario: Manager creates a restaurant
    Given I am a logged manager
    When I create a restaurant with its name, address, phone and email
    Then the restaurant should appear among my restaurants

  Scenario: Manager builds a menu
    Given I am a logged manager
    And I own a restaurant
    When I create a menu with a category and a menu item with a variation
    Then customers should see the menu item, its price and its variation in the restaurant's menu

  Scenario: Manager configures the tables
    Given I am a logged manager
    And I own a restaurant
    When I set its tables with their number of seats
    Then customers should be able to reserve those tables

  Scenario: Manager configures the kitchen capacity
    Given I am a logged manager
    And I own a restaurant
    When I set its opening hours, its slot length and how many items the kitchen can take on per slot
    Then customers should be offered only slots within the opening hours
    And no slot should accept more items than the kitchen can take on

  Scenario: Manager configures deliveries
    Given I am a logged manager
    And I own a restaurant
    When I set its position on the map and how many delivery trips can leave per slot
    Then customers should be able to order delivery from it
    And no slot should accept more trips than I allowed
```

## Browsing and ordering

```gherkin
Feature: Restaurant Browsing

  Scenario: Customer searches for a restaurant
    Given I am a logged customer
    When I search for a restaurant by its name or address
    Then I should see the restaurants that match

  Scenario: Customer browses a restaurant menu
    Given I am a logged customer
    And there is a restaurant with a menu
    When I open the restaurant
    Then I should see its menus, with the categories, the items, their variations and their prices

Feature: Ordering

  Scenario: Customer chooses a time slot
    Given I am a logged customer
    And there is a restaurant open on the chosen day
    When I ask for its available slots on that day
    Then I should see only the slots I can still book, with the capacity left in each one

  Scenario: Customer places a delivery order
    Given I am a logged customer
    And I have chosen an available slot
    When I place a delivery order with my address located on the map
    Then the order should be created as pending
    And it should be booked in the chosen slot

  Scenario: Customer cannot order in a full slot
    Given I am a logged customer
    And a slot has no kitchen capacity left for my items
    When I place an order in that slot
    Then the order should be refused
    And I should be asked to choose another slot

  Scenario: Customer cancels a pending order
    Given I am a logged customer
    And I have placed an order that is still pending
    When I cancel the order
    Then the order should be cancelled
    And the restaurant should not prepare it
```

## Order handling and tracking

```gherkin
Feature: Order Handling

  Scenario: Preparation starts automatically
    Given a pending order is booked in a slot
    When the preparation lead time before the slot is reached
    Then the order should move to preparing without any action of the manager

  Scenario: Manager advances an order
    Given I am a logged manager
    And my restaurant has an order in preparation
    When I move the order to its next status
    Then the order should be ready
    And the customer should see the new status

  Scenario: Manager checks the kitchen load
    Given I am a logged manager
    When I open the load of my restaurant for a day
    Then I should see, for each slot, the capacity booked and the capacity available

Feature: Order Tracking

  Scenario: Customer tracks the order status
    Given I am a logged customer
    And I have placed an order
    When the order status advances
    Then I should see the updated order status

  Scenario: Customer follows the driver
    Given I am a logged customer
    And my delivery order is on the way
    When I open the order
    Then I should see the position of the driver on a map
```

## Payment

```gherkin
Feature: Payment

  Scenario: Customer pays for an order
    Given I am a logged customer
    And I have placed an order
    When I pay the order choosing a payment method
    Then I should be charged the order total
    And the order should be marked as paid

  Scenario: Customer cannot pay an order twice
    Given I am a logged customer
    And my order is already paid
    When I pay the order again
    Then the payment should be refused
    And I should not be charged

Feature: Notifications

  Scenario: Customer is notified of a successful payment
    Given I am a logged customer
    And I have placed an order
    When I pay for the order
    Then I should receive a payment confirmation notification
```

## Table reservations

```gherkin
Feature: Table Reservations

  Scenario: Customer reserves a table
    Given I am a logged customer
    And a restaurant has a free table for my party at the chosen time
    When I reserve a table for that time and number of guests
    Then the reservation should be accepted
    And I should be told which table I have

  Scenario: Customer cannot reserve a full restaurant
    Given I am a logged customer
    And every table that can seat my party is taken at the chosen time
    When I reserve a table for that time
    Then the reservation should be refused

  Scenario: Customer modifies a reservation
    Given I am a logged customer
    And I have an accepted reservation
    When I change its time or its number of guests
    Then the reservation should be updated, at a table that fits the new party

  Scenario: Manager sees the reservations
    Given I am a logged manager
    When I open the reservations of my restaurant
    Then I should see every reservation, with its time, its number of guests and its table
```
