Feature: Create an order

  Scenario: A client creates an delivery order
    Given a valid delivery order
    When the client places the order
    Then order is created successfully