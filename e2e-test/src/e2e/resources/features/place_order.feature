Feature: Create an order

  Scenario: A authenticated client creates an delivery order
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    Then order is created successfully
