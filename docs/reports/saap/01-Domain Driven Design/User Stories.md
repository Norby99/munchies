# User Stories

```gherkin
Feature: Authentication and Account Management

  Scenario: User registers to the system
    Given I am not registered
    When I register with valid credentials
    Then I should receive a personal auth token
    And I should be able to query my info

  Scenario: User logs in to the system
    Given I am registered
    And I am not logged in
    When I log in with valid credentials
    Then I should receive a personal auth token
    And I should be able to access the protected features

  Scenario: User updates its profile
    Given I am a logged user
    When I update my profile
    Then my profile should be updated
    And only the allowed fields should be changed

Feature: Restaurant Browsing

  Scenario: Client browses a restaurant menu
    Given I am a logged user
    And there is a restaurant with a menu
    When I open the restaurant menu
    Then I should see the restaurant and its dishes
    And each dish should show its details and availability

Feature: Menu Management

  Scenario: Restaurant staff marks a dish as unavailable
    Given I am a logged restaurant staff member
    And my restaurant has an available dish
    When I mark the dish as unavailable
    Then the dish should be shown as unavailable in the menu
    And clients should not be able to order the dish

Feature: Order Management

  Scenario: Client places a delivery order
    Given I am a logged user
    And I have a valid delivery order
    When I place the order
    Then the order should be created

  Scenario: Client pays for an order
    Given I am a logged user
    And I have placed a delivery order
    When I pay for the order
    Then the payment should be completed
    And the order should be confirmed

  Scenario: Client tracks the order status
    Given I am a logged user
    And I have placed a delivery order
    When the order status advances
    Then I should see the updated order status

Feature: Notifications

  Scenario: Client is notified of a successful payment
    Given I am a logged user
    And I have placed a delivery order
    When I pay for the order
    Then the payment should be completed
    And I should receive a payment confirmation notification

Feature: Table Reservations

  Scenario: Client reserves a table
    Given I am a logged user
    And there is a restaurant with a free table
    When I reserve a table at the restaurant
    Then the reservation should be created

```
