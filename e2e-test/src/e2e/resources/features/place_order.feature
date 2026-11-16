Feature: Create an order

  Scenario: A authenticated client creates an delivery order
    Given a restaurant with a dish on its menu
    And an authenticated client
    And a valid delivery order
    When the client places the order
    Then order is created successfully
